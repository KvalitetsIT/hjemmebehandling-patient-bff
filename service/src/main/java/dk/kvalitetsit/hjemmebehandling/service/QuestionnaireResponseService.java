package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.fhir.*;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.service.frequency.FrequencyEnumerator;
import dk.kvalitetsit.hjemmebehandling.service.triage.TriageEvaluator;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import dk.kvalitetsit.hjemmebehandling.util.DateProvider;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class QuestionnaireResponseService extends AccessValidatingService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireResponseService.class);

    private FhirClient fhirClient;

    private FhirMapper fhirMapper;

    private DateProvider dateProvider;

    private TriageEvaluator triageEvaluator;

    public QuestionnaireResponseService(FhirClient fhirClient, FhirMapper fhirMapper, DateProvider dateProvider, TriageEvaluator triageEvaluator, AccessValidator accessValidator) {
        super(accessValidator);

        this.fhirClient = fhirClient;
        this.fhirMapper = fhirMapper;
        this.dateProvider = dateProvider;
        this.triageEvaluator = triageEvaluator;
    }

    public List<QuestionnaireResponseModel> getQuestionnaireResponses(String carePlanId, List<String> questionnaireIds, PageDetails pageDetails) throws ServiceException, AccessValidationException {

        List<Questionnaire> historicalQuestionnaires = fhirClient.lookupVersionsOfQuestionnaireById(questionnaireIds);
        FhirLookupResult lookupResult = fhirClient.lookupQuestionnaireResponses(carePlanId, questionnaireIds);
        List<QuestionnaireResponse> responses = lookupResult.getQuestionnaireResponses();
        if (responses.isEmpty()) {
            return List.of();
        }

        validateCorrectSubject(lookupResult);

        // Sort the responses by priority.
        responses = sortResponsesByDate(responses);

        // Perform paging if required.
        if (pageDetails != null) {
            responses = pageResponses(responses, pageDetails);
        }
        // Map and return the responses
        return responses
                .stream()
                .map(qr -> fhirMapper.mapQuestionnaireResponse(qr, lookupResult, historicalQuestionnaires))
                .collect(Collectors.toList());
    }

    public Optional<QuestionnaireResponseModel> getQuestionnaireResponseById(QualifiedId id) throws ServiceException, AccessValidationException {
        FhirLookupResult lookupResult = fhirClient.lookupQuestionnaireResponseById(id.toString());

        Optional<QuestionnaireResponse> questionnaireResponse = lookupResult.getQuestionnaireResponse(id.toString());
        if (!questionnaireResponse.isPresent()) {
            return Optional.empty();
        }

        List<String> questionnaireIds = lookupResult.getQuestionnaires().stream().map(questionnaire -> questionnaire.getIdElement().toUnqualifiedVersionless().getIdPart()).collect(Collectors.toList());

        List<Questionnaire> historicalQuestionnaires = fhirClient.lookupVersionsOfQuestionnaireById(questionnaireIds);

        // Map the resource
        QuestionnaireResponseModel mappedQuestionnaireResponse = fhirMapper.mapQuestionnaireResponse(questionnaireResponse.get(), lookupResult, historicalQuestionnaires);

        validateCorrectSubject(lookupResult);

        return Optional.of(mappedQuestionnaireResponse);
    }

    public String submitQuestionnaireResponse(QuestionnaireResponseModel questionnaireResponseModel, String cpr) throws ServiceException, AccessValidationException {
        // Look up the careplan indicated in the response. Check that this is the user's active careplan.
        var carePlanResult = fhirClient.lookupActiveCarePlans(cpr);
        if (carePlanResult.getCarePlans().isEmpty()) {
            throw new ServiceException(String.format("No CarePlan found for cpr %s", cpr), ErrorKind.BAD_REQUEST, ErrorDetails.NO_ACTIVE_CAREPLAN_EXISTS);
        }

        var carePlanId = questionnaireResponseModel.getCarePlanId().getId();

        var careplansWithMatchingId = carePlanResult.getCarePlans().stream().filter(carePlan -> {
            var id = carePlan.getIdElement().getIdPart();
            return id.equals(carePlanId);
        }).collect(Collectors.toList());


        // Check that the carePlan indicated by the client is that of the patient's active careplan
        if (careplansWithMatchingId.size() < 1) {
            throw new ServiceException("The provided CarePlan id does not identify the patient's current active careplan.", ErrorKind.BAD_REQUEST, ErrorDetails.WRONG_CAREPLAN_ID);
        }

        if (careplansWithMatchingId.size() != 1) {
            throw new IllegalStateException(String.format("Error looking up active careplan! Expected to retrieve exactly one resource with matching id!"));
        }

        var carePlanModel = fhirMapper.mapCarePlan(careplansWithMatchingId.get(0), carePlanResult);

        // make sure the questionnaire is still active
        boolean questionnaireExists = carePlanModel.getQuestionnaires().stream().anyMatch(q -> q.getQuestionnaire().getId().equals(questionnaireResponseModel.getQuestionnaireId()));
        if (!questionnaireExists) {
            throw new ServiceException("The questionnaire is no longer active", ErrorKind.BAD_REQUEST, ErrorDetails.QUESTIONNAIRE_DOES_NOT_EXIST);
        }

        // Update the frequency timestamps on the careplan (the specific activity and the careplan itself)
        refreshFrequencyTimestamps(carePlanModel, questionnaireResponseModel.getQuestionnaireId());

        // Extract thresholds from the careplan, and compute the triaging category for the response
        initializeQuestionnaireResponseAttributes(questionnaireResponseModel, carePlanModel);

        // Save the response, along with the updated careplan, and return the generated QuestionnaireResponse id.
        return fhirClient.saveQuestionnaireResponse(fhirMapper.mapQuestionnaireResponseModel(questionnaireResponseModel), fhirMapper.mapCarePlanModel(carePlanModel));
    }

    public String getCallToAction(QuestionnaireResponseModel questionnaireResponseModel) throws ServiceException {
        String questionnaireId = questionnaireResponseModel.getQuestionnaireId().toString();
        var questionnaireResult = fhirClient.lookupQuestionnaires(List.of(questionnaireId));
        if (questionnaireResult.getQuestionnaires().isEmpty()) {
            throw new ServiceException(String.format("No Questionnaire found for id %s", questionnaireId), ErrorKind.BAD_REQUEST, ErrorDetails.QUESTIONNAIRE_DOES_NOT_EXIST);
        }

        Questionnaire questionnaire = questionnaireResult.getQuestionnaire(questionnaireId).get();
        QuestionnaireModel questionnaireModel = fhirMapper.mapQuestionnaire(questionnaire);

        String callToActionString = "";
        // for call-to-actions:
        QuestionModel callToAction = questionnaireModel.getCallToAction();
        if (callToAction != null && callToAction.getEnableWhens() != null) {
            // find matchende spørgmål for call-to-actions enableWhen
            for (QuestionModel.EnableWhen enableWhen : callToAction.getEnableWhens()) {
                // led efter et svar der matcher vores enable-when condition
                Optional<QuestionAnswerPairModel> answer = questionnaireResponseModel.getQuestionAnswerPairs().stream()
                        .filter(qa -> qa.getAnswer().equals(enableWhen.getAnswer()))
                        .findFirst();

                if (answer.isPresent()) {
                    // vi har et match
                    callToActionString = callToAction.getText();
                }
            }
        }

        return callToActionString;
    }

    private void refreshFrequencyTimestamps(CarePlanModel carePlanModel, QualifiedId questionnaireId) {
        // Get the wrapper object that we wish to update
        var questionnaireWrapper = getMatchingQuestionnaireWrapper(carePlanModel, questionnaireId);

        var currentSatisfiedUntil = questionnaireWrapper.getSatisfiedUntil();
        var satisfiedUntil = new FrequencyEnumerator(questionnaireWrapper.getFrequency(), currentSatisfiedUntil).getSatisfiedUntil(dateProvider.now());
        questionnaireWrapper.setSatisfiedUntil(satisfiedUntil);

        // Now that the timestamp is updated for the questionnaire, recompute the timestamp for the careplan as well.
        refreshFrequencyTimestampForCarePlan(carePlanModel);
    }

    private void refreshFrequencyTimestampForCarePlan(CarePlanModel carePlanModel) {
        var carePlanSatisfiedUntil = carePlanModel.getQuestionnaires()
                .stream()
                .map(qw -> qw.getSatisfiedUntil())
                .min(Comparator.naturalOrder())
                .orElse(Instant.MAX);
        carePlanModel.setSatisfiedUntil(carePlanSatisfiedUntil);
    }

    private void initializeQuestionnaireResponseAttributes(QuestionnaireResponseModel questionnaireResponseModel, CarePlanModel carePlanModel) {
        questionnaireResponseModel.setAuthorId(carePlanModel.getPatient().getId());
        questionnaireResponseModel.setSourceId(carePlanModel.getPatient().getId());
        questionnaireResponseModel.setAnswered(dateProvider.now());
        questionnaireResponseModel.setExaminationStatus(ExaminationStatus.NOT_EXAMINED);
        questionnaireResponseModel.setPatient(carePlanModel.getPatient());

        initializeTriagingCategory(questionnaireResponseModel, carePlanModel);
    }

    private void initializeTriagingCategory(QuestionnaireResponseModel questionnaireResponseModel, CarePlanModel carePlanModel) {
        var answers = getAnswers(questionnaireResponseModel);

        var thresholds = getThresholds(carePlanModel, questionnaireResponseModel.getQuestionnaireId());

        questionnaireResponseModel.setTriagingCategory(triageEvaluator.determineTriagingCategory(answers, thresholds));
    }

    private List<AnswerModel> getAnswers(QuestionnaireResponseModel questionnaireResponseModel) {
        return questionnaireResponseModel.getQuestionAnswerPairs()
                .stream()
                .map(qa -> qa.getAnswer())
                .collect(Collectors.toList());
    }

    private List<QuestionnaireResponse> sortResponsesByDate(List<QuestionnaireResponse> responses) {
        return responses
                .stream()
                .sorted((a, b) -> b.getAuthored().compareTo(a.getAuthored()))
                .collect(Collectors.toList());
    }

    private List<ThresholdModel> getThresholds(CarePlanModel carePlanModel, QualifiedId questionnaireId) {
        var questionnaireWrapper = getMatchingQuestionnaireWrapper(carePlanModel, questionnaireId);
        return questionnaireWrapper.getThresholds();
    }

    private List<QuestionnaireResponse> pageResponses(List<QuestionnaireResponse> responses, PageDetails pageDetails) {
        return responses
                .stream()
                .skip((pageDetails.getPageNumber() - 1) * pageDetails.getPageSize())
                .limit(pageDetails.getPageSize())
                .collect(Collectors.toList());
    }

    private QuestionnaireWrapperModel getMatchingQuestionnaireWrapper(CarePlanModel carePlanModel, QualifiedId questionnaireId) {
        return carePlanModel.getQuestionnaires()
                .stream()
                .filter(q -> q.getQuestionnaire().getId().equals(questionnaireId))
                .findFirst()
                .get();
    }

    public List<QuestionnaireResponseModel> getQuestionnaireResponsesForMultipleCarePlans(List<String> carePlanIds, List<String> questionnaireIds, PageDetails pageDetails) throws AccessValidationException {
        List<Questionnaire> historicalQuestionnaires = fhirClient.lookupVersionsOfQuestionnaireById(questionnaireIds);
        FhirLookupResult lookupResult = fhirClient.lookupQuestionnaireResponsesForMultipleCarePlans(carePlanIds, questionnaireIds);
        List<QuestionnaireResponse> responses = lookupResult.getQuestionnaireResponses();
        if (responses.isEmpty()) {
            return List.of();
        }

        validateCorrectSubject(lookupResult);

        // Sort the responses by priority.
        responses = sortResponsesByDate(responses);

        // Perform paging if required.
        if (pageDetails != null) {
            responses = pageResponses(responses, pageDetails);
        }
        // Map and return the responses
        return responses
                .stream()
                .map(qr -> fhirMapper.mapQuestionnaireResponse(qr, lookupResult, historicalQuestionnaires)).collect(Collectors.toList());
    }
}
