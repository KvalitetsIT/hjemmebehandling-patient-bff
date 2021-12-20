package dk.kvalitetsit.hjemmebehandling.service.access;

import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import org.hl7.fhir.r4.model.*;
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
        if(resource.getResourceType() != ResourceType.Patient) {
            throw new IllegalArgumentException(String.format("Don't know how to validate access for resource of type %s", resource.getResourceType().toString()));
        }
        Patient patient = (Patient) resource;

        // We only know how to validate access for patients: Check that the cpr matches that on the user context.
        var context = userContextProvider.getUserContext();
        if(context == null) {
            throw new IllegalStateException("UserContext was not initialized!");
        }

        if(!context.getCpr().equals(patient.getIdentifierFirstRep().getValue())) {
            throw new AccessValidationException(String.format("The current user is not allowed to access data belonging to patient %s", patient.getId()));
        }
    }
}
