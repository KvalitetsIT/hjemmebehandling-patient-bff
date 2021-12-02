package dk.kvalitetsit.hjemmebehandling.api;

import dk.kvalitetsit.hjemmebehandling.constants.AnswerType;
import dk.kvalitetsit.hjemmebehandling.model.*;
import dk.kvalitetsit.hjemmebehandling.model.AnswerModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionModel;
import dk.kvalitetsit.hjemmebehandling.types.Weekday;
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
    public void mapQuestionnaireResponseModel_success() {
        // Arrange
        QuestionnaireResponseModel questionnaireResponseModel = buildQuestionnaireResponseModel();

        // Act
        QuestionnaireResponseDto result = subject.mapQuestionnaireResponseModel(questionnaireResponseModel);

        // Assert
        assertEquals(questionnaireResponseModel.getId().toString(), result.getId());
    }

    private QuestionnaireResponseModel buildQuestionnaireResponseModel() {
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();

        questionnaireResponseModel.setId(new QualifiedId(QUESTIONNAIRERESPONSE_ID_1));
        questionnaireResponseModel.setQuestionnaireId(new QualifiedId(QUESTIONNAIRE_ID_1));
        questionnaireResponseModel.setQuestionAnswerPairs(List.of(buildQuestionAnswerPairModel()));
        questionnaireResponseModel.setPatient(buildPatientModel());

        return questionnaireResponseModel;
    }

    private PatientModel buildPatientModel() {
        PatientModel patientModel = new PatientModel();

        patientModel.setCpr("0101010101");
        patientModel.setPatientContactDetails(buildContactDetailsModel());
        patientModel.setPrimaryRelativeContactDetails(buildContactDetailsModel());
        patientModel.setAdditionalRelativeContactDetails(List.of(buildContactDetailsModel()));

        return patientModel;
    }

    private ContactDetailsModel buildContactDetailsModel() {
        ContactDetailsModel contactDetailsModel = new ContactDetailsModel();

        contactDetailsModel.setStreet("Fiskergade");

        return contactDetailsModel;
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

    private AnswerModel buildAnswerModel() {
        AnswerModel answerModel = new AnswerModel();

        answerModel.setAnswerType(AnswerType.STRING);
        answerModel.setValue("foo");

        return answerModel;
    }
}