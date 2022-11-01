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
         List<String> questionnaireIds = List.of("questionnaire-1");

         // Act
         ApiResponse<List<QuestionnaireResponseDto>> response = subject.getQuestionnaireResponsesByCarePlanIdWithHttpInfo(carePlanId, questionnaireIds, 1,1);

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

        List<AnswerDto> answers = List.of(
            buildAnswerDto("urn:uuid:63dc8443-8012-4c1d-ac40-623c4921869c", AnswerDto.AnswerTypeEnum.QUANTITY, "37.5"), // morgen temperatur
            buildAnswerDto("urn:uuid:fbfe8c5c-e441-47ec-a475-597ec55d9261", AnswerDto.AnswerTypeEnum.QUANTITY, "52"),   // crp
            buildAnswerDto("urn:uuid:dc22ed52-d1c1-4bc7-bc21-35022c51b0f9", AnswerDto.AnswerTypeEnum.BOOLEAN, "true"),  // antibiotika
            buildAnswerDto("urn:uuid:c551a88a-f73e-4cd8-976f-7b53f85526cd", AnswerDto.AnswerTypeEnum.BOOLEAN, "false"), // helbredstilstand
            buildAnswerDto("urn:uuid:132792dc-5dc6-4e3a-8064-da324cd3526f", AnswerDto.AnswerTypeEnum.BOOLEAN, "true"),  // nye symptomer
            buildAnswerDto("urn:uuid:0ffc36b3-a8b6-48ed-a483-0b9df7a64da2", AnswerDto.AnswerTypeEnum.BOOLEAN,"true"),   // udslæt
            buildAnswerDto("urn:uuid:707d1946-51c0-4fd7-b579-3dd3ec432531", AnswerDto.AnswerTypeEnum.BOOLEAN, "true"),  // udslæt forværring
            buildAnswerDto("urn:uuid:68135704-3996-4b1d-85a7-93ab6bc64a9e", AnswerDto.AnswerTypeEnum.BOOLEAN, "true"),  // antibiotika problemer
            buildAnswerDto("urn:uuid:c187eabf-51d1-4e6e-a07a-9ef9437f4ac7", AnswerDto.AnswerTypeEnum.BOOLEAN, "true"),  // kateter problemer
            buildAnswerDto("urn:uuid:8472c8ea-48f0-4d66-825c-21b120dae6f8", AnswerDto.AnswerTypeEnum.BOOLEAN, "true"),  // tryg ved behandling
            buildAnswerDto("urn:uuid:6b1cabe3-9b68-4851-b6a3-34fdcb9d96bf", AnswerDto.AnswerTypeEnum.BOOLEAN, "true")   // fortsæt behandling
        );


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

    private AnswerDto buildAnswerDto(String linkId, AnswerDto.AnswerTypeEnum type, String value) {
        AnswerDto answerDto = new AnswerDto();

        answerDto.setLinkId(linkId);
        answerDto.setAnswerType(type);
        answerDto.setValue(value);

        return answerDto;
    }

}
