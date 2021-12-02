package dk.kvalitetsit.hjemmebehandling.fhir;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.constants.TriagingCategory;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionModel;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
import dk.kvalitetsit.hjemmebehandling.util.DateProvider;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    public void mapQuestionnaireResponse_canMapAnswers() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID_1, List.of(buildStringItem("hej", "1"), buildIntegerItem(2, "2")));
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1"), buildQuestionItem("2")));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");

        // Act
        QuestionnaireResponseModel result = subject.mapQuestionnaireResponse(questionnaireResponse, FhirLookupResult.fromResources(questionnaireResponse, questionnaire, patient));

        // Assert
        assertEquals(2, result.getQuestionAnswerPairs().size());
        assertEquals(AnswerType.STRING, result.getQuestionAnswerPairs().get(0).getAnswer().getAnswerType());
        assertEquals(AnswerType.INTEGER, result.getQuestionAnswerPairs().get(1).getAnswer().getAnswerType());
    }

    @Test
    public void mapQuestionnaireResponse_roundtrip_preservesExtensions() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID_1, List.of(buildStringItem("hej", "1"), buildIntegerItem(2, "2")));
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1"), buildQuestionItem("2")));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(patient, questionnaire);

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
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1"), buildQuestionItem("2")));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(patient, questionnaire);

        // Act
        QuestionnaireResponse result = subject.mapQuestionnaireResponseModel(subject.mapQuestionnaireResponse(questionnaireResponse, lookupResult));

        // Assert
        assertTrue(!questionnaireResponse.getBasedOn().isEmpty());
        assertEquals(questionnaireResponse.getBasedOn().size(), result.getBasedOn().size());
        assertEquals(questionnaireResponse.getBasedOn().get(0).getReference(), result.getBasedOn().get(0).getReference());

        assertEquals(questionnaireResponse.getAuthor().getReference(), result.getAuthor().getReference());

        assertEquals(questionnaireResponse.getSource().getReference(), result.getSource().getReference());
    }

    @Test
    public void mapQuestionnaireResponse_roundtrip_preservesLinks() {
        // Arrange
        QuestionnaireResponse questionnaireResponse = buildQuestionnaireResponse(QUESTIONNAIRERESPONSE_ID_1, QUESTIONNAIRE_ID_1, PATIENT_ID_1, List.of(buildStringItem("hej", "1"), buildIntegerItem(2, "2")));
        Questionnaire questionnaire = buildQuestionnaire(QUESTIONNAIRE_ID_1, List.of(buildQuestionItem("1"), buildQuestionItem("2")));
        Patient patient = buildPatient(PATIENT_ID_1, "0101010101");

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(patient, questionnaire);

        // Act
        QuestionnaireResponse result = subject.mapQuestionnaireResponseModel(subject.mapQuestionnaireResponse(questionnaireResponse, lookupResult));

        // Assert
        assertEquals(questionnaireResponse.getItem().size(), result.getItem().size());
        assertTrue(result.getItem().stream().anyMatch(item -> item.getLinkId().equals("1")));
        assertTrue(result.getItem().stream().anyMatch(item -> item.getLinkId().equals("2")));
    }

    private Patient buildPatient(String patientId, String cpr) {
        Patient patient = new Patient();

        patient.setId(patientId);

        var identifier = new Identifier();
        identifier.setSystem(Systems.CPR);
        identifier.setValue(cpr);
        patient.setIdentifier(List.of(identifier));

        return patient;
    }

    private Questionnaire buildQuestionnaire(String questionnaireId, List<Questionnaire.QuestionnaireItemComponent> questionItems) {
        Questionnaire questionnaire = new Questionnaire();

        questionnaire.setId(questionnaireId);
        questionnaire.setStatus(Enumerations.PublicationStatus.ACTIVE);
        questionnaire.getItem().addAll(questionItems);
        questionnaire.addExtension(ExtensionMapper.mapOrganizationId(ORGANIZATION_ID_1));

        return questionnaire;
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
        questionnaireResponse.getExtension().add(new Extension(Systems.TRIAGING_CATEGORY, new StringType(TriagingCategory.GREEN.toString())));
        questionnaireResponse.addExtension(ExtensionMapper.mapOrganizationId(ORGANIZATION_ID_1));

        return questionnaireResponse;
    }

    private Questionnaire.QuestionnaireItemComponent buildQuestionItem(String linkId) {
        var item = new Questionnaire.QuestionnaireItemComponent();

        item.setType(Questionnaire.QuestionnaireItemType.INTEGER);
        item.setLinkId(linkId);

        return item;
    }

    private QuestionModel buildQuestionModel() {
        QuestionModel questionModel = new QuestionModel();

        questionModel.setText("Hvordan har du det?");

        return questionModel;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent buildStringItem(String value, String linkId) {
        return buildItem(new StringType(value), linkId);
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent buildIntegerItem(int value, String linkId) {
        return buildItem(new IntegerType(value), linkId);
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent buildItem(Type value, String linkId) {
        var item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();

        var answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
        answer.setValue(value);
        item.getAnswer().add(answer);

        item.setLinkId(linkId);

        return item;
    }
}