package dk.kvalitetsit.hjemmebehandling.context;

import com.auth0.jwt.interfaces.DecodedJWT;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import org.openapitools.model.UserContext;

public interface IUserContextHandler {

    UserContext mapTokenToUser(FhirClient client, DecodedJWT jwt);
}
