package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.fhir.*;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.service.frequency.FrequencyEnumerator;
import dk.kvalitetsit.hjemmebehandling.service.triage.TriageEvaluator;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import dk.kvalitetsit.hjemmebehandling.util.DateProvider;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.stream.Collectors;

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
    private DateProvider dateProvider;

    @Mock
    private TriageEvaluator triageEvaluator;

    @Mock
    private AccessValidator accessValidator;

    private static final String CAREPLAN_ID_1 = "CarePlan/careplan-1";
    private static final String CAREPLAN_ID_2 = "CarePlan/careplan-2";
    private static final String ORGANIZATION_ID_1 = "Organization/organization-1";
    private static final String PATIENT_ID = "Patient/patient-1";
    private static final String QUESTIONNAIRE_ID_1 = "Questionnaire/questionnaire-1";
    private static final String QUESTIONNAIRE_ID_2 = "Questionnaire/questionnaire-2";
    private static final String QUESTIONNAIRE_RESPONSE_ID_1 = "QuestionnaireResponse/questionnaireresponse-1";

    private static final Instant POINT_IN_TIME = Instant.parse("2021-11-26T10:00:00.000Z");

    @Test
    public void getQuestionnaireResponses_responsesPresent_returnsResponses() throws Exception {
        // Arrange
        String carePlanId = CAREPLAN_ID_1;

        QuestionnaireResponse response = buildQuestionnaireResponse(QUESTIONNAIRE_RESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID);
        Patient patient = buildPatient(PATIENT_ID);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(response, patient);
        Mockito.when(fhirClient.lookupQuestionnaireResponses(carePlanId, new ArrayList<>())).thenReturn(lookupResult);
        Mockito.when(fhirClient.lookupVersionsOfQuestionnaireById(List.of())).thenReturn(List.of());


        QuestionnaireResponseModel responseModel = new QuestionnaireResponseModel();
        Mockito.when(fhirMapper.mapQuestionnaireResponse(response, lookupResult, List.of())).thenReturn(responseModel);

        // Act
        int pageNumber = 1;
        int pageSize = 10;
        PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
        List<QuestionnaireResponseModel> result = subject.getQuestionnaireResponses(carePlanId, new ArrayList<>(),pageDetails);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains(responseModel));
    }

    @Test
    public void getQuestionnaireResponses_responsesMissing_returnsEmptyList() throws Exception {
        // Arrange
        String carePlanId = CAREPLAN_ID_1;

        Mockito.when(fhirClient.lookupQuestionnaireResponses(carePlanId, new ArrayList<>())).thenReturn(FhirLookupResult.fromResources());

        // Act
        int pageNumber = 1;
        int pageSize = 10;
        PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
        List<QuestionnaireResponseModel> result = subject.getQuestionnaireResponses(carePlanId, new ArrayList<>(), pageDetails);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    public void getQuestionnaireResponses_ReturnOneItem_WhenPagesizeIsOne() throws Exception {
        //Arrange
        QuestionnaireResponseService qrservice = new QuestionnaireResponseService(fhirClient, fhirMapper, null,triageEvaluator, accessValidator);


        QuestionnaireResponse response = new QuestionnaireResponse();
        Patient patient = new Patient();
        FhirLookupResult questionnaireResponseResult = FhirLookupResult.fromResource(response);
        FhirLookupResult patientResult = FhirLookupResult.fromResource(patient);
        FhirLookupResult lookupResult = questionnaireResponseResult.merge(patientResult);

        Mockito.when(fhirClient.lookupQuestionnaireResponses(null, new ArrayList<>())).thenReturn(lookupResult);



        PageDetails pageDetails = new PageDetails(1,1);
        List<QuestionnaireResponseModel> result = qrservice.getQuestionnaireResponses(null,  new ArrayList<>(),pageDetails);


        assertEquals(1, result.size());
    }

    @Test
    public void getQuestionnaireResponses_ReturnSortedPages_WhenTwoPages() throws Exception {
        //Arrange
        QuestionnaireResponseService qrservice = new QuestionnaireResponseService(fhirClient, fhirMapper, null, triageEvaluator,accessValidator);

        QuestionnaireResponse response1 = new QuestionnaireResponse();
        response1.setAuthored(new Date(1,Calendar.JANUARY,1));
        response1.setId("1");

        QuestionnaireResponse response2 = new QuestionnaireResponse();
        response2.setAuthored(new Date(2, Calendar.FEBRUARY,2));
        response2.setId("2");

        QuestionnaireResponse response3 = new QuestionnaireResponse();
        response3.setAuthored(new Date(3,Calendar.MARCH,3));
        response3.setId("3");

        QuestionnaireResponse response4 = new QuestionnaireResponse();
        response4.setAuthored(new Date(4,Calendar.APRIL,4));
        response4.setId("4");

        FhirLookupResult questionnaireResponseResult = FhirLookupResult.fromResources(response1,response3, response4, response2);
        Patient patient = new Patient();
        FhirLookupResult patientResult = FhirLookupResult.fromResource(patient);
        FhirLookupResult lookupResult1 = questionnaireResponseResult.merge(patientResult);

        Mockito.when(fhirClient.lookupVersionsOfQuestionnaireById(List.of())).thenReturn(List.of());
        Mockito.when(fhirClient.lookupQuestionnaireResponses(null, new ArrayList<>())).thenReturn(lookupResult1);
        Mockito.when(fhirMapper.mapQuestionnaireResponse(response1,lookupResult1, List.of())).thenReturn(questionnaireResponseToQuestionnaireResponseModel(response1));
        Mockito.when(fhirMapper.mapQuestionnaireResponse(response2,lookupResult1, List.of())).thenReturn(questionnaireResponseToQuestionnaireResponseModel(response2));
        Mockito.when(fhirMapper.mapQuestionnaireResponse(response3,lookupResult1, List.of())).thenReturn(questionnaireResponseToQuestionnaireResponseModel(response3));
        Mockito.when(fhirMapper.mapQuestionnaireResponse(response4,lookupResult1, List.of())).thenReturn(questionnaireResponseToQuestionnaireResponseModel(response4));

        //ACTION
        PageDetails pageDetails1 = new PageDetails(1,2);
        List<QuestionnaireResponseModel> result1 = qrservice.getQuestionnaireResponses(null, new ArrayList<>(),pageDetails1);

        PageDetails pageDetails2 = new PageDetails(2,2);
        List<QuestionnaireResponseModel> result2 = qrservice.getQuestionnaireResponses(null, new ArrayList<>(),pageDetails2);

        //ASSERT
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());

        assertEquals("4",result1.get(0).getId().getId());
        assertEquals("3",result1.get(1).getId().getId());
        assertEquals("2",result2.get(0).getId().getId());
        assertEquals("1",result2.get(1).getId().getId());
    }
    QuestionnaireResponseModel questionnaireResponseToQuestionnaireResponseModel(QuestionnaireResponse questionnaireResponse){
        QuestionnaireResponseModel responseModel1 = new QuestionnaireResponseModel();
        responseModel1.setId(new QualifiedId("QuestionnaireResponse/"+questionnaireResponse.getId()));
        return responseModel1;
    }

    @Test
    public void getQuestionnaireResponses_ReturnZeroItem_WhenPagesizeIsZero() throws Exception {
        //Arrange
        QuestionnaireResponseService qrservice = new QuestionnaireResponseService(fhirClient, fhirMapper, null, triageEvaluator,accessValidator);


        QuestionnaireResponse response = new QuestionnaireResponse();
        FhirLookupResult questionnaireResponseResult = FhirLookupResult.fromResources(response);
        Patient patient = new Patient();
        FhirLookupResult patientResult = FhirLookupResult.fromResource(patient);
        FhirLookupResult lookupResult = questionnaireResponseResult.merge(patientResult);

        Mockito.when(fhirClient.lookupQuestionnaireResponses(null, new ArrayList<>())).thenReturn(lookupResult);



        PageDetails pageDetails = new PageDetails(1,0);
        List<QuestionnaireResponseModel> result = qrservice.getQuestionnaireResponses(null, new ArrayList<>(), pageDetails);


        assertEquals(0, result.size());
    }

    @Test
    public void getQuestionnaireResponses_ReturnZeroItem_WhenOnSecondPage() throws Exception {
        //Arrange
        QuestionnaireResponseService qrservice = new QuestionnaireResponseService(fhirClient, fhirMapper, null,triageEvaluator, accessValidator);


        QuestionnaireResponse response = new QuestionnaireResponse();
        FhirLookupResult questionnaireResponseResult = FhirLookupResult.fromResources(response);
        Patient patient = new Patient();
        FhirLookupResult patientResult = FhirLookupResult.fromResource(patient);
        FhirLookupResult lookupResult = questionnaireResponseResult.merge(patientResult);
        Mockito.when(fhirClient.lookupQuestionnaireResponses(null, new ArrayList<>())).thenReturn(lookupResult);

        PageDetails pageDetails = new PageDetails(2,1);
        List<QuestionnaireResponseModel> result = qrservice.getQuestionnaireResponses(null,  new ArrayList<>(),pageDetails);

        assertEquals(0, result.size());
    }

    @Test
    public void getQuestionnaireResponses_accessViolation_throwsException() throws Exception {
        // Arrange
        String carePlanId = CAREPLAN_ID_1;

        QuestionnaireResponse response = buildQuestionnaireResponse(QUESTIONNAIRE_RESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID);
        Patient patient = buildPatient(PATIENT_ID);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(response, patient);
        Mockito.when(fhirClient.lookupQuestionnaireResponses(carePlanId, new ArrayList<>())).thenReturn(lookupResult);

        Mockito.doThrow(AccessValidationException.class).when(accessValidator).validateAccess(patient);

        // Act

        // Assert
        assertThrows(AccessValidationException.class, () -> subject.getQuestionnaireResponses(carePlanId,  new ArrayList<>(),new PageDetails(1,10)));
    }

    @Test
    public void getQuestionnaireResponseById_responsePresent_returnsQuestionnaireResponse() throws Exception {
        // Arrange
        String questionnaireResponseId = QUESTIONNAIRE_RESPONSE_ID_1;

        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRE_RESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID);
        Patient patient = buildPatient(PATIENT_ID);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(questionnaireResponse, patient);
        Mockito.when(fhirClient.lookupQuestionnaireResponseById(questionnaireResponseId)).thenReturn(lookupResult);
        Mockito.when(fhirClient.lookupVersionsOfQuestionnaireById(List.of())).thenReturn(List.of());

        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        Mockito.when(fhirMapper.mapQuestionnaireResponse(questionnaireResponse, lookupResult, List.of())).thenReturn(questionnaireResponseModel);

        // Act
        Optional<QuestionnaireResponseModel> result = subject.getQuestionnaireResponseById(new QualifiedId(questionnaireResponseId));

        // Assert
        assertEquals(questionnaireResponseModel, result.get());
    }

    @Test
    public void getQuestionnaireResponseById_responseForDifferentPatient_throwsException() throws Exception {
        // Arrange
        String questionnaireResponseId = QUESTIONNAIRE_RESPONSE_ID_1;

        QuestionnaireResponse response = buildQuestionnaireResponse(QUESTIONNAIRE_RESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID);
        Patient patient = buildPatient(PATIENT_ID);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(response, patient);
        Mockito.when(fhirClient.lookupQuestionnaireResponseById(questionnaireResponseId)).thenReturn(lookupResult);

        Mockito.doThrow(AccessValidationException.class).when(accessValidator).validateAccess(patient);

        // Act

        // Assert
        assertThrows(AccessValidationException.class, () -> subject.getQuestionnaireResponseById(new QualifiedId(questionnaireResponseId)));
    }

    @Test
    public void getQuestionnaireResponseById_responseMissing_returnsEmpty() throws Exception {
        // Arrange
        String questionnaireResponseId = QUESTIONNAIRE_RESPONSE_ID_1;

        Mockito.when(fhirClient.lookupQuestionnaireResponseById(questionnaireResponseId)).thenReturn(FhirLookupResult.fromResources());

        // Act
        Optional<QuestionnaireResponseModel> result = subject.getQuestionnaireResponseById(new QualifiedId(questionnaireResponseId));

        // Assert
        assertFalse(result.isPresent());
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
    public void submitQuestionnaireResponse_wrongCarePlanId_throwsException() {
        // Arrange
        var carePlanId = new QualifiedId(CAREPLAN_ID_1);
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel(carePlanId);
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_2);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = buildCarePlanModel(CAREPLAN_ID_2, PATIENT_ID);
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        // Act

        // Assert
        assertThrows(ServiceException.class, () -> subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr));
    }

    @Test
    public void submitQuestionnaireResponse_success_returnsGeneratedId() throws Exception {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = buildCarePlanModel(CAREPLAN_ID_1, PATIENT_ID);
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        Mockito.when(fhirMapper.mapQuestionnaireResponseModel(questionnaireResponseModel)).thenReturn(questionnaireResponse);

        Mockito.when(fhirMapper.mapCarePlanModel(carePlanModel)).thenReturn(carePlan);

        Mockito.when(dateProvider.now()).thenReturn(POINT_IN_TIME);

        Mockito.when(fhirClient.saveQuestionnaireResponse(questionnaireResponse, carePlan)).thenReturn(QUESTIONNAIRE_RESPONSE_ID_1);

        // Act
        String result = subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

        // Assert
        assertEquals(QUESTIONNAIRE_RESPONSE_ID_1, result);
    }

    @Test
    public void submitQuestionnaireResponse_success_populatesAttributes() throws Exception {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = buildCarePlanModel(CAREPLAN_ID_1, PATIENT_ID);
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        Mockito.when(dateProvider.now()).thenReturn(POINT_IN_TIME);

        // Act
        String result = subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

        // Assert
        assertEquals(new QualifiedId(PATIENT_ID), questionnaireResponseModel.getAuthorId());
        assertEquals(new QualifiedId(PATIENT_ID), questionnaireResponseModel.getSourceId());
        assertEquals(POINT_IN_TIME, questionnaireResponseModel.getAnswered());
        assertEquals(ExaminationStatus.NOT_EXAMINED, questionnaireResponseModel.getExaminationStatus());
        assertEquals(new QualifiedId(PATIENT_ID), questionnaireResponseModel.getPatient().getId());
    }

    @Test
    public void submitQuestionnaireResponse_success_computesTriagingCategory() throws Exception {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = buildCarePlanModel(CAREPLAN_ID_1, PATIENT_ID);
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        Mockito.when(dateProvider.now()).thenReturn(POINT_IN_TIME);

        Mockito.when(triageEvaluator.determineTriagingCategory(
                questionnaireResponseModel.getQuestionAnswerPairs().stream().map(qa -> qa.getAnswer()).collect(Collectors.toList()),
                carePlanModel.getQuestionnaires().get(0).getThresholds())
                ).thenReturn(TriagingCategory.YELLOW);

        // Act
        String result = subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

        // Assert
        assertEquals(TriagingCategory.YELLOW, questionnaireResponseModel.getTriagingCategory());
    }

    @Test
    public void submitQuestionnaireResponse_submittedInTime_refreshesSatisfiedUntil() throws Exception {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = buildCarePlanModel(CAREPLAN_ID_1, PATIENT_ID, POINT_IN_TIME.minus(Duration.ofHours(1)));
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        Mockito.when(dateProvider.now()).thenReturn(POINT_IN_TIME);

        // Act
        String result = subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

        // Assert
        // Verify that the satisfiedUntil-timestamp is advanced twice from the current point in time: to the next deadline (which was met), and to the next one after that.
        var questionnaireWrapper = carePlanModel.getQuestionnaires().get(0);
        assertEquals(Instant.parse("2021-11-26T03:00:00Z"), questionnaireWrapper.getSatisfiedUntil());
    }

    @Test
    public void submitQuestionnaireResponse_submissionOverdue_dont_refreshesSatisfiedUntil() throws Exception {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = buildCarePlanModel(CAREPLAN_ID_1, PATIENT_ID, POINT_IN_TIME.plus(Duration.ofHours(1)));
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        Mockito.when(dateProvider.now()).thenReturn(POINT_IN_TIME);

        // Act
        String result = subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

        // Assert
        // Verify that the satisfiedUntil-timestamp is advanced twice from the current point in time: to the next deadline (which was met), and to the next one after that.
        var questionnaireWrapper = carePlanModel.getQuestionnaires().get(0);
        assertEquals(carePlanModel.getSatisfiedUntil(), questionnaireWrapper.getSatisfiedUntil());
    }

    @Test
    public void submitQuestionnaireResponse_refreshesSatisfiedUntilForCarePlan() throws Exception {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        String cpr = "0101010101";

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = buildCarePlanModel(CAREPLAN_ID_1, PATIENT_ID, List.of(
                buildQuestionnaireWrapperModel(QUESTIONNAIRE_ID_1, POINT_IN_TIME.minus(Period.ofDays(3))),
                buildQuestionnaireWrapperModel(QUESTIONNAIRE_ID_2, POINT_IN_TIME.plus(Period.ofDays(1)))));
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        Mockito.when(dateProvider.now()).thenReturn(POINT_IN_TIME);

        // Act
        String result = subject.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

        // Assert
        // Verify that the satisfiedUntil-timestamp on the careplan is now the minimum among the questionnaires.
        // We submitted an answer to QUESTIONNAIRE_ID_1, so the value from QUESTIONNAIRE_ID_2 should be the new one.
        assertEquals(Instant.parse("2021-11-23T03:00:00Z"), carePlanModel.getSatisfiedUntil());
    }

    private CarePlan buildCarePlan(String carePlanId) {
        CarePlan carePlan = new CarePlan();

        carePlan.setId(carePlanId);

        return carePlan;
    }

    private CarePlanModel buildCarePlanModel(String carePlanId, String patientId) {
        Instant satisfiedUntil = POINT_IN_TIME.plus(Period.ofDays(3)).plus(Duration.ofHours(4));
        return buildCarePlanModel(carePlanId, patientId, satisfiedUntil);
    }

    private CarePlanModel buildCarePlanModel(String carePlanId, String patientId, Instant satisfiedUntil) {
        FrequencyModel frequencyModel = new FrequencyModel();
        frequencyModel.setWeekdays(List.of(Weekday.FRI));
        frequencyModel.setTimeOfDay(LocalTime.parse("04:00"));

        var questionnaireWrapper = buildQuestionnaireWrapperModel(QUESTIONNAIRE_ID_1, frequencyModel, satisfiedUntil);

        return buildCarePlanModel(carePlanId, patientId, List.of(questionnaireWrapper));
    }

    private CarePlanModel buildCarePlanModel(String carePlanId, String patientId, List<QuestionnaireWrapperModel> questionnaires) {
        CarePlanModel carePlanModel = new CarePlanModel();

        carePlanModel.setId(new QualifiedId(carePlanId));

        carePlanModel.setPatient(new PatientModel());
        carePlanModel.getPatient().setId(new QualifiedId(patientId));

        carePlanModel.setQuestionnaires(questionnaires);

        return carePlanModel;
    }

    private Patient buildPatient(String patientId) {
        Patient patient = new Patient();

        patient.setId(patientId);

        return patient;
    }

    private QuestionnaireWrapperModel buildQuestionnaireWrapperModel(String questionnaireId, Instant satisfiedUntil) {
        FrequencyModel frequencyModel = new FrequencyModel();
        frequencyModel.setWeekdays(List.of(Weekday.TUE));
        frequencyModel.setTimeOfDay(LocalTime.parse("04:00"));

        return buildQuestionnaireWrapperModel(questionnaireId, frequencyModel, satisfiedUntil);
    }

    private QuestionnaireWrapperModel buildQuestionnaireWrapperModel(String questionnaireId, FrequencyModel frequencyModel, Instant satisfiedUntil) {
        var model = new QuestionnaireWrapperModel();

        model.setQuestionnaire(new QuestionnaireModel());
        model.getQuestionnaire().setId(new QualifiedId(questionnaireId));

        model.setFrequency(frequencyModel);
        model.setSatisfiedUntil(satisfiedUntil);
        model.setThresholds(List.of());

        return model;
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
        return buildQuestionnaireResponseModel(new QualifiedId(CAREPLAN_ID_1));
    }

    private QuestionnaireResponseModel buildQuestionnaireResponseModel(QualifiedId carePlanId) {
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();

        questionnaireResponseModel.setQuestionAnswerPairs(List.of(new QuestionAnswerPairModel()));
        questionnaireResponseModel.setQuestionnaireId(new QualifiedId(QUESTIONNAIRE_ID_1));
        questionnaireResponseModel.setCarePlanId(carePlanId);

        return questionnaireResponseModel;
    }
}