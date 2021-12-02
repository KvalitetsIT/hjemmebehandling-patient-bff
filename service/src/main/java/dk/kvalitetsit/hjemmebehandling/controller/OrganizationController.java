package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.OrganizationDto;
import dk.kvalitetsit.hjemmebehandling.service.OrganizationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Organization", description = "API for retrieving Organizations.")
public class OrganizationController {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    private OrganizationService organizationService;
    private DtoMapper dtoMapper;

    public OrganizationController(OrganizationService organizationService, DtoMapper dtoMapper) {
        this.organizationService = organizationService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping(value = "/v1/organization/{id}")
    public ResponseEntity<OrganizationDto> getOrganization(@PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }
}
