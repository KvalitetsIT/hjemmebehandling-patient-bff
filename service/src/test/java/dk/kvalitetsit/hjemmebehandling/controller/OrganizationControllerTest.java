package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.controller.exception.InternalServerErrorException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.model.OrganizationModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.service.OrganizationService;
import dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.OrganizationDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrganizationControllerTest {
    @InjectMocks
    private OrganizationController subject;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private DtoMapper dtoMapper;

    @Test
    public void getOrganization_organizationPresent_200() throws Exception {
        // Arrange
        String organizationId = "Organization/organization-1";

        OrganizationModel organizationModel = new OrganizationModel();
        OrganizationDto organizationDto = new OrganizationDto();

        Mockito.when(organizationService.getOrganizationById(new QualifiedId(organizationId))).thenReturn(Optional.of(organizationModel));
        Mockito.when(dtoMapper.mapOrganizationModel(organizationModel)).thenReturn(organizationDto);

        // Act
        ResponseEntity<OrganizationDto> result = subject.getOrganization(organizationId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(organizationDto, result.getBody());
    }

    @Test
    public void getOrganization_organizationMissing_404() throws Exception {
        // Arrange
        String organizationId = "Organization/organization-1";
        Mockito.when(organizationService.getOrganizationById(new QualifiedId(organizationId))).thenReturn(Optional.empty());

        // Act

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> subject.getOrganization(organizationId));
    }

    @Test
    public void getOrganization_failure_500() throws Exception {
        // Arrange
        String organizationId = "Organization/organization-1";
        Mockito.doThrow(new ServiceException("error", ErrorKind.INTERNAL_SERVER_ERROR, ErrorDetails.INTERNAL_SERVER_ERROR)).when(organizationService).getOrganizationById(new QualifiedId(organizationId));

        // Act

        // Assert
        assertThrows(InternalServerErrorException.class, () -> subject.getOrganization(organizationId));
    }
}