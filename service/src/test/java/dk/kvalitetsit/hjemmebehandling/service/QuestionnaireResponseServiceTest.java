package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.fhir.*;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionnaireResponseModel;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class QuestionnaireResponseServiceTest {
    @InjectMocks
    private QuestionnaireResponseService subject;

    @Mock
    private FhirClient fhirClient;

    @Mock
    private FhirMapper fhirMapper;

    @Mock
    private AccessValidator accessValidator;

    private static final String CAREPLAN_ID_1 = "careplan-1";
    private static final String CAREPLAN_ID_2 = "careplan-2";
    private static final String ORGANIZATION_ID_1 = "organization-1";
    private static final String PATIENT_ID = "patient-1";
    private static final String QUESTIONNAIRE_ID_1 = "questionnaire-1";
    private static final String QUESTIONNAIRE_RESPONSE_ID_1 = "questionnaireresponse-1";

    @Test
    public void getQuestionnaireResponses_responsesPresent_returnsResponses() throws Exception {
        // Arrange
        String carePlanId = CAREPLAN_ID_1;

        QuestionnaireResponse response = buildQuestionnaireResponse(QUESTIONNAIRE_RESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID);
        FhirLookupResult lookupResult = FhirLookupResult.fromResource(response);
        Mockito.when(fhirClient.lookupQuestionnaireResponses(carePlanId)).thenReturn(lookupResult);

        QuestionnaireResponseModel responseModel = new QuestionnaireResponseModel();
        Mockito.when(fhirMapper.mapQuestionnaireResponse(response, lookupResult)).thenReturn(responseModel);

        // Act
        List<QuestionnaireResponseModel> result = subject.getQuestionnaireResponses(carePlanId);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(responseModel));
    }

    @Test
    public void getQuestionnaireResponses_responsesMissing_returnsEmptyList() throws Exception {
        // Arrange
        String carePlanId = CAREPLAN_ID_1;

        Mockito.when(fhirClient.lookupQuestionnaireResponses(carePlanId)).thenReturn(FhirLookupResult.fromResources());

        // Act
        List<QuestionnaireResponseModel> result = subject.getQuestionnaireResponses(carePlanId);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    public void getQuestionnaireResponses_accessViolation_throwsException() throws Exception {
        // Arrange
        String carePlanId = CAREPLAN_ID_1;

        QuestionnaireResponse response = new QuestionnaireResponse();
        FhirLookupResult lookupResult = FhirLookupResult.fromResource(response);
        Mockito.when(fhirClient.lookupQuestionnaireResponses(carePlanId)).thenReturn(lookupResult);

        Mockito.doThrow(AccessValidationException.class).when(accessValidator).validateAccess(List.of(response));

        // Act

        // Assert
        assertThrows(AccessValidationException.class, () -> subject.getQuestionnaireResponses(carePlanId));
    }

    @Test
    public void submitQuestionnaireResponse_noActiveCarePlan_throwsException() {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(FhirLookupResult.fromResources());

        // Act

        // Assert
        assertThrows(ServiceException.class, () -> subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr));
    }

    @Test
    public void submitQuestionnaireResponse_multipleCarePlans_throwsException() {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(FhirLookupResult.fromResources(buildCarePlan(CAREPLAN_ID_1), buildCarePlan(CAREPLAN_ID_2)));

        // Act

        // Assert
        assertThrows(IllegalStateException.class, () -> subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr));
    }

    @Test
    public void submitQuestionnaireResponse_success_returnsGeneratedId() throws Exception {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = new CarePlanModel();
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        Mockito.when(fhirMapper.mapQuestionnaireResponseModel(questionnaireResponseModel)).thenReturn(questionnaireResponse);

        Mockito.when(fhirMapper.mapCarePlanModel(carePlanModel)).thenReturn(carePlan);

        Mockito.when(fhirClient.saveQuestionnaireResponse(questionnaireResponse, carePlan)).thenReturn(QUESTIONNAIRE_RESPONSE_ID_1);

        // Act
        String result = subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

        // Assert
        assertEquals(QUESTIONNAIRE_RESPONSE_ID_1, result);
    }

    private CarePlan buildCarePlan(String carePlanId) {
        CarePlan carePlan = new CarePlan();

        carePlan.setId(carePlanId);

        return carePlan;
    }

    private QuestionnaireResponse buildQuestionnaireResponse(String questionnaireResponseId, String questionnaireId, String patientId) {
        return buildQuestionnaireResponse(questionnaireResponseId, questionnaireId, patientId, ORGANIZATION_ID_1);
    }

    private QuestionnaireResponse buildQuestionnaireResponse(String questionnaireResponseId, String questionnaireId, String patientId, String organizationId) {
        QuestionnaireResponse response = new QuestionnaireResponse();

        response.setId(questionnaireResponseId);
        response.setQuestionnaire(questionnaireId);
        response.setSubject(new Reference(patientId));
        response.addExtension(Systems.ORGANIZATION, new Reference(organizationId));

        return response;
    }

    private QuestionnaireResponseModel buildQuestionnaireResponseModel() {
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();

        return questionnaireResponseModel;
    }
}