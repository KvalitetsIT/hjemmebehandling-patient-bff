package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.ExtensionMapper;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.service.access.AccessValidator;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CarePlanServiceTest {
    private static final String CPR_1 = "0101010101";
    private static final String CAREPLAN_ID_1 = "CarePlan/careplan-1";
    private static final String CAREPLAN_ID_2 = "CarePlan/careplan-2";
    private static final String PATIENT_ID_1 = "Patient/patient-1";
    private static final Instant POINT_IN_TIME = Instant.parse("2021-11-23T00:00:00.000Z");
    @InjectMocks
    private CarePlanService subject;
    @Mock
    private FhirClient fhirClient;
    @Mock
    private FhirMapper fhirMapper;
    @Mock
    private AccessValidator accessValidator;

    @Test
    public void getActiveCarePlan_carePlanPresent_returnsCarePlan() throws Exception {
        String cpr = CPR_1;
        CarePlan carePlan = buildCarePlan(CAREPLAN_ID_1);
        Patient patient = buildPatient();
        FhirLookupResult lookupResult = FhirLookupResult.fromResources(carePlan, patient);

        Mockito.when(fhirClient.lookupActiveCarePlans(cpr)).thenReturn(lookupResult);
        CarePlanModel carePlanModel = new CarePlanModel();
        Mockito.when(fhirMapper.mapCarePlan(carePlan, lookupResult)).thenReturn(carePlanModel);

        List<CarePlanModel> result = subject.getActiveCarePlans(cpr);
        assertFalse(result.isEmpty());
        assertEquals(carePlanModel, result.getFirst());
    }

    @Test
    public void getActiveCarePlan_carePlanMissing_returnsEmpty() throws Exception {
        String cpr = CPR_1;
        Mockito.when(fhirClient.lookupActiveCarePlans(cpr)).thenReturn(FhirLookupResult.fromResources());
        List<CarePlanModel> result = subject.getActiveCarePlans(cpr);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getActiveCarePlan_malformedResult_throwsException() throws Exception {
        String cpr = CPR_1;
        CarePlan carePlan1 = buildCarePlan(CAREPLAN_ID_1);
        CarePlan carePlan2 = buildCarePlan(CAREPLAN_ID_2);
        Mockito.when(fhirClient.lookupActiveCarePlans(cpr)).thenReturn(FhirLookupResult.fromResources(carePlan1, carePlan2));
        assertThrows(IllegalStateException.class, () -> subject.getActiveCarePlans(cpr));
    }


    private CarePlan buildCarePlan(String carePlanId) {
        CarePlan carePlan = new CarePlan();
        carePlan.setId(carePlanId);
        carePlan.setSubject(new Reference(CarePlanServiceTest.PATIENT_ID_1));
        return carePlan;
    }

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setId(CarePlanServiceTest.PATIENT_ID_1);
        return patient;
    }
}