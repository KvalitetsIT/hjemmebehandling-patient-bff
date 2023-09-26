package dk.kvalitetsit.hjemmebehandling.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.CarePlanApi;
import org.openapitools.client.api.QuestionnaireResponseApi;
import org.openapitools.client.model.CarePlanDto;
import org.openapitools.client.model.QuestionnaireResponseDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarePlanIntegrationTest extends AbstractIntegrationTest {
    private CarePlanApi subject;

    @BeforeEach
    public void setup() {
        subject = new CarePlanApi();

        subject.getApiClient().setBasePath(enhanceBasePath(subject.getApiClient().getBasePath()));
    }
/*
    @Test
    public void getQuestionnaireResponsesByCarePlanId_success() throws Exception {
        // Arrange

        // Act
        ApiResponse<CarePlanDto> response = subject.getActiveCarePlanWithHttpInfo();

        // Assert
        assertEquals(200, response.getStatusCode());
    }

 */
}
