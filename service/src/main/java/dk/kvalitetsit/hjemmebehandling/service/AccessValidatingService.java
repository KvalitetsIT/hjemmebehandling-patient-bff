package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import org.hl7.fhir.r4.model.DomainResource;

import java.util.List;

public abstract class AccessValidatingService {
    private AccessValidator accessValidator;

    public AccessValidatingService(AccessValidator accessValidator) {
        this.accessValidator = accessValidator;
    }

    protected void validateCorrectSubject(FhirLookupResult lookupResult) throws AccessValidationException {
        // Validate that the user is allowed to retrieve the resource (ie. exactly one Patient-resource was retrieved, and the current user is allowed to access it).
        if(lookupResult.getPatients().size() != 1) {
            throw new IllegalStateException(String.format("Expected to look up exactly one patient when retrieving resource, got %d.", lookupResult.getPatients().size()));
        }
        validateAccess(lookupResult.getPatients().get(0));
    }

    private void validateAccess(DomainResource resource) throws AccessValidationException {
        accessValidator.validateAccess(resource);
    }
}
