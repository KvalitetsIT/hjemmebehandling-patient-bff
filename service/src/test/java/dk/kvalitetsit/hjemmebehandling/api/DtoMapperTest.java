package dk.kvalitetsit.hjemmebehandling.api;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.constants.CarePlanStatus;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionModel;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DtoMapperTest {
    private DtoMapper subject = new DtoMapper();

    private static final String CAREPLAN_ID_1 = "CarePlan/careplan-1";
    private static final String ORGANIZATION_ID_1 = "Organization/organization-1";
    private static final String PATIENT_ID_1 = "Patient/patient-1";
    private static final String PLANDEFINITION_ID_1 = "PlanDefinition/plandefinition-1";
    private static final String QUESTIONNAIRE_ID_1 = "Questionnaire/questionnaire-1";
    private static final String QUESTIONNAIRERESPONSE_ID_1 = "QuestionnaireResponse/questionnaireresponse-1";

    @Test
    public void mapCarePlanDto_success() {
        // Arrange
        CarePlanDto carePlanDto = buildCarePlanDto();

        // Act
        CarePlanModel result = subject.mapCarePlanDto(carePlanDto);

        // Assert
        assertEquals(carePlanDto.getId(), result.getId().toString());
    }

    @Test
    public void mapFrequencyModel_allValuesAreNull_noErrors(){
        var toMap = new FrequencyModel();
        var result = subject.mapFrequencyModel(toMap);
        assertNotNull(result);

    }

    @Test
    public void mapCarePlanModel_success() {
        // Arrange
        CarePlanModel carePlanModel = buildCarePlanModel();

        // Act
        CarePlanDto result = subject.mapCarePlanModel(carePlanModel);

        // Assert
        assertEquals(carePlanModel.getId().toString(), result.getId());
    }

    @Test
    public void mapOrganizationDto_success() {
        // Arrange
        OrganizationDto organizationDto = buildOrganizationDto();

        // Act
        OrganizationModel result = subject.mapOrganizationDto(organizationDto);

        // Assert
        assertEquals(organizationDto.getContactDetails().getAddress().getCity(), result.getContactDetails().getAddress().getCity());
        assertEquals(organizationDto.getContactDetails().getAddress().getPostalCode(), result.getContactDetails().getAddress().getPostalCode());
    }

    @Test
    public void mapOrganizationModel_success() {
        // Arrange
        OrganizationModel organizationModel = buildOrganizationModel();

        // Act
        OrganizationDto result = subject.mapOrganizationModel(organizationModel);

        // Assert
        assertEquals(organizationModel.getContactDetails().getAddress().getCity(), result.getContactDetails().getAddress().getCity());
        assertEquals(organizationModel.getContactDetails().getAddress().getPostalCode(), result.getContactDetails().getAddress().getPostalCode());
    }

    @Test
    public void mapPatientDto_success() {
        // Arrange
        PatientDto patientDto = buildPatientDto();

        // Act
        PatientModel result = subject.mapPatientDto(patientDto);

        // Assert
        assertEquals(patientDto.getCpr(), result.getCpr());
        assertEquals(patientDto.getPatientContactDetails().getAddress().getStreet(), result.getPatientContactDetails().getAddress().getStreet());
        assertEquals(patientDto.getPatientContactDetails().getAddress().getCountry(), result.getPatientContactDetails().getAddress().getCountry());
        assertEquals(patientDto.getPatientContactDetails().getAddress().getPostalCode(), result.getPatientContactDetails().getAddress().getPostalCode());
        assertEquals(patientDto.getPatientContactDetails().getPhone().getPrimary(), result.getPatientContactDetails().getPhone().getPrimary());
        assertEquals(patientDto.getPatientContactDetails().getPhone().getSecondary(), result.getPatientContactDetails().getPhone().getSecondary());
    }

    @Test
    public void mapPatientModel_success() {
        // Arrange
        PatientModel patientModel = buildPatientModel();

        // Act
        PatientDto result = subject.mapPatientModel(patientModel);

        // Assert
        assertEquals(patientModel.getCpr(), result.getCpr());
        assertEquals(patientModel.getPatientContactDetails().getAddress().getStreet(), result.getPatientContactDetails().getAddress().getStreet());
        assertEquals(patientModel.getPatientContactDetails().getAddress().getCountry(), result.getPatientContactDetails().getAddress().getCountry());
        assertEquals(patientModel.getPatientContactDetails().getAddress().getPostalCode(), result.getPatientContactDetails().getAddress().getPostalCode());
        assertEquals(patientModel.getPatientContactDetails().getPhone().getPrimary(), result.getPatientContactDetails().getPhone().getPrimary());
        assertEquals(patientModel.getPatientContactDetails().getPhone().getSecondary(), result.getPatientContactDetails().getPhone().getSecondary());

    }



    @Test
    public void mapQuestionnaireResponseModel_success() {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();

        // Act
        QuestionnaireResponseDto result = subject.mapQuestionnaireResponseModel(questionnaireResponseModel);

        // Assert
        assertEquals(questionnaireResponseModel.getId().toString(), result.getId());
    }

    private AnswerModel buildAnswerModel() {
        AnswerModel answerModel = new AnswerModel();

        answerModel.setAnswerType(AnswerType.STRING);
        answerModel.setValue("foo");

        return answerModel;
    }

    private CarePlanDto buildCarePlanDto() {
        CarePlanDto carePlanDto = new CarePlanDto();

        carePlanDto.setId(CAREPLAN_ID_1);
        carePlanDto.setStatus("ACTIVE");
        carePlanDto.setPatientDto(buildPatientDto());
        carePlanDto.setQuestionnaires(List.of(buildQuestionnaireWrapperDto()));
        carePlanDto.setPlanDefinitions(List.of(buildPlanDefinitionDto()));

        return carePlanDto;
    }

    private CarePlanModel buildCarePlanModel() {
        CarePlanModel carePlanModel = new CarePlanModel();

        carePlanModel.setId(new QualifiedId(CAREPLAN_ID_1));
        carePlanModel.setStatus(CarePlanStatus.ACTIVE);
        carePlanModel.setPatient(buildPatientModel());
        carePlanModel.setQuestionnaires(List.of(buildQuestionnaireWrapperModel()));
        carePlanModel.setPlanDefinitions(List.of(buildPlanDefinitionModel()));

        return carePlanModel;
    }
    private PrimaryContactDto buildPrimaryContactDto() {
        PrimaryContactDto dto = new PrimaryContactDto();
        dto.setContactDetails(buildContactDetailsDto());
        return dto;
    }

    private PrimaryContactModel buildPrimaryContactModel() {
        PrimaryContactModel model = new PrimaryContactModel();
        model.setContactDetails(buildContactDetailsModel());
        return model;
    }


    private ContactDetailsDto buildContactDetailsDto() {
        ContactDetailsDto contactDetailsDto = new ContactDetailsDto();
        contactDetailsDto.setAddress(new AddressDto());
        contactDetailsDto.setPhone(new PhoneDto());
        contactDetailsDto.getAddress().setStreet("Fiskergade");

        return contactDetailsDto;
    }

    private ContactDetailsModel buildContactDetailsModel() {
        ContactDetailsModel contactDetailsModel = new ContactDetailsModel();
        contactDetailsModel.setAddress(new AddressModel());
        contactDetailsModel.setPhone(new PhoneModel());
        contactDetailsModel.getAddress().setStreet("Fiskergade");

        return contactDetailsModel;
    }

    private FrequencyDto buildFrequencyDto() {
        FrequencyDto frequencyDto = new FrequencyDto();

        frequencyDto.setWeekdays(List.of(Weekday.FRI));
        frequencyDto.setTimeOfDay("04:00");

        return frequencyDto;
    }

    private FrequencyModel buildFrequencyModel() {
        FrequencyModel frequencyModel = new FrequencyModel();

        frequencyModel.setWeekdays(List.of(Weekday.FRI));
        frequencyModel.setTimeOfDay(LocalTime.parse("04:00"));

        return frequencyModel;
    }

    private OrganizationDto buildOrganizationDto() {
        OrganizationDto organizationDto = new OrganizationDto();

        organizationDto.setName("Infektionsmedicinsk afdeling");
        organizationDto.setContactDetails(new ContactDetailsDto());
        organizationDto.getContactDetails().setAddress(new AddressDto());
        organizationDto.getContactDetails().setPhone(new PhoneDto());

        organizationDto.getContactDetails().getAddress().setStreet("Fiskergade 66");
        organizationDto.getContactDetails().getAddress().setPostalCode("8000");
        organizationDto.getContactDetails().getAddress().setCity("Aarhus");
        organizationDto.getContactDetails().getAddress().setCountry("Danmark");
        organizationDto.getContactDetails().getPhone().setPrimary("22334455");

        PhoneHourDto phoneHourDto = new PhoneHourDto();
        phoneHourDto.setWeekdays(List.of(Weekday.MON, Weekday.FRI));
        phoneHourDto.setFrom("07:00");
        phoneHourDto.setTo("11:00");
        organizationDto.setPhoneHours(List.of(phoneHourDto));

        return organizationDto;
    }

    private OrganizationModel buildOrganizationModel() {
        OrganizationModel organizationModel = new OrganizationModel();

        organizationModel.setId(new QualifiedId(ORGANIZATION_ID_1));
        organizationModel.setName("Infektionsmedicinsk afdeling");
        organizationModel.setContactDetails(new ContactDetailsModel());
        organizationModel.getContactDetails().setAddress(new AddressModel());
        organizationModel.getContactDetails().getAddress().setStreet("Fiskergade 66");
        organizationModel.getContactDetails().getAddress().setPostalCode("8000");
        organizationModel.getContactDetails().getAddress().setCity("Aarhus");
        organizationModel.getContactDetails().getAddress().setCountry("Danmark");

        organizationModel.getContactDetails().setPhone(new PhoneModel());
        organizationModel.getContactDetails().getPhone().setPrimary("22334455");

        PhoneHourModel phoneHourModel = new PhoneHourModel();
        phoneHourModel.setWeekdays(List.of(Weekday.MON, Weekday.FRI));
        phoneHourModel.setFrom(LocalTime.parse("07:00"));
        phoneHourModel.setTo(LocalTime.parse("11:00"));
        organizationModel.setPhoneHours(List.of(phoneHourModel));

        return organizationModel;
    }

    private PatientDto buildPatientDto() {
        PatientDto patientDto = new PatientDto();

        patientDto.setCpr("0101010101");
        patientDto.setPatientContactDetails(buildContactDetailsDto());
        patientDto.setPrimaryContacts(List.of(buildPrimaryContactDto()));

        return patientDto;
    }

    private PatientModel buildPatientModel() {
        PatientModel patientModel = new PatientModel();

        patientModel.setCpr("0101010101");
        patientModel.setPatientContactDetails(buildContactDetailsModel());
        patientModel.setPrimaryContacts(List.of(buildPrimaryContactModel()));


        return patientModel;
    }

    private PlanDefinitionDto buildPlanDefinitionDto() {
        PlanDefinitionDto planDefinitionDto = new PlanDefinitionDto();

        planDefinitionDto.setId(PLANDEFINITION_ID_1);
        planDefinitionDto.setQuestionnaires(List.of(buildQuestionnaireWrapperDto()));

        return planDefinitionDto;
    }

    private PlanDefinitionModel buildPlanDefinitionModel() {
        PlanDefinitionModel planDefinitionModel = new PlanDefinitionModel();

        planDefinitionModel.setId(new QualifiedId(PLANDEFINITION_ID_1));
        planDefinitionModel.setQuestionnaires(List.of(buildQuestionnaireWrapperModel()));

        return planDefinitionModel;
    }

    private QuestionDto buildQuestionDto() {
        QuestionDto questionDto = new QuestionDto();

        questionDto.setText("Hvordan har du det?");

        return questionDto;
    }

    private QuestionModel buildQuestionModel() {
        QuestionModel questionModel = new QuestionModel();

        questionModel.setText("Hvordan har du det?");

        return questionModel;
    }

    private QuestionAnswerPairModel buildQuestionAnswerPairModel() {
        QuestionAnswerPairModel questionAnswerPairModel = new QuestionAnswerPairModel(buildQuestionModel(), buildAnswerModel());

        return questionAnswerPairModel;
    }

    private QuestionnaireDto buildQuestionnaireDto() {
        QuestionnaireDto questionnaireDto = new QuestionnaireDto();

        questionnaireDto.setId("questionnaire-1");
        questionnaireDto.setQuestions(List.of(buildQuestionDto()));

        return questionnaireDto;
    }

    private QuestionnaireModel buildQuestionnaireModel() {
        QuestionnaireModel questionnaireModel = new QuestionnaireModel();

        questionnaireModel.setId(new QualifiedId(QUESTIONNAIRE_ID_1));
        questionnaireModel.setQuestions(List.of(buildQuestionModel()));

        return questionnaireModel;
    }

    private QuestionnaireResponseModel buildQuestionnaireResponseModel() {
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();

        questionnaireResponseModel.setId(new QualifiedId(QUESTIONNAIRERESPONSE_ID_1));
        questionnaireResponseModel.setQuestionnaireId(new QualifiedId(QUESTIONNAIRE_ID_1));
        questionnaireResponseModel.setCarePlanId(new QualifiedId(CAREPLAN_ID_1));
        questionnaireResponseModel.setQuestionAnswerPairs(List.of(buildQuestionAnswerPairModel()));
        questionnaireResponseModel.setPatient(buildPatientModel());

        return questionnaireResponseModel;
    }

    private QuestionnaireWrapperDto buildQuestionnaireWrapperDto() {
        QuestionnaireWrapperDto questionnaireWrapperDto = new QuestionnaireWrapperDto();

        questionnaireWrapperDto.setQuestionnaire(buildQuestionnaireDto());
        questionnaireWrapperDto.setFrequency(buildFrequencyDto());

        return questionnaireWrapperDto;
    }

    private QuestionnaireWrapperModel buildQuestionnaireWrapperModel() {
        QuestionnaireWrapperModel questionnaireWrapperModel = new QuestionnaireWrapperModel();

        questionnaireWrapperModel.setQuestionnaire(buildQuestionnaireModel());
        questionnaireWrapperModel.setFrequency(buildFrequencyModel());

        return questionnaireWrapperModel;
    }
}