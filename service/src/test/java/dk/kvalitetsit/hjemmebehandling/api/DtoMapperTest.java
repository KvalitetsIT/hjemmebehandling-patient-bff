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
    public void mapCarePlanDto_success() {
        // Arrange
        CarePlanDto carePlanDto = buildCarePlanDto();

        // Act
        CarePlanModel result = subject.mapCarePlanDto(carePlanDto);

        // Assert
        assertEquals(carePlanDto.getId(), result.getId().toString());
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
    public void mapPatientDto_success() {
        // Arrange
        PatientDto patientDto = buildPatientDto();

        // Act
        PatientModel result = subject.mapPatientDto(patientDto);

        // Assert
        assertEquals(patientDto.getCpr(), result.getCpr());
    }

    @Test
    public void mapPatientModel_success() {
        // Arrange
        PatientModel patientModel = buildPatientModel();

        // Act
        PatientDto result = subject.mapPatientModel(patientModel);

        // Assert
        assertEquals(patientModel.getCpr(), result.getCpr());
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
        carePlanDto.setPatientDto(buildPatientDto());
        carePlanDto.setQuestionnaires(List.of(buildQuestionnaireWrapperDto()));
        carePlanDto.setPlanDefinitions(List.of(buildPlanDefinitionDto()));

        return carePlanDto;
    }

    private CarePlanModel buildCarePlanModel() {
        CarePlanModel carePlanModel = new CarePlanModel();

        carePlanModel.setId(new QualifiedId(CAREPLAN_ID_1));
        carePlanModel.setPatient(buildPatientModel());
        carePlanModel.setQuestionnaires(List.of(buildQuestionnaireWrapperModel()));
        carePlanModel.setPlanDefinitions(List.of(buildPlanDefinitionModel()));

        return carePlanModel;
    }

    private ContactDetailsDto buildContactDetailsDto() {
        ContactDetailsDto contactDetailsDto = new ContactDetailsDto();

        contactDetailsDto.setStreet("Fiskergade");

        return contactDetailsDto;
    }

    private ContactDetailsModel buildContactDetailsModel() {
        ContactDetailsModel contactDetailsModel = new ContactDetailsModel();

        contactDetailsModel.setStreet("Fiskergade");

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

    private PatientDto buildPatientDto() {
        PatientDto patientDto = new PatientDto();

        patientDto.setCpr("0101010101");
        patientDto.setPatientContactDetails(buildContactDetailsDto());
        patientDto.setPrimaryRelativeContactDetails(buildContactDetailsDto());
        patientDto.setAdditionalRelativeContactDetails(List.of(buildContactDetailsDto()));

        return patientDto;
    }

    private PatientModel buildPatientModel() {
        PatientModel patientModel = new PatientModel();

        patientModel.setCpr("0101010101");
        patientModel.setPatientContactDetails(buildContactDetailsModel());
        patientModel.setPrimaryRelativeContactDetails(buildContactDetailsModel());
        patientModel.setAdditionalRelativeContactDetails(List.of(buildContactDetailsModel()));

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