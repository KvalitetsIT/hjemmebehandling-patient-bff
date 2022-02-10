package dk.kvalitetsit.hjemmebehandling.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiException;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.QuestionnaireResponseApi;
import org.openapitools.client.model.AnswerDto;
import org.openapitools.client.model.CallToActionDTO;
import org.openapitools.client.model.QuestionAnswerPairDto;
import org.openapitools.client.model.QuestionnaireResponseDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionnaireResponseIntegrationTest extends AbstractIntegrationTest {
    private QuestionnaireResponseApi subject;

    @BeforeEach
    public void setup() {
        subject = new QuestionnaireResponseApi();

        subject.getApiClient().setBasePath(enhanceBasePath(subject.getApiClient().getBasePath()));
    }

    @Test
    public void getQuestionnaireResponsesByCarePlanId_success() throws Exception {
        // Arrange
        String carePlanId = "careplan-infektionsmedicinsk-1";

        // Act
        ApiResponse<List<QuestionnaireResponseDto>> response = subject.getQuestionnaireResponsesByCarePlanIdWithHttpInfo(carePlanId, 1, 10);

        // Assert
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void connectionTest() throws Exception {
        // Arrange
        String questionnaireResponseId = "questionnaireresponse-infektionsmedicinsk-1";

        // Act
        try{
            ApiResponse<QuestionnaireResponseDto> response = subject.getQuestionnaireResponseByIdWithHttpInfo(questionnaireResponseId);
        } catch (ApiException exception){
            fail(exception.getCode() + ": " + exception.getMessage() + " => " + exception.getResponseBody(),exception);
        }

    }

    @Test
    public void getQuestionnaireResponseById_success() throws Exception {
        // Arrange
        String questionnaireResponseId = "questionnaireresponse-infektionsmedicinsk-1";

        // Act
        ApiResponse<QuestionnaireResponseDto> response = subject.getQuestionnaireResponseByIdWithHttpInfo(questionnaireResponseId);

        // Assert
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void submitQuestionnaireResponse_success() throws Exception {
        // Arrange
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();
        questionnaireResponseDto.setCarePlanId("CarePlan/careplan-infektionsmedicinsk-1");
        questionnaireResponseDto.setQuestionnaireId("Questionnaire/questionnaire-infektionsmedicinsk-1");

        questionnaireResponseDto.setQuestionAnswerPairs(new ArrayList<>());

        List<AnswerDto> answers = new ArrayList<>();

        var answer1 = new AnswerDto();
        answer1.setLinkId("temperature");
        answer1.setAnswerType(AnswerDto.AnswerTypeEnum.QUANTITY);
        answer1.setValue("37.5");
        answers.add(answer1);

        var answer2 = new AnswerDto();
        answer2.setLinkId("crp");
        answer2.setAnswerType(AnswerDto.AnswerTypeEnum.QUANTITY);
        answer2.setValue("52");
        answers.add(answer2);

        var answer3 = new AnswerDto();
        answer3.setLinkId("antibiotika");
        answer3.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer3.setValue("true");
        answers.add(answer3);

        var answer4 = new AnswerDto();
        answer4.setLinkId("helbredstilstand");
        answer4.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer4.setValue("false");
        answers.add(answer4);

        var answer5 = new AnswerDto();
        answer5.setLinkId("nye_symptomer");
        answer5.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer5.setValue("true");
        answers.add(answer5);

        var answer6 = new AnswerDto();
        answer6.setLinkId("udslæt");
        answer6.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer6.setValue("true");
        answers.add(answer6);

        var answer7 = new AnswerDto();
        answer7.setLinkId("udslæt_2");
        answer7.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer7.setValue("false");
        answers.add(answer7);

        for(var answer : answers) {
            var questionAnswerPair = new QuestionAnswerPairDto();
            questionAnswerPair.setAnswer(answer);
            questionnaireResponseDto.addQuestionAnswerPairsItem(questionAnswerPair);
        }

        // Act
        ApiResponse<CallToActionDTO> response = subject.submitQuestionnaireResponseWithHttpInfo(questionnaireResponseDto);

        // Assert
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("location"));
        assertTrue(response.getData().getCallToActions().isEmpty());
    }

    @Test
    public void submitQuestionnaireResponse_return_callToAction_success() throws Exception {
        // Arrange
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();
        questionnaireResponseDto.setCarePlanId("CarePlan/careplan-infektionsmedicinsk-1");
        questionnaireResponseDto.setQuestionnaireId("Questionnaire/questionnaire-infektionsmedicinsk-1");

        questionnaireResponseDto.setQuestionAnswerPairs(new ArrayList<>());

        List<AnswerDto> answers = new ArrayList<>();

        var answer1 = new AnswerDto();
        answer1.setLinkId("temperature");
        answer1.setAnswerType(AnswerDto.AnswerTypeEnum.QUANTITY);
        answer1.setValue("37.5");
        answers.add(answer1);

        var answer2 = new AnswerDto();
        answer2.setLinkId("crp");
        answer2.setAnswerType(AnswerDto.AnswerTypeEnum.QUANTITY);
        answer2.setValue("52");
        answers.add(answer2);

        var answer3 = new AnswerDto();
        answer3.setLinkId("antibiotika");
        answer3.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer3.setValue("true");
        answers.add(answer3);

        var answer4 = new AnswerDto();
        answer4.setLinkId("helbredstilstand");
        answer4.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer4.setValue("false");
        answers.add(answer4);

        var answer5 = new AnswerDto();
        answer5.setLinkId("nye_symptomer");
        answer5.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer5.setValue("true");
        answers.add(answer5);

        var answer6 = new AnswerDto();
        answer6.setLinkId("udslæt");
        answer6.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer6.setValue("true");
        answers.add(answer6);

        var answer7 = new AnswerDto();
        answer7.setLinkId("udslæt_2");
        answer7.setAnswerType(AnswerDto.AnswerTypeEnum.BOOLEAN);
        answer7.setValue("true");
        answers.add(answer7);

        for(var answer : answers) {
            var questionAnswerPair = new QuestionAnswerPairDto();
            questionAnswerPair.setAnswer(answer);
            questionnaireResponseDto.addQuestionAnswerPairsItem(questionAnswerPair);
        }

        // Act
        ApiResponse<CallToActionDTO> response = subject.submitQuestionnaireResponseWithHttpInfo(questionnaireResponseDto);

        // Assert
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("location"));
        assertEquals(1, response.getData().getCallToActions().size());
    }
}
