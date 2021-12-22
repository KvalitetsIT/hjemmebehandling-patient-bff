package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.OrganizationDto;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.fhir.FhirUtils;
import dk.kvalitetsit.hjemmebehandling.model.OrganizationModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.service.OrganizationService;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Tag(name = "Organization", description = "API for retrieving Organizations.")
public class OrganizationController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    private OrganizationService organizationService;
    private DtoMapper dtoMapper;

    public OrganizationController(OrganizationService organizationService, DtoMapper dtoMapper) {
        this.organizationService = organizationService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping(value = "/v1/organization/{id}")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable("id") String id) {
        // Look up the Organization
        Optional<OrganizationModel> organization = Optional.empty();

        try {
            organization = organizationService.getOrganizationById(new QualifiedId(FhirUtils.qualifyId(id, ResourceType.Organization)));
        }
        catch(ServiceException e) {
            logger.error("Could not retrieve QuestionnaireResponse", e);
            throw toStatusCodeException(e);
        }

        if(!organization.isPresent()) {
            throw new ResourceNotFoundException(String.format("Organization with id %s not found.", id), ErrorDetails.ORGANIZATION_DOES_NOT_EXIST);
        }
        return ResponseEntity.ok(dtoMapper.mapOrganizationModel(organization.get()));
    }
}
