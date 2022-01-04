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

    public List<QuestionnaireResponseModel> getQuestionnaireResponses(String carePlanId, PageDetails pageDetails) throws ServiceException, AccessValidationException {
        FhirLookupResult lookupResult = fhirClient.lookupQuestionnaireResponses(carePlanId);
        List<QuestionnaireResponse> responses = lookupResult.getQuestionnaireResponses();
        if(responses.isEmpty()) {
            return List.of();
        }

        validateCorrectSubject(lookupResult);

        // Sort the responses by priority.
        responses = sortResponsesByDate(responses);

        // Perform paging if required.
        if(pageDetails != null) {
            responses = pageResponses(responses, pageDetails);
        }
        // Map and return the responses
        return responses
                .stream()
                .map(qr -> fhirMapper.mapQuestionnaireResponse(qr, lookupResult))
                .collect(Collectors.toList());
    }

    public Optional<QuestionnaireResponseModel> getQuestionnaireResponseById(QualifiedId id) throws ServiceException, AccessValidationException {
        FhirLookupResult lookupResult = fhirClient.lookupQuestionnaireResponseById(id.toString());

        Optional<QuestionnaireResponse> questionnaireResponse = lookupResult.getQuestionnaireResponse(id.toString());
        if(!questionnaireResponse.isPresent()) {
            return Optional.empty();
        }

        // Map the resource
        QuestionnaireResponseModel mappedQuestionnaireResponse = fhirMapper.mapQuestionnaireResponse(questionnaireResponse.get(), lookupResult);

        validateCorrectSubject(lookupResult);

        return Optional.of(mappedQuestionnaireResponse);
    }

    public String submitQuestionnaireResponse(QuestionnaireResponseModel questionnaireResponseModel, String cpr) throws ServiceException, AccessValidationException {
        // Look up the careplan indicated in the response. Check that this is the user's active careplan.
        var carePlanResult = fhirClient.lookupActiveCarePlan(cpr);
        if(carePlanResult.getCarePlans().isEmpty()) {
            throw new ServiceException(String.format("No CarePnlan found for cpr %s", cpr), ErrorKind.BAD_REQUEST, ErrorDetails.NO_ACTIVE_CAREPLAN_EXISTS);
        }
        if(carePlanResult.getCarePlans().size() > 1) {
            throw new IllegalStateException(String.format("Error looking up active careplan! Expected to retrieve exactly one reosurce!"));
        }
        var carePlanModel = fhirMapper.mapCarePlan(carePlanResult.getCarePlans().get(0), carePlanResult);

        // Check that the carePlan indicated by the client is that of the patient's active careplan
        if(!questionnaireResponseModel.getCarePlanId().equals(carePlanModel.getId())) {
            throw new ServiceException("The provided CarePlan id does not identify the patient's current active careplan.", ErrorKind.BAD_REQUEST, ErrorDetails.WRONG_CAREPLAN_ID);
        }

        // Update the frequency timestamps on the careplan (the specific activity and the careplan itself)
        refreshFrequencyTimestamps(carePlanModel, questionnaireResponseModel.getQuestionnaireId());

        // Extract thresholds from the careplan, and compute the triaging category for the response
        initializeQuestionnaireResponseAttributes(questionnaireResponseModel, carePlanModel);

        // Save the response, along with the updated careplan, and return the generated QuestionnaireResponse id.
        return fhirClient.saveQuestionnaireResponse(fhirMapper.mapQuestionnaireResponseModel(questionnaireResponseModel), fhirMapper.mapCarePlanModel(carePlanModel));
    }

    private void refreshFrequencyTimestamps(CarePlanModel carePlanModel, QualifiedId questionnaireId) {
        // Get the wrapper object that we wish to update
        var questionnaireWrapper = getMatchingQuestionnaireWrapper(carePlanModel, questionnaireId);

        // Compute the new deadline from the current point in time. Invoke 'next' once to get the current deadline, then
        // invoke 'next' again to get the new deadline. This works regardless of whether the current submission is overdue or not.
        var nextDeadline = new FrequencyEnumerator(dateProvider.now(), questionnaireWrapper.getFrequency()).next().next().getPointInTime();
        questionnaireWrapper.setSatisfiedUntil(nextDeadline);

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
                .sorted( (a,b) -> b.getAuthored().compareTo(a.getAuthored()) )
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
}
