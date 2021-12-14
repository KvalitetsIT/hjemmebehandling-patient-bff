package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.ExtensionMapper;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CarePlanServiceTest {
    @InjectMocks
    private CarePlanService subject;

    @Mock
    private FhirClient fhirClient;

    @Mock
    private FhirMapper fhirMapper;

    private static final String CPR_1 = "0101010101";

    private static final String CAREPLAN_ID_1 = "CarePlan/careplan-1";
    private static final String CAREPLAN_ID_2 = "CarePlan/careplan-2";
    private static final String PATIENT_ID_1 = "Patient/patient-1";

    private static final Instant POINT_IN_TIME = Instant.parse("2021-11-23T00:00:00.000Z");

    @Test
    public void getActiveCarePlan_carePlanPresent_returnsCarePlan() throws Exception {
        // Arrange
        String cpr = CPR_1;

        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1);

        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan);
        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(lookupResult);

        CarePlanModel carePlanModel = new CarePlanModel();
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        // Act
        Optional<CarePlanModel> result = subject.getActiveCarePlan(cpr);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(carePlanModel, result.get());
    }

    @Test
    public void getActiveCarePlan_carePlanMissing_returnsEmpty() throws Exception {
        // Arrange
        String cpr = CPR_1;

        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(FhirLookupResult.fromResources());

        // Act
        Optional<CarePlanModel> result = subject.getActiveCarePlan(cpr);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void getActiveCarePlan_malformedResult_throwsException() throws Exception {
        // Arrange
        String cpr = CPR_1;

        CarePlan carePlan1 = buildCarePlan(CAREPLAN_ID_1, PATIENT_ID_1);
        CarePlan carePlan2 = buildCarePlan(CAREPLAN_ID_2, PATIENT_ID_1);

        Mockito.when(fhirClient.lookupActiveCarePlan(cpr)).thenReturn(FhirLookupResult.fromResources(carePlan1, carePlan2));

        // Act

        // Assert
        assertThrows(IllegalStateException.class, () -> subject.getActiveCarePlan(cpr));
    }

    private CarePlan buildCarePlan(String carePlanId, String patientId) {
        return buildCarePlan(carePlanId, patientId, null);
    }

    private CarePlan buildCarePlan(String carePlanId, String patientId, String questionnaireId) {
        CarePlan carePlan = new CarePlan();

        carePlan.setId(carePlanId);
        carePlan.setSubject(new Reference(patientId));

        if(questionnaireId != null) {
            CarePlan.CarePlanActivityDetailComponent detail = new CarePlan.CarePlanActivityDetailComponent();
            detail.setInstantiatesCanonical(List.of(new CanonicalType(questionnaireId)));
            detail.setScheduled(new Timing());
            detail.addExtension(ExtensionMapper.mapActivitySatisfiedUntil(POINT_IN_TIME));
            carePlan.addActivity().setDetail(detail);
        }

        return carePlan;
    }
}