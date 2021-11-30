package dk.kvalitetsit.hjemmebehandling.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.PatientApi;
import org.openapitools.client.model.PatientDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatientIntegrationTest extends AbstractIntegrationTest {
    private PatientApi subject;

    @BeforeEach
    public void setup() {
        subject = new PatientApi();

        subject.getApiClient().setBasePath(enhanceBasePath(subject.getApiClient().getBasePath()));
    }

    @Test
    public void getPatient_success() throws Exception {
        // Arrange
        String cpr = "0101010101";

        // Act
        ApiResponse<PatientDto> response = subject.getPatientWithHttpInfo(cpr);

        // Assert
        assertEquals(200, response.getStatusCode());
    }
}
