package dk.kvalitetsit.hjemmebehandling.context;

import com.auth0.jwt.interfaces.DecodedJWT;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;

public class MockContextHandler implements IUserContextHandler {
    private String cpr;

    public MockContextHandler(String cpr) {
        this.cpr = cpr;
    }

    @Override
	public UserContext mapTokenToUser(FhirClient client, DecodedJWT jwt) {
		var context = new UserContext();

        context.setCpr(cpr);
        context.setFullName("Test Testsen");
        context.setFirstName("Test");
        context.setLastName("Testsen");
        context.setUserId("TesTes");
        context.setEntitlements(new String[]{"DIAS_HJEMMEBEHANDLING_Sygeplejerske"} );

        return context;
	}

}
