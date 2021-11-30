package dk.kvalitetsit.hjemmebehandling.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.CarePlanApi;
import org.openapitools.client.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CarePlanIntegrationTest extends AbstractIntegrationTest {
    private CarePlanApi subject;

    @BeforeEach
    public void setup() {
        subject = new CarePlanApi();

        subject.getApiClient().setBasePath(enhanceBasePath(subject.getApiClient().getBasePath()));
    }

    @Test
    public void createCarePlan_success() throws Exception {
        // Arrange
        CarePlanDto carePlanDto = new CarePlanDto();

        carePlanDto.setPatientDto(new PatientDto());
        carePlanDto.getPatientDto().setCpr("0606060606");

        QuestionnaireDto questionnaireDto = new QuestionnaireDto();
        questionnaireDto.setId("Questionnaire/questionnaire-1");

        FrequencyDto frequencyDto = new FrequencyDto();
        frequencyDto.setWeekdays(List.of(FrequencyDto.WeekdaysEnum.TUE, FrequencyDto.WeekdaysEnum.FRI));
        frequencyDto.setTimeOfDay("04:00");

        QuestionnaireWrapperDto wrapper = new QuestionnaireWrapperDto();
        wrapper.setQuestionnaire(questionnaireDto);
        wrapper.setFrequency(frequencyDto);

        carePlanDto.setQuestionnaires(List.of(wrapper));

        CreateCarePlanRequest request = new CreateCarePlanRequest()
                .carePlan(carePlanDto);

        // Act
        ApiResponse<Void> response = subject.createCarePlanWithHttpInfo(request);

        // Assert
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("location"));
    }

    @Test
    public void getCarePlan_success() throws Exception {
        // Arrange
        String carePlanId = "careplan-1";

        // Act
        ApiResponse<CarePlanDto> response = subject.getCarePlanByIdWithHttpInfo(carePlanId);

        // Assert
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void getCarePlansByCpr_success() throws Exception {
        // Arrange
        String cpr = "0101010101";
        int pageNumber = 1;
        int pageSize = 10;

        // Act
        ApiResponse<List<CarePlanDto>> response = subject.searchCarePlansWithHttpInfo(cpr, null, pageNumber, pageSize);

        // Assert
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void getCarePlansWithUnsatisfiedSchedules_success() throws Exception {
        // Arrange
        boolean onlyUnsatisfiedSchedules = true;
        int pageNumber = 1;
        int pageSize = 10;

        // Act
        ApiResponse<List<CarePlanDto>> response = subject.searchCarePlansWithHttpInfo(null, onlyUnsatisfiedSchedules, pageNumber, pageSize);

        // Assert
        assertEquals(200, response.getStatusCode());
    }

    @Test
    @Disabled
    public void patchCarePlan_success() throws Exception {
        // Arrange
        String id = "careplan-2";
        PartialUpdateCareplanRequest request = new PartialUpdateCareplanRequest();
        request.addQuestionnaireIdsItem("Questionnaire/questionnaire-1");
        request.addQuestionnaireIdsItem("Questionnaire/questionnaire-2");

        FrequencyDto frequencyDto1 = new FrequencyDto();
        frequencyDto1.setWeekdays(List.of(FrequencyDto.WeekdaysEnum.TUE));
        frequencyDto1.setTimeOfDay("04:00");
        request.putQuestionnaireFrequenciesItem("Questionnaire/questionnaire-1", frequencyDto1);

        FrequencyDto frequencyDto2 = new FrequencyDto();
        frequencyDto2.setWeekdays(List.of(FrequencyDto.WeekdaysEnum.WED));
        frequencyDto2.setTimeOfDay("05:00");
        request.putQuestionnaireFrequenciesItem("Questionnaire/questionnaire-2", frequencyDto2);

        // Act
        ApiResponse<Void> response = subject.patchCarePlanWithHttpInfo(id, request);

        // Assert
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void resolveAlarm_success() throws Exception {
        // Arrange
        String id = "careplan-2";

        // Act
        ApiResponse<Void> response = subject.resolveAlarmWithHttpInfo(id);

        // Assert
        assertEquals(200, response.getStatusCode());
    }
}
