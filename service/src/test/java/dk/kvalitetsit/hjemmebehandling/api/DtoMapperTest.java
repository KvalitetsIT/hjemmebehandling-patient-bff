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
    private final DtoMapper subject = new DtoMapper();

    @Test
    public void mapCarePlanDto_success() {
        CarePlanDto carePlanDto = buildCarePlanDto();
        CarePlanModel result = subject.mapCarePlanDto(carePlanDto);
        assertEquals(carePlanDto.getId().get(), result.getId().toString());
    }

    @Test
    public void mapFrequencyModel_allValuesAreNull_noErrors() {
        var toMap = new FrequencyModel();
        var result = subject.mapFrequencyModel(toMap);
        assertNotNull(result);

    }

    @Test
    public void mapCarePlanModel_success() {
        CarePlanModel carePlanModel = buildCarePlanModel();
        CarePlanDto result = subject.mapCarePlanModel(carePlanModel);
        assertEquals(carePlanModel.getId().toString(), result.getId().get());
    }

    @Test
    public void mapOrganizationDto_success() {
        OrganizationDto organizationDto = buildOrganizationDto();
        OrganizationModel result = subject.mapOrganizationDto(organizationDto);
        assertEquals(organizationDto.getContactDetails().get().getAddress().get().getCity().get(), result.getContactDetails().getAddress().getCity());
        assertEquals(organizationDto.getContactDetails().get().getAddress().get().getPostalCode().get(), result.getContactDetails().getAddress().getPostalCode());
    }

    @Test
    public void mapOrganizationModel_success() {
        OrganizationModel organizationModel = buildOrganizationModel();
        OrganizationDto result = subject.mapOrganizationModel(organizationModel);
        assertEquals(organizationModel.getContactDetails().getAddress().getCity(), result.getContactDetails().get().getAddress().get().getCity().get());
        assertEquals(organizationModel.getContactDetails().getAddress().getPostalCode(), result.getContactDetails().get().getAddress().get().getPostalCode().get());
    }

    @Test
    public void mapPatientDto_success() {
        PatientDto patientDto = buildPatientDto();

        PatientModel result = subject.mapPatientDto(patientDto);
        assertEquals(patientDto.getCpr().get(), result.getCpr());

        var contactDetailsDto = patientDto.getContactsDetails().get();
        var addressDto = contactDetailsDto.getAddress().get();
        var phoneDto = contactDetailsDto.getPhone().get();

        assertEquals(addressDto.getStreet().get(), result.getContactDetails().getAddress().getStreet());
        assertEquals(addressDto.getCountry().get(), result.getContactDetails().getAddress().getCountry());
        assertEquals(addressDto.getPostalCode().get(), result.getContactDetails().getAddress().getPostalCode());
        assertEquals(phoneDto.getPrimary().get(), result.getContactDetails().getPhone().getPrimary());
        assertEquals(phoneDto.getSecondary().get(), result.getContactDetails().getPhone().getSecondary());
    }

    @Test
    public void mapPatientModel_success() {
        PatientModel patientModel = buildPatientModel();
        PatientDto result = subject.mapPatientModel(patientModel);

        assertEquals(patientModel.getCpr(), result.getCpr().get());

        var contactDetailsDto = result.getContactsDetails().get();
        var addressDto = contactDetailsDto.getAddress().get();
        var phoneDto = contactDetailsDto.getPhone().get();

        var contactDetailsModel = patientModel.getContactDetails();
        var addressModel = contactDetailsModel.getAddress();
        var phoneModel = contactDetailsModel.getPhone();

        assertEquals(addressModel.getStreet(), addressDto.getStreet().get());
        assertEquals(addressModel.getCountry(), addressDto.getCountry().get());
        assertEquals(addressModel.getPostalCode(), addressDto.getPostalCode().get());
        assertEquals(phoneModel.getPrimary(), phoneDto.getPrimary().get());
        assertEquals(phoneModel.getSecondary(), phoneDto.getSecondary().get());
    }


    @Test
    public void mapQuestionnaireResponseModel_success() {
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();
        QuestionnaireResponseDto result = subject.mapQuestionnaireResponseModel(questionnaireResponseModel);
        assertEquals(questionnaireResponseModel.getId().toString(), result.getId().get());
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
        return new PatientDto()
                .cpr("0101010101")
                .contactsDetails(buildContactDetailsDto())
                .primaryContacts(List.of(buildPrimaryContactDto()));
    }

    private PatientModel buildPatientModel() {
        PatientModel patientModel = new PatientModel();
        patientModel.setCpr("0101010101");
        patientModel.setContactDetails(buildContactDetailsModel());
        patientModel.setContacts(List.of(buildPrimaryContactModel()));
        return patientModel;
    }

    private PlanDefinitionDto buildPlanDefinitionDto() {
        return new PlanDefinitionDto()
                .id(PLANDEFINITION_ID_1)
                .questionnaires(List.of(buildQuestionnaireWrapperDto()));
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
        return new QuestionnaireDto()
                .id("questionnaire-1")
                .questions(List.of(buildQuestionDto()));
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