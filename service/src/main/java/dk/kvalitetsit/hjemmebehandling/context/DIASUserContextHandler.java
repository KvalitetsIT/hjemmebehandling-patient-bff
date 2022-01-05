package dk.kvalitetsit.hjemmebehandling.context;

import com.auth0.jwt.interfaces.DecodedJWT;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;

public class DIASUserContextHandler implements IUserContextHandler {

	private static String CPR = "cpr";
	private static String GIVEN_NAME = "given_name";
	private static String FAMILY_NAME = "family_name";
	private static String PREFERRED_NAME = "preferred_username";
	private static String NAME = "name";
	
	public UserContext mapTokenToUser(FhirClient client, DecodedJWT jwt) {
        var context = new UserContext();
        
        // for now we only use cpr (the rest of the patientinformation comes from FHIRPatient)
        context.setCpr(jwt.getClaim(DIASUserContextHandler.CPR) !=null ? jwt.getClaim(DIASUserContextHandler.CPR).asString() : null );

        return context;


	}
}
