package dk.kvalitetsit.hjemmebehandling.service.access;

import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccessValidator {
    private UserContextProvider userContextProvider;
    private FhirClient fhirClient;

    public AccessValidator(UserContextProvider userContextProvider, FhirClient fhirClient) {
        this.userContextProvider = userContextProvider;
        this.fhirClient = fhirClient;
    }

    public void validateAccess(DomainResource resource) throws AccessValidationException {
        validateAccess(List.of(resource));
    }

    public void validateAccess(List<? extends DomainResource> resources) throws AccessValidationException {
        // TODO - figure out how this should work for patients!

    }
}
