package dk.kvalitetsit.hjemmebehandling.service.access;

import dk.kvalitetsit.hjemmebehandling.constants.Systems;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.UserContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AccessValidatorTest {
    private final String PATIENT_ID_1 = "Patient/patient-1";
    private final String CPR_1 = "0101010101";
    private final String CPR_2 = "0202020202";
    @InjectMocks
    private AccessValidator subject;
    @Mock
    private UserContextProvider userContextProvider;
    @Mock
    private FhirClient fhirClient;

    @Test
    public void validateAccess_unknownResourceType() {
        var resource = new QuestionnaireResponse();
        assertThrows(IllegalArgumentException.class, () -> subject.validateAccess(resource));
    }

    @Test
    public void validateAccess_contextNotInitialized() {
        var resource = buildResource(PATIENT_ID_1, CPR_1);
        assertThrows(IllegalStateException.class, () -> subject.validateAccess(resource));
    }

    @Test
    public void validateAccess_success() {
        var resource = buildResource(PATIENT_ID_1, CPR_1);
        UserContext context = buildContext(CPR_1);
        Mockito.when(userContextProvider.getUserContext()).thenReturn(context);
        assertDoesNotThrow(() -> subject.validateAccess(resource));
    }

    @Test
    public void validateAccess_failure() {
        var resource = buildResource(PATIENT_ID_1, CPR_1);
        UserContext context = buildContext(CPR_2);
        Mockito.when(userContextProvider.getUserContext()).thenReturn(context);
        assertThrows(AccessValidationException.class, () -> subject.validateAccess(resource));
    }

    private DomainResource buildResource(String patientId, String cpr) {
        var resource = new Patient();
        resource.setId(patientId);
        resource.addIdentifier()
                .setSystem(Systems.CPR)
                .setValue(cpr);
        return resource;
    }

    private UserContext buildContext(String cpr) {
        return new UserContext().cpr(cpr);
    }
}