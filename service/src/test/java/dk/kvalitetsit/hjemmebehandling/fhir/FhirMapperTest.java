package dk.kvalitetsit.hjemmebehandling.fhir;

import dk.kvalitetsit.hjemmebehandling.constants.*;

import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.types.ThresholdType;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FhirMapperTest {
    @InjectMocks
    private FhirMapper subject;

    private static final String CAREPLAN_ID_1 = "CarePlan/careplan-1";
    private static final String ORGANIZATION_ID_1 = "Organization/organization-1";
    private static final String PATIENT_ID_1 = "Patient/patient-1";
    private static final String PLANDEFINITION_ID_1 = "PlanDefinition/plandefinition-1";
    private static final String QUESTIONNAIRE_ID_1 = "Questionnaire/questionnaire-1";
    private static final String QUESTIONNAIRERESPONSE_ID_1 = "QuestionnaireResponse/questionnaireresponse-1";

    private static final Instant POINT_IN_TIME = Instant.parse("2021-11-23T00:00:00.000Z");

    @Test
    public void mapCarePlanModel_mapsSubject() {
        // Arrange
        CarePlanModel carePlanModel = buildCarePlanModel();

        // Act
        CarePlan result = subject.mapCarePlanModel(carePlanModel);

        // Assert
        assertEquals(result.getSubject().getReference(), carePlanModel.getPatient().getId().toString());
        assertEquals(result.getSubject().getReference(), carePlanModel.getPatient().getId().toString());
    }


    @Test
    public void mapPatientModel_mapsSubject() {
        // Arrange
        Patient patient = buildPatient(PATIENT_ID_1, "1234567890");

        // Act
        PatientModel patientModel = subject.mapPatient(patient);

        // Assert

        ContactDetailsModel contactDetails = patientModel.getContactDetails();

        //=== Address
        assertEquals(patient.getAddressFirstRep().getCountry(), contactDetails.getAddress().getCountry());
        assertEquals(patient.getAddressFirstRep().getCity(), contactDetails.getAddress().getCity());
        assertEquals(patient.getAddressFirstRep().getPostalCode(), contactDetails.getAddress().getPostalCode());

        //=== Patient contact information
        var phoneNumbers = patient.getTelecom();
        assertNotEquals(contactDetails.getPhone().getSecondary(), contactDetails.getPhone().getPrimary(),"For testing purposes theese should not be the same");
        assertEquals(phoneNumbers.get(0).getValue(), contactDetails.getPhone().getPrimary());
        assertEquals(phoneNumbers.get(1).getValue(), contactDetails.getPhone().getSecondary());
        assertEquals(patient.getName(),patient.getName());

        //== Primarycontact
        var primaryContactDetails = patientModel.getContacts().get(0).getContactDetails();
        var primaryContactNumbers = patient.getTelecom();
        assertEquals(primaryContactNumbers.get(0).getValue(), primaryContactDetails.getPhone().getPrimary());
        assertEquals(primaryContactNumbers.get(1).getValue(), primaryContactDetails.getPhone().getSecondary());
        assertEquals(patient.getContactFirstRep().getAddress().getCountry(),primaryContactDetails.getAddress().getCountry());
        assertEquals(patient.getContactFirstRep().getAddress().getPostalCode(),primaryContactDetails.getAddress().getPostalCode());
        assertEquals(patient.getContactFirstRep().getAddress().getCity(),primaryContactDetails.getAddress().getCity());
        assertEquals(patient.getContactFirstRep().getRelationshipFirstRep().getText(),patientModel.getContacts().get(0).getAffiliation());
    }





    @Test
    public void mapCarePlan_mapsPeriod() {
        // Arrange
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Organization organization = buildOrganization(ORGANIZATION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan, patient, questionnaire, organization, planDefinition);

        // Act
        CarePlanModel result = subject.mapCarePlan(carePlan, lookupResult);

        // Assert
        assertEquals(result.getStartDate(), Instant.parse("2021-10-28T00:00:00Z"));
        assertEquals(result.getEndDate(), Instant.parse("2021-10-29T00:00:00Z"));
    }

    @Test
    public void mapCarePlan_roundtrip_preservesExtensions() {
        // Arrange
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Organization organization = buildOrganization(ORGANIZATION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan, patient, questionnaire, organization, planDefinition);

        // Act
        CarePlan result = subject.mapCarePlanModel(subject.mapCarePlan(carePlan, lookupResult));

        // Assert
        assertEquals(carePlan.getExtension().size(), result.getExtension().size());
        assertTrue(result.getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.ORGANIZATION)));
        assertTrue(result.getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.CAREPLAN_SATISFIED_UNTIL)));
    }

    @Test
    public void mapCarePlan_roundtrip_preservesActivities() {
        // Arrange
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Organization organization = buildOrganization(ORGANIZATION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan, patient, questionnaire, organization, planDefinition);

        // Act
        CarePlan result = subject.mapCarePlanModel(subject.mapCarePlan(carePlan, lookupResult));

        // Assert
        assertEquals(carePlan.getActivity().size(), result.getActivity().size());
        assertEquals(carePlan.getActivity().get(0).getDetail().getInstantiatesCanonical().get(0).getValue(), result.getActivity().get(0).getDetail().getInstantiatesCanonical().get(0).getValue());
        assertEquals(carePlan.getActivity().get(0).getDetail().getScheduledTiming().getRepeat().getDayOfWeek().get(0).getValue(), result.getActivity().get(0).getDetail().getScheduledTiming().getRepeat().getDayOfWeek().get(0).getValue());

        assertTrue(result.getActivity().get(0).getDetail().getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.ACTIVITY_SATISFIED_UNTIL)));
    }

    @Test
    public void mapCarePlan_includesQuestionnaires_andThresholds() {
        // Arrange
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Organization organization = buildOrganization(ORGANIZATION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan, patient, questionnaire, organization, planDefinition);

        // Act
        CarePlanModel result = subject.mapCarePlan(carePlan, lookupResult);

        // Assert
        assertEquals(1, result.getQuestionnaires().size());

        assertEquals(QUESTIONNAIRE_ID_1, result.getQuestionnaires().get(0).getQuestionnaire().getId().toString());
        assertEquals(1, result.getQuestionnaires().get(0).getThresholds().size());
    }

    @Test
    public void mapCarePlan_includesOrganization_staticHtml_blob() {
        // Arrange
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Organization organization = buildOrganization(ORGANIZATION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan, patient, questionnaire, organization, planDefinition);

        // Act
        CarePlanModel result = subject.mapCarePlan(carePlan, lookupResult);

        // Assert
        assertEquals(1, result.getQuestionnaires().size());

        assertEquals(QUESTIONNAIRE_ID_1, result.getQuestionnaires().get(0).getQuestionnaire().getId().toString());
        assertNotNull(result.getQuestionnaires().get(0).getQuestionnaire().getBlob());
        assertEquals(organization.getExtensionByUrl(Systems.QUESTIONNAIRE_SUMMARY_BLOB).getValue().primitiveValue(), result.getQuestionnaires().get(0).getQuestionnaire().getBlob());
    }

    @Test
    public void mapPlandefinition_includesQuestionnaires_andThresholds() {
        // Arrange
        //CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        //Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Organization organization = buildOrganization(ORGANIZATION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(questionnaire, organization, planDefinition);

        // Act
        PlanDefinitionModel result = subject.mapPlanDefinition(planDefinition, lookupResult);

        // Assert
        assertEquals(1, result.getQuestionnaires().size());

        assertEquals(QUESTIONNAIRE_ID_1, result.getQuestionnaires().get(0).getQuestionnaire().getId().toString());
        assertEquals(1, result.getQuestionnaires().get(0).getThresholds().size());
    }

    @Test
    public void mapOrganization_mapsAttributes() {
        // Arrange
        Organization organization = buildOrganization(ORGANIZATION_ID_1);

        // Act
        OrganizationModel result = subject.mapOrganization(organization);

        // Assert
        assertEquals(ORGANIZATION_ID_1, result.getId().toString());
        assertEquals(organization.getName(), result.getName());
        assertEquals(organization.getTelecomFirstRep().getValue(), result.getContactDetails().getPhone().getPrimary());
        assertNotNull(organization.getTelecomFirstRep().getExtensionByUrl(Systems.PHONE_HOURS));
        assertEquals(organization.getExtensionByUrl(Systems.ORGANISATION_BLOB).getValue().primitiveValue(), result.getBlob());
    }

    @Test
    public void mapQuestionnaireResponseModel_mapsAnswers() {
        // Arrange
        QuestionnaireResponseModel model = buildQuestionnaireResponseModel();

        // Act
        QuestionnaireResponse result = subject.mapQuestionnaireResponseModel(model);

        // Assert
        assertEquals(1, result.getItem().size());
        assertEquals(new IntegerType(2).getValue(), result.getItem().get(0).getAnswer().get(0).getValueIntegerType().getValue());
    }
    @Test
    public void mapQuestion_abbreviation() {
        String abbreviation = "dagsform";
        Questionnaire.QuestionnaireItemComponent question1 = buildQuestionItem("1", Questionnaire.QuestionnaireItemType.BOOLEAN, "Har du det godt?", abbreviation);
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(question1));

        // Act
        QuestionnaireModel result = subject.mapQuestionnaire(questionnaire);

        // Assert
        assertEquals(1, result.getQuestions().size());
        assertEquals(abbreviation, result.getQuestions().get(0).getAbbreviation());

    }
    @Test
    public void mapQuestionnaireResponseModel_mapsExaminationStatus() {
        // Arrange
        QuestionnaireResponseModel model = buildQuestionnaireResponseModel();

        // Act
        QuestionnaireResponse result = subject.mapQuestionnaireResponseModel(model);

        // Assert
        assertTrue(result.getExtension().stream().anyMatch(e ->
                e.getUrl().equals(Systems.EXAMINATION_STATUS) &&
                        e.getValue().toString().equals(new StringType(ExaminationStatus.NOT_EXAMINED.name()).toString())));
    }

    @Test
    public void mapTiming_allValuesAreNull_noErrors(){
        var timingToMap = new Timing();
        var result = subject.mapTiming(timingToMap);
        assertNotNull(result);
    }

    @Test
    public void mapPlandefinition_noCreatedDate_DontThrowError() {
        // Arrange
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1);
        Organization organization = buildOrganization(ORGANIZATION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan, patient, questionnaire, organization, planDefinition);

        // Act
        planDefinition.setDate(null);
        PlanDefinitionModel result = subject.mapPlanDefinition(planDefinition, lookupResult);

        // Assert
        assertEquals(1, result.getQuestionnaires().size());

        assertEquals(QUESTIONNAIRE_ID_1, result.getQuestionnaires().get(0).getQuestionnaire().getId().toString());
        assertEquals(1, result.getQuestionnaires().get(0).getThresholds().size());
    }

    @Test
    public void mapQuestionnaireResponse_canMapAnswers() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID_1, List.of(buildStringItem("hej", "1"), buildQuantityItem(2, "2")));
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1", Questionnaire.QuestionnaireItemType.STRING), buildQuestionItem("2", Questionnaire.QuestionnaireItemType.QUANTITY)));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        // Act
        QuestionnaireResponseModel result = subject.mapQuestionnaireResponse(questionnaireResponse, FhirLookupResult.fromResources(questionnaireResponse, questionnaire, patient, carePlan, planDefinition));

        // Assert
        assertEquals(2, result.getQuestionAnswerPairs().size());
        assertEquals(AnswerType.STRING, result.getQuestionAnswerPairs().get(0).getAnswer().getAnswerType());
        assertEquals(AnswerType.QUANTITY, result.getQuestionAnswerPairs().get(1).getAnswer().getAnswerType());
    }

    @Test
    public void mapQuestionnaireResponse_roundtrip_preservesExtensions() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID_1, List.of(buildStringItem("hej", "1"), buildIntegerItem(2, "2")));
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1", Questionnaire.QuestionnaireItemType.STRING), buildQuestionItem("2", Questionnaire.QuestionnaireItemType.INTEGER)));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(patient, questionnaire, carePlan, planDefinition);

        // Act
        QuestionnaireResponse result = subject.mapQuestionnaireResponseModel(subject.mapQuestionnaireResponse(questionnaireResponse, lookupResult));

        // Assert
        assertEquals(questionnaireResponse.getExtension().size(), result.getExtension().size());
        assertTrue(result.getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.ORGANIZATION)));
        assertTrue(result.getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.EXAMINATION_STATUS)));
        assertTrue(result.getExtension().stream().anyMatch(e -> e.getUrl().equals(Systems.TRIAGING_CATEGORY)));
    }

    @Test
    public void mapQuestionnaireResponse_roundtrip_preservesReferences() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID_1, List.of(buildStringItem("hej", "1"), buildIntegerItem(2, "2")));
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1", Questionnaire.QuestionnaireItemType.STRING), buildQuestionItem("2", Questionnaire.QuestionnaireItemType.INTEGER)));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(patient, questionnaire, carePlan, planDefinition);

        // Act
        QuestionnaireResponse result = subject.mapQuestionnaireResponseModel(subject.mapQuestionnaireResponse(questionnaireResponse, lookupResult));

        // Assert
        assertFalse(questionnaireResponse.getBasedOn().isEmpty());
        assertEquals(questionnaireResponse.getBasedOn().size(), result.getBasedOn().size());
        assertEquals(questionnaireResponse.getBasedOn().get(0).getReference(), result.getBasedOn().get(0).getReference());

        assertEquals(questionnaireResponse.getAuthor().getReference(), result.getAuthor().getReference());

        assertEquals(questionnaireResponse.getSource().getReference(), result.getSource().getReference());
    }

    @Test
    public void mapQuestionnaireResponse_roundtrip_preservesLinks() {
        // Arrange
        var stringItem = buildStringItem("hej", "1");
        var integerItem = buildIntegerItem(2, "2");
        var quantityItem = buildQuantityItem(3.1, "3");

        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID_1, List.of(stringItem, integerItem, quantityItem));
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1", Questionnaire.QuestionnaireItemType.STRING), buildQuestionItem("2", Questionnaire.QuestionnaireItemType.INTEGER), buildQuestionItem("3", Questionnaire.QuestionnaireItemType.QUANTITY)));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(patient, questionnaire, carePlan, planDefinition);

        // Act
        QuestionnaireResponse result = subject.mapQuestionnaireResponseModel(subject.mapQuestionnaireResponse(questionnaireResponse, lookupResult));

        // Assert
        assertEquals(questionnaireResponse.getItem().size(), result.getItem().size());

        assertEquals(stringItem.getLinkId(), result.getItem().get(0).getLinkId());
        assertEquals(stringItem.getAnswerFirstRep().getValueStringType().getValue(), result.getItem().get(0).getAnswerFirstRep().getValueStringType().getValue());

        assertEquals(integerItem.getLinkId(), result.getItem().get(1).getLinkId());
        assertEquals(integerItem.getAnswerFirstRep().getValueIntegerType().getValue(), result.getItem().get(1).getAnswerFirstRep().getValueIntegerType().getValue());

        assertEquals(quantityItem.getLinkId(), result.getItem().get(2).getLinkId());
        assertEquals(quantityItem.getAnswerFirstRep().getValueQuantity().getValue(), result.getItem().get(2).getAnswerFirstRep().getValueQuantity().getValue());
    }

    @Test
    public void mapQuestion_helperText() {
        // Arrange
        var question = buildQuestionItem("1", Questionnaire.QuestionnaireItemType.STRING, "spørgsmål","s");
        var helperText = buildQuestionItem("help", Questionnaire.QuestionnaireItemType.DISPLAY, "hjælpetekst","s");
        question.addItem(helperText);

        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(question));

        // Act
        QuestionnaireModel result = subject.mapQuestionnaire(questionnaire);

        // Assert
        assertEquals(1, result.getQuestions().size());
        assertEquals("spørgsmål", result.getQuestions().get(0).getText());
        assertEquals("hjælpetekst", result.getQuestions().get(0).getHelperText());
    }

    @Test
    public void mapQuestion_no_helperText_returnsNull() {
        // Arrange
        var question = buildQuestionItem("1", Questionnaire.QuestionnaireItemType.STRING, "spørgsmål","s");

        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(question));

        // Act
        QuestionnaireModel result = subject.mapQuestionnaire(questionnaire);

        // Assert
        assertEquals(1, result.getQuestions().size());
        assertEquals("spørgsmål", result.getQuestions().get(0).getText());
        assertNull(result.getQuestions().get(0).getHelperText());
    }

    /**
     * Thresholds is modelled as an extension on PlanDefinition, but was previously modelled on CarePlan.
     * Make sure that Thresholds is defined as extension the right place
     */
    @Test
    public void mapCarePlan_where_PlanDefinition_has_thresholds() {
        PlanDefinition planDefinition = buildPlanDefinition(PLANDEFINITION_ID_1, QUESTIONNAIRE_ID_1);
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1, QUESTIONNAIRE_ID_1, PLANDEFINITION_ID_1);

        // Act

        // Assert
        assertTrue(planDefinition.getAction().stream().anyMatch(a -> a.hasExtension(Systems.THRESHOLD)));
        assertTrue(carePlan.getActivity().stream().noneMatch(a -> a.getDetail().hasExtension(Systems.THRESHOLD)));
    }


    private CarePlan buildCarePlan(String careplanId, String patientId, String questionnaireId, String planDefinitionId) {
        CarePlan carePlan = new CarePlan();

        carePlan.setId(careplanId);
        carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
        carePlan.setSubject(new Reference(patientId));
        carePlan.addInstantiatesCanonical(planDefinitionId);
        carePlan.setPeriod(new Period());
        carePlan.setCreated(Date.from(Instant.parse("2021-10-28T00:00:00Z")));
        carePlan.getPeriod().setStart(Date.from(Instant.parse("2021-10-28T00:00:00Z")));
        carePlan.getPeriod().setEnd(Date.from(Instant.parse("2021-10-29T00:00:00Z")));
        carePlan.addExtension(ExtensionMapper.mapCarePlanSatisfiedUntil(Instant.parse("2021-12-07T10:11:12.124Z")));
        carePlan.addExtension(ExtensionMapper.mapOrganizationId(ORGANIZATION_ID_1));

        var detail = new CarePlan.CarePlanActivityDetailComponent();
        detail.setInstantiatesCanonical(List.of(new CanonicalType(questionnaireId)));
        detail.setScheduled(buildTiming());
        detail.addExtension(ExtensionMapper.mapActivitySatisfiedUntil(POINT_IN_TIME));

        carePlan.addActivity().setDetail(detail);

        return carePlan;
    }

    private CarePlanModel buildCarePlanModel() {
        CarePlanModel carePlanModel = new CarePlanModel();

        carePlanModel.setId(new QualifiedId(CAREPLAN_ID_1));
        carePlanModel.setStatus(CarePlanStatus.ACTIVE);
        carePlanModel.setCreated(Instant.parse("2021-12-07T10:11:12.124Z"));
        carePlanModel.setPatient(buildPatientModel());
        carePlanModel.setQuestionnaires(List.of(buildQuestionnaireWrapperModel()));
        carePlanModel.setPlanDefinitions(List.of(buildPlanDefinitionModel()));
        carePlanModel.setSatisfiedUntil(Instant.parse("2021-12-07T10:11:12.124Z"));

        return carePlanModel;
    }

    private ContactDetailsModel buildContactDetailsModel() {
        ContactDetailsModel contactDetailsModel = new ContactDetailsModel();
        contactDetailsModel.setPhone(new PhoneModel());
        contactDetailsModel.setAddress(new AddressModel());

        contactDetailsModel.getAddress().setStreet("Fiskergade");

        return contactDetailsModel;
    }

    private PrimaryContactModel buildPrimaryContactModel() {
        PrimaryContactModel model = new PrimaryContactModel();

        model.setName("tove");
        model.setAffiliation("tante");

        model.setContactDetails(buildContactDetailsModel());

        return model;
    }


    private FrequencyModel buildFrequencyModel() {
        FrequencyModel frequencyModel = new FrequencyModel();

        frequencyModel.setWeekdays(List.of(Weekday.FRI));
        frequencyModel.setTimeOfDay(LocalTime.parse("05:00"));

        return frequencyModel;
    }

    private Organization buildOrganization(String organizationId) {
        Organization organization = new Organization();

        organization.setId(organizationId);
        organization.setName("Infektionsmedicinsk Afdeling");

        organization.addAddress()
                .setLine(List.of(new StringType("Fiskergade 66")))
                .setPostalCode("8000")
                .setCity("Aarhus")
                .setCountry("Danmark");

        organization.addTelecom()
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue("22334455")
                .setRank(1);

        String blob = "<h1>Infektionsmedicinsk</h1><h3>Åbningstider:<h3><p>08:00 - 16:00</p>";
        organization.addExtension(ExtensionMapper.mapBlob(blob));

        PhoneHourModel phoneHourModel = new PhoneHourModel();
        phoneHourModel.setWeekdays(List.of(Weekday.MON, Weekday.FRI));
        phoneHourModel.setFrom(LocalTime.parse("07:00"));
        phoneHourModel.setTo(LocalTime.parse("11:00"));
        organization.getTelecomFirstRep().addExtension(ExtensionMapper.mapPhoneHours(phoneHourModel));

        String questionnaireSummaryBlob = "<Typography>Hvis der er noget, du er i tvivl om, eller du har praktiske problemer, kan du <b>altid</b> kontakte Infektionsklinikken på tlf. 78 45 28 64 på hverdage kl. 8.00 – 15.00. Uden for dette tidspunkt kan du kontakte Sengeafsnittet på tlf. 24 77 78 80.</Typography>";
        organization.addExtension(ExtensionMapper.mapQuestionnaireSummaryBlob(questionnaireSummaryBlob));

        return organization;
    }

    private Patient buildPatient(String patientId, String cpr) {
        Patient patient = new Patient();

        patient.setId(patientId);

        var identifier = new Identifier();
        identifier.setSystem(Systems.CPR);
        identifier.setValue(cpr);
        patient.setIdentifier(List.of(identifier));

        var name = new HumanName();
        name.setFamily("Dent");
        name.addGiven("Arthur");
        patient.addName(name);

        var address = new Address();
        address.setCity("Aarhus");
        address.setCountry("MockedLand");
        address.setPostalCode("MockedPostal");
        patient.addAddress(address);

        var primaryTelecom = new ContactPoint();
        primaryTelecom.setSystem(ContactPoint.ContactPointSystem.PHONE);
        primaryTelecom.setValue("12345678");
        primaryTelecom.setRank(1);
        patient.addTelecom(primaryTelecom);

        var secondaryTelecom = new ContactPoint();
        secondaryTelecom.setSystem(ContactPoint.ContactPointSystem.PHONE);
        secondaryTelecom.setValue("87654321");
        secondaryTelecom.setRank(2);
        patient.addTelecom(secondaryTelecom);

        var contactComponent = new Patient.ContactComponent();
        var contactName = new HumanName();
        contactName.setText("Slartibartfast");
        contactComponent.setName(contactName);

        var concept = new CodeableConcept();
        concept.setText("Ven");
        contactComponent.setRelationship(List.of(concept));
        contactComponent.addTelecom(primaryTelecom);
        contactComponent.addTelecom(secondaryTelecom);
        patient.addContact(contactComponent);

        return patient;
    }

    private PatientModel buildPatientModel() {
        PatientModel patientModel = new PatientModel();

        patientModel.setId(new QualifiedId(PATIENT_ID_1));
        patientModel.setCpr("0101010101");
        patientModel.setContactDetails(buildContactDetailsModel());
        patientModel.setContacts(List.of(buildPrimaryContactModel()));

        return patientModel;
    }

    private PlanDefinition buildPlanDefinition(String planDefinitionId, String questionnaireId) {
        PlanDefinition planDefinition = new PlanDefinition();

        planDefinition.setId(planDefinitionId);
        planDefinition.addExtension(ExtensionMapper.mapOrganizationId(ORGANIZATION_ID_1));

        PlanDefinition.PlanDefinitionActionComponent action = new PlanDefinition.PlanDefinitionActionComponent();
        action.setDefinition(new CanonicalType(questionnaireId));
        action.getTimingTiming().getRepeat().addDayOfWeek(Timing.DayOfWeek.MON).addTimeOfDay("11:00");

        ThresholdModel threshold = new ThresholdModel();
        threshold.setQuestionnaireItemLinkId("1");
        threshold.setType(ThresholdType.NORMAL);
        threshold.setValueBoolean(true);
        action.addExtension(ExtensionMapper.mapThreshold(threshold));

        planDefinition.addAction(action);

        return planDefinition;
    }

    private PlanDefinitionModel buildPlanDefinitionModel() {
        PlanDefinitionModel planDefinitionModel = new PlanDefinitionModel();

        planDefinitionModel.setId(new QualifiedId(PLANDEFINITION_ID_1));
        planDefinitionModel.setQuestionnaires(List.of(buildQuestionnaireWrapperModel()));

        return planDefinitionModel;
    }

    private Questionnaire buildQuestionnaire(String questionnaireId) {
        return buildQuestionnaire(questionnaireId, List.of());
    }

    private Questionnaire buildQuestionnaire(String questionnaireId, List<Questionnaire.QuestionnaireItemComponent> questionItems) {
        Questionnaire questionnaire = new Questionnaire();

        questionnaire.setId(questionnaireId);
        questionnaire.setStatus(Enumerations.PublicationStatus.ACTIVE);
        questionnaire.getItem().addAll(questionItems);
        questionnaire.addExtension(ExtensionMapper.mapOrganizationId(ORGANIZATION_ID_1));

        return questionnaire;
    }

    private QuestionnaireModel buildQuestionnaireModel() {
        QuestionnaireModel questionnaireModel = new QuestionnaireModel();

        questionnaireModel.setId(new QualifiedId(QUESTIONNAIRE_ID_1));
        questionnaireModel.setQuestions(List.of(buildQuestionModel()));

        return questionnaireModel;
    }

    private QuestionModel buildQuestionModel() {
        return buildQuestionModel(QuestionType.BOOLEAN, "Hvordan har du det?", "dagsform");
    }

    private QuestionnaireWrapperModel buildQuestionnaireWrapperModel() {
        QuestionnaireWrapperModel questionnaireWrapperModel = new QuestionnaireWrapperModel();

        questionnaireWrapperModel.setQuestionnaire(buildQuestionnaireModel());
        questionnaireWrapperModel.setFrequency(buildFrequencyModel());
        questionnaireWrapperModel.setSatisfiedUntil(Instant.parse("2021-12-08T10:11:12.124Z"));

        return questionnaireWrapperModel;
    }

    private QuestionnaireResponseModel buildQuestionnaireResponseModel() {
        QuestionnaireResponseModel model = new QuestionnaireResponseModel();

        model.setId(new QualifiedId(QUESTIONNAIRERESPONSE_ID_1));
        model.setQuestionnaireId(new QualifiedId(QUESTIONNAIRE_ID_1));
        model.setCarePlanId(new QualifiedId(CAREPLAN_ID_1));
        model.setAuthorId(new QualifiedId(PATIENT_ID_1));
        model.setSourceId(new QualifiedId(PATIENT_ID_1));

        model.setAnswered(Instant.parse("2021-11-03T00:00:00Z"));

        model.setQuestionAnswerPairs(new ArrayList<>());

        QuestionModel question = new QuestionModel();
        AnswerModel answer = new AnswerModel();
        answer.setAnswerType(AnswerType.INTEGER);
        answer.setValue("2");

        model.getQuestionAnswerPairs().add(new QuestionAnswerPairModel(question, answer));

        model.setExaminationStatus(ExaminationStatus.NOT_EXAMINED);
        model.setTriagingCategory(TriagingCategory.GREEN);

        PatientModel patientModel = new PatientModel();
        patientModel.setId(new QualifiedId(PATIENT_ID_1));
        model.setPatient(patientModel);

        return model;
    }

    private QuestionnaireResponse buildQuestionnaireResponse(String questionnaireResponseId, String questionnaireId, String patiientId, List<QuestionnaireResponse.QuestionnaireResponseItemComponent> answerItems) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

        questionnaireResponse.setId(questionnaireResponseId);
        questionnaireResponse.setQuestionnaire(questionnaireId);
        questionnaireResponse.setBasedOn(List.of(new Reference(CAREPLAN_ID_1)));
        questionnaireResponse.setAuthor(new Reference(PATIENT_ID_1));
        questionnaireResponse.setSource(new Reference(PATIENT_ID_1));
        questionnaireResponse.setSubject(new Reference(patiientId));
        questionnaireResponse.getItem().addAll(answerItems);
        questionnaireResponse.setAuthored(Date.from(Instant.parse("2021-10-28T00:00:00Z")));
        questionnaireResponse.getExtension().add(new Extension(Systems.EXAMINATION_STATUS, new StringType(ExaminationStatus.EXAMINED.toString())));
        questionnaireResponse.getMeta().setLastUpdated(Date.from(Instant.parse("2021-10-29T00:00:00Z")));
        questionnaireResponse.getExtension().add(new Extension(Systems.TRIAGING_CATEGORY, new StringType(TriagingCategory.GREEN.toString())));
        questionnaireResponse.addExtension(ExtensionMapper.mapOrganizationId(ORGANIZATION_ID_1));

        return questionnaireResponse;
    }

    private Questionnaire.QuestionnaireItemComponent buildQuestionItem(String linkId, Questionnaire.QuestionnaireItemType itemType) {
        return buildQuestionItem(linkId, itemType, null, null);
    }
    private Questionnaire.QuestionnaireItemComponent buildQuestionItem(String linkId, Questionnaire.QuestionnaireItemType itemType, String text, String abbreviation) {
        var item = new Questionnaire.QuestionnaireItemComponent();

        item.setType(itemType);
        item.setLinkId(linkId);
        item.setText(text);
        if (abbreviation != null) {
            item.addExtension(ExtensionMapper.mapQuestionAbbreviation(abbreviation));
        }

        return item;
    }

    private QuestionModel buildQuestionModel(QuestionType type, String text, String abbreviation) {
        QuestionModel questionModel = new QuestionModel();
        questionModel.setText(text);
        questionModel.setAbbreviation(abbreviation);
        questionModel.setQuestionType(type);

        return questionModel;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent buildStringItem(String value, String linkId) {
        return buildItem(new StringType(value), linkId);
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent buildIntegerItem(int value, String linkId) {
        return buildItem(new IntegerType(value), linkId);
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent buildQuantityItem(double value, String linkId) {
        return buildItem(new Quantity(value), linkId);
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent buildItem(Type value, String linkId) {
        var item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();

        var answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
        answer.setValue(value);
        item.getAnswer().add(answer);

        item.setLinkId(linkId);

        return item;
    }

    private Timing buildTiming() {
        Timing timing = new Timing();

        var repeat = new Timing.TimingRepeatComponent();
        repeat.setDayOfWeek(List.of(new Enumeration<>(new Timing.DayOfWeekEnumFactory(), Timing.DayOfWeek.FRI)));
        repeat.setTimeOfDay(List.of(new TimeType("04:00")));

        timing.setRepeat(repeat);

        return timing;
    }
}