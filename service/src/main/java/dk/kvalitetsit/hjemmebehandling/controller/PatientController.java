package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.PatientDto;
import dk.kvalitetsit.hjemmebehandling.service.PatientService;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Patient", description = "API for retrieving information about the patient.")
public class PatientController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserContextProvider userContextProvider;

    private final PatientService patientService;
    private final DtoMapper dtoMapper;
    public PatientController(UserContextProvider userContextProvider, PatientService patientService, DtoMapper dtoMapper) {
        this.userContextProvider = userContextProvider;
        this.patientService = patientService;
        this.dtoMapper = dtoMapper;
    }


    @GetMapping(value = "/v1/patient")
    public @ResponseBody ResponseEntity<PatientDto> getPatient() throws JsonMappingException, JsonProcessingException {
        logger.info("Getting current patient");
        var cpr = userContextProvider.getUserContext().getCpr();

        try {
            // TODO: handle 'Optional.get()' without 'isPresent()' check
            return ResponseEntity.ok(dtoMapper.mapPatientModel(patientService.getPatient(cpr.get())));
        } catch(ServiceException e) {
            logger.error("Could not retrieve patient", e);
            throw toStatusCodeException(e);
        }

    }

}
