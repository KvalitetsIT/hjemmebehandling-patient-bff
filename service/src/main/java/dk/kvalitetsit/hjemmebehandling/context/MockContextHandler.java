package dk.kvalitetsit.hjemmebehandling.context;

import com.auth0.jwt.interfaces.DecodedJWT;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import org.openapitools.model.UserContext;

import java.util.List;

public class MockContextHandler implements IUserContextHandler {
    private final String cpr;

    public MockContextHandler(String cpr) {
        this.cpr = cpr;
    }

    @Override
    public UserContext mapTokenToUser(FhirClient client, DecodedJWT jwt) {

        return new UserContext()
                .cpr(cpr)
                .fullName("Test Testsen")
                .firstName("Test")
                .lastName("Testsen")
                .userId("TesTes")
                .entitlements(List.of("DIAS_HJEMMEBEHANDLING_Sygeplejerske"));
    }

}
