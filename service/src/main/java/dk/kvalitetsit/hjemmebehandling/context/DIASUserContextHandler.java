package dk.kvalitetsit.hjemmebehandling.context;

import com.auth0.jwt.interfaces.DecodedJWT;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import org.openapitools.model.UserContext;

public class DIASUserContextHandler implements IUserContextHandler {

    private static final String CPR = "cpr";
    private static final String GIVEN_NAME = "given_name";
    private static final String FAMILY_NAME = "family_name";
    private static final String PREFERRED_NAME = "preferred_username";
    private static final String NAME = "name";

    public UserContext mapTokenToUser(FhirClient client, DecodedJWT jwt) {
        // for now we only use cpr (the rest of the patientinformation comes from FHIRPatient)
        return new UserContext().cpr(jwt.getClaim(DIASUserContextHandler.CPR) != null ? jwt.getClaim(DIASUserContextHandler.CPR).asString() : null);
    }
}
