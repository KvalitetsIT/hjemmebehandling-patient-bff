package dk.kvalitetsit.hjemmebehandling.service.access;

import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.context.UserContext;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AccessValidatorTest {
    @InjectMocks
    private AccessValidator subject;

    @Mock
    private UserContextProvider userContextProvider;

    @Mock
    private FhirClient fhirClient;

    @Test
    @Disabled("TODO - Decide how access validation should work")
    public void validateAccess_contextNotInitialized() {
        // Arrange
        var resource = buildResource();

        // Act

        // Assert
        assertThrows(IllegalStateException.class, () -> subject.validateAccess(resource));
    }

    private DomainResource buildResource() {
        return buildResource(null);
    }

    private DomainResource buildResource(String organizationId) {
        var resource = new CarePlan();

        if(organizationId != null) {
            resource.addExtension(Systems.ORGANIZATION, new Reference(organizationId));
        }

        return resource;
    }
}