package dk.kvalitetsit.hjemmebehandling.service;

import dk.kvalitetsit.hjemmebehandling.fhir.FhirClient;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirLookupResult;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirMapper;
import dk.kvalitetsit.hjemmebehandling.model.OrganizationModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import org.hl7.fhir.r4.model.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {
    private static final String ORGANIZATION_ID_1 = "Organization/organization-1";
    @InjectMocks
    private OrganizationService subject;
    @Mock
    private FhirClient fhirClient;
    @Mock
    private FhirMapper fhirMapper;

    @Test
    public void getOrganizationById_organizationPresent_returnsOrganization() throws Exception {
        String organizationId = ORGANIZATION_ID_1;
        Organization organization = buildOrganization();
        Mockito.when(fhirClient.lookupOrganizationById(organizationId)).thenReturn(FhirLookupResult.fromResources(organization));
        OrganizationModel organizationModel = buildOrganizationModel();
        Mockito.when(fhirMapper.mapOrganization(organization)).thenReturn(organizationModel);
        Optional<OrganizationModel> result = subject.getOrganizationById(new QualifiedId(organizationId));
        assertEquals(organizationModel, result.get());
    }

    @Test
    public void getOrganizationById_organizationMissing_returnsEmpty() throws Exception {
        String organizationId = ORGANIZATION_ID_1;
        Mockito.when(fhirClient.lookupOrganizationById(organizationId)).thenReturn(FhirLookupResult.fromResources());
        Optional<OrganizationModel> result = subject.getOrganizationById(new QualifiedId(organizationId));
        assertFalse(result.isPresent());
    }

    private Organization buildOrganization() {
        Organization organization = new Organization();
        organization.setId(OrganizationServiceTest.ORGANIZATION_ID_1);
        return organization;
    }

    private OrganizationModel buildOrganizationModel() {
        OrganizationModel organizationModel = new OrganizationModel();
        organizationModel.setId(new QualifiedId(OrganizationServiceTest.ORGANIZATION_ID_1));
        return organizationModel;
    }
}