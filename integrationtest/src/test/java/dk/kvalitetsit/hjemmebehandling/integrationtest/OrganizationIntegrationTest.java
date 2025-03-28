package dk.kvalitetsit.hjemmebehandling.integrationtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiResponse;
import org.openapitools.client.api.OrganizationApi;
import org.openapitools.client.model.OrganizationDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrganizationIntegrationTest extends AbstractIntegrationTest {
    private OrganizationApi subject;

    @BeforeEach
    public void setup() {
        subject = new OrganizationApi();
        subject.getApiClient().setBasePath(enhanceBasePath(subject.getApiClient().getBasePath()));
    }

    @Test
    public void getOrganization_success() throws Exception {
        String organizationId = "organization-1";
        ApiResponse<OrganizationDto> response = subject.getOrganizationWithHttpInfo(organizationId);
        assertEquals(200, response.getStatusCode());
    }
}
