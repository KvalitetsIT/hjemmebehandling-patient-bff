package dk.kvalitetsit.hjemmebehandling.api;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.constants.CarePlanStatus;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import org.junit.jupiter.api.Test;
import org.openapitools.model.*;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DtoMapperTest {
    private static final String CAREPLAN_ID_1 = "CarePlan/careplan-1";
    private static final String ORGANIZATION_ID_1 = "Organization/organization-1";
    private static final String PATIENT_ID_1 = "Patient/patient-1";
    private static final String PLANDEFINITION_ID_1 = "PlanDefinition/plandefinition-1";
    private static final String QUESTIONNAIRE_ID_1 = "Questionnaire/questionnaire-1";
    private static final String QUESTIONNAIRERESPONSE_ID_1 = "QuestionnaireResponse/questionnaireresponse-1";
    private DtoMapper subject = new DtoMapper();

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
    public void mapFrequencyModel_allValuesAreNull_noErrors() {
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
        assertEquals(organizationDto.getContactDetails().get().getAddress().get().getCity().get(), result.getContactDetails().getAddress().getCity());
        assertEquals(organizationDto.getContactDetails().get().getAddress().get().getPostalCode().get(), result.getContactDetails().getAddress().getPostalCode());
    }

    @Test
    public void mapOrganizationModel_success() {
        // Arrange
        OrganizationModel organizationModel = buildOrganizationModel();

        // Act
        OrganizationDto result = subject.mapOrganizationModel(organizationModel);

        // Assert
        assertEquals(organizationModel.getContactDetails().getAddress().getCity(), result.getContactDetails().get().getAddress().get().getCity().get());
        assertEquals(organizationModel.getContactDetails().getAddress().getPostalCode(), result.getContactDetails().get().getAddress().get().getPostalCode().get());
    }

    @Test
    public void mapPatientDto_success() {
        // Arrange
        PatientDto patientDto = buildPatientDto();

        // Act
        PatientModel result = subject.mapPatientDto(patientDto);

        // Assert
        assertEquals(patientDto.getCpr().get(), result.getCpr());
        assertEquals(patientDto.getContactsDetails().get().getAddress().get().getStreet().get(), result.getContactDetails().getAddress().getStreet());
        assertEquals(patientDto.getContactsDetails().get().getAddress().get().getCountry().get(), result.getContactDetails().getAddress().getCountry());
        assertEquals(patientDto.getContactsDetails().get().getAddress().get().getPostalCode().get(), result.getContactDetails().getAddress().getPostalCode());
        assertEquals(patientDto.getContactsDetails().get().getPhone().get().getPrimary().get(), result.getContactDetails().getPhone().getPrimary());
        assertEquals(patientDto.getContactsDetails().get().getPhone().get().getSecondary().get(), result.getContactDetails().getPhone().getSecondary());
    }

    @Test
    public void mapPatientModel_success() {
        // Arrange
        PatientModel patientModel = buildPatientModel();

        // Act
        PatientDto result = subject.mapPatientModel(patientModel);

        // Assert
        assertEquals(patientModel.getCpr(), result.getCpr().get());
        assertEquals(patientModel.getContactDetails().getAddress().getStreet(), result.getContactsDetails().get().getAddress().get().getStreet().get());
        assertEquals(patientModel.getContactDetails().getAddress().getCountry(), result.getContactsDetails().get().getAddress().get().getCountry().get());
        assertEquals(patientModel.getContactDetails().getAddress().getPostalCode(), result.getContactsDetails().get().getAddress().get().getPostalCode().get());
        assertEquals(patientModel.getContactDetails().getPhone().getPrimary(), result.getContactsDetails().get().getPhone().get().getPrimary().get());
        assertEquals(patientModel.getContactDetails().getPhone().getSecondary(), result.getContactsDetails().get().getPhone().get().getSecondary().get());
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
        return new CarePlanDto()
                .patientDto(buildPatientDto())
                .questionnaires(List.of(buildQuestionnaireWrapperDto()))
                .planDefinitions(List.of(buildPlanDefinitionDto()))
                .status("ACTIVE")
                .id(CAREPLAN_ID_1);
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
        return new PrimaryContactDto().contactDetails(buildContactDetailsDto());
    }

    private PrimaryContactModel buildPrimaryContactModel() {
        PrimaryContactModel model = new PrimaryContactModel();
        model.setContactDetails(buildContactDetailsModel());
        return model;
    }


    private ContactDetailsDto buildContactDetailsDto() {
        return new ContactDetailsDto()
                .phone(new PhoneDto()
                        .primary("88888888")
                        .secondary("77777777")
                )
                .address(new AddressDto()
                        .street("Fiskergade")
                        .country("denmark")
                        .city("aarhus")
                        .postalCode("8000")
                );
    }

    private ContactDetailsModel buildContactDetailsModel() {
        ContactDetailsModel contactDetailsModel = new ContactDetailsModel();
        contactDetailsModel.setAddress(new AddressModel());
        contactDetailsModel.setPhone(new PhoneModel());

        var phone = contactDetailsModel.getPhone();

        phone.setPrimary("88888888");
        phone.setSecondary("77777777");

        var address = contactDetailsModel.getAddress();
        address.setStreet("Fiskergade");
        address.setCountry("Danmark");
        address.setCity("Århus");
        address.setPostalCode("8000");

        return contactDetailsModel;
    }

    private FrequencyDto buildFrequencyDto() {
        return new FrequencyDto()
                .weekdays(List.of(WeekDayDto.FRI))
                .timeOfDay("04:00");
    }

    private FrequencyModel buildFrequencyModel() {
        FrequencyModel frequencyModel = new FrequencyModel();

        frequencyModel.setWeekdays(List.of(Weekday.FRI));
        frequencyModel.setTimeOfDay(LocalTime.parse("04:00"));

        return frequencyModel;
    }

    private OrganizationDto buildOrganizationDto() {
        return new OrganizationDto()
                .name("Infektionsmedicinsk afdeling")
                .contactDetails(new ContactDetailsDto()
                        .phone(new PhoneDto().primary("22334455"))
                        .address(new AddressDto()
                                .street("Fiskergade 66")
                                .postalCode("8000")
                                .city("Aarhus")
                                .country("Danmark")
                        ))
                .phoneHours(List.of(new PhoneHourDto()
                        .weekdays(List.of(WeekDayDto.MON, WeekDayDto.FRI))
                        .from("07:00")
                        .to("11:00")));
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
        PatientDto patientDto = new PatientDto()
                .cpr("0101010101")
                .contactsDetails(buildContactDetailsDto());
        patientDto.setPrimaryContacts(List.of(buildPrimaryContactDto()));

        return patientDto;
    }

    private PatientModel buildPatientModel() {
        PatientModel patientModel = new PatientModel();
        patientModel.setCpr("0101010101");
        patientModel.setContactDetails(buildContactDetailsModel());
        patientModel.setContacts(List.of(buildPrimaryContactModel()));
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
        return new QuestionDto().text("Hvordan har du det?");
    }

    private QuestionModel buildQuestionModel() {
        QuestionModel questionModel = new QuestionModel();
        questionModel.setText("Hvordan har du det?");
        return questionModel;
    }

    private QuestionAnswerPairModel buildQuestionAnswerPairModel() {
        return new QuestionAnswerPairModel(buildQuestionModel(), buildAnswerModel());
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
        return new QuestionnaireWrapperDto()
                .questionnaire(buildQuestionnaireDto())
                .frequency(buildFrequencyDto());
    }

    private QuestionnaireWrapperModel buildQuestionnaireWrapperModel() {
        QuestionnaireWrapperModel questionnaireWrapperModel = new QuestionnaireWrapperModel();
        questionnaireWrapperModel.setQuestionnaire(buildQuestionnaireModel());
        questionnaireWrapperModel.setFrequency(buildFrequencyModel());
        return questionnaireWrapperModel;
    }
}