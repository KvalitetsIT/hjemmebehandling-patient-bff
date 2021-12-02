package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.QuestionnaireResponseDto;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.model.QuestionnaireResponseModel;
import dk.kvalitetsit.hjemmebehandling.service.QuestionnaireResponseService;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "QuestionnaireResponse", description = "API for manipulating and retrieving QuestionnaireResponses.")
public class QuestionnaireResponseController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireResponseController.class);

    private QuestionnaireResponseService questionnaireResponseService;
    private DtoMapper dtoMapper;
    private LocationHeaderBuilder locationHeaderBuilder;

    public QuestionnaireResponseController(QuestionnaireResponseService questionnaireResponseService, DtoMapper dtoMapper, LocationHeaderBuilder locationHeaderBuilder) {
        this.questionnaireResponseService = questionnaireResponseService;
        this.dtoMapper = dtoMapper;
        this.locationHeaderBuilder = locationHeaderBuilder;
    }

    @GetMapping(value = "/v1/questionnaireresponses/{carePlanId}")
    public ResponseEntity<List<QuestionnaireResponseDto>> getQuestionnaireResponsesByCarePlanId(@PathVariable("carePlanId") String carePlanId, @RequestParam("page_number") Optional<Integer> pageNumber, @RequestParam("page_size") Optional<Integer> pageSize) {
        if(carePlanId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<QuestionnaireResponseModel> questionnaireResponses = questionnaireResponseService.getQuestionnaireResponses(carePlanId);
            if(questionnaireResponses.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(questionnaireResponses.stream().map(qr -> dtoMapper.mapQuestionnaireResponseModel(qr)).collect(Collectors.toList()));
        }
        catch(AccessValidationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catch(ServiceException e) {
            logger.error("Could not look up questionnaire responses by cpr and questionnaire ids", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/v1/questionnaireresponse/{id}")
    public ResponseEntity<QuestionnaireResponseDto> getQuestionnaireResponseById(@PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }

    @PostMapping(value = "/v1/questionnaireresponse")
    public ResponseEntity<Void> submitQuestionnaireResponse(QuestionnaireResponseDto questionnaireResponseDto) {
        // The operations needs to compute the triaging category (based on the thresholds, which can be found on the careplan), and update the careplan with fresh SatisfiedUntil-timestamps.
        // This update must be atomic - preferably by using a fhir transaction (the medarbejder-bff project contains an example).

        // Return 201 on success.
        throw new UnsupportedOperationException();
    }
}
