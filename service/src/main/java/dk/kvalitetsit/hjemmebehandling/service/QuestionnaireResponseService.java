package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.fhir.*;
import dk.kvalitetsit.hjemmebehandling.model.PatientModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.model.QuestionnaireResponseModel;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import org.apache.commons.lang3.tuple.ImmutablePair;
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

    public QuestionnaireResponseService(FhirClient fhirClient, FhirMapper fhirMapper, AccessValidator accessValidator) {
        super(accessValidator);

        this.fhirClient = fhirClient;
        this.fhirMapper = fhirMapper;
    }

    public List<QuestionnaireResponseModel> getQuestionnaireResponses(String carePlanId) throws ServiceException, AccessValidationException {
        FhirLookupResult lookupResult = fhirClient.lookupQuestionnaireResponses(carePlanId);
        List<QuestionnaireResponse> responses = lookupResult.getQuestionnaireResponses();
        if(responses.isEmpty()) {
            return List.of();
        }

        // Validate that the user is allowed to retrieve the QuestionnaireResponses.
        validateAccess(responses);

        // Map and return the responses
        return responses
                .stream()
                .map(qr -> fhirMapper.mapQuestionnaireResponse(qr, lookupResult))
                .collect(Collectors.toList());
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

        // Update the frequency timestamps on the careplan (the specific activity and the careplan itself)



        // Extract thresholds from the careplan, and compute the triaging category for the response
        // (And initialize various other attributes)
        questionnaireResponseModel.setAuthorId(new QualifiedId("Patient/patient-1"));
        questionnaireResponseModel.setSourceId(new QualifiedId("Patient/patient-1"));
        questionnaireResponseModel.setAnswered(Instant.now());
        questionnaireResponseModel.setExaminationStatus(ExaminationStatus.NOT_EXAMINED);
        questionnaireResponseModel.setTriagingCategory(TriagingCategory.GREEN);
        questionnaireResponseModel.setPatient(new PatientModel());
        questionnaireResponseModel.getPatient().setId(new QualifiedId("Patient/patient-1"));

        // Save the response, along with the updated careplan, and return the generated QuestionnaireResponse id.
        return fhirClient.saveQuestionnaireResponse(fhirMapper.mapQuestionnaireResponseModel(questionnaireResponseModel), fhirMapper.mapCarePlanModel(carePlanModel));
    }
}
