package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.QuestionnaireResponseDto;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
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

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Tag(name = "QuestionnaireResponse", description = "API for manipulating and retrieving QuestionnaireResponses.")
public class QuestionnaireResponseController extends BaseController {
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
            throw new BadRequestException(ErrorDetails.PARAMETERS_INCOMPLETE);
        }

        try {
            List<QuestionnaireResponseModel> questionnaireResponses = questionnaireResponseService.getQuestionnaireResponses(carePlanId);

            return ResponseEntity.ok(questionnaireResponses.stream().map(qr -> dtoMapper.mapQuestionnaireResponseModel(qr)).collect(Collectors.toList()));
        }
        catch(AccessValidationException | ServiceException e) {
            logger.error("Could not look up questionnaire responses by careplan id", e);
            throw toStatusCodeException(e);
        }
    }

    @GetMapping(value = "/v1/questionnaireresponse/{id}")
    public ResponseEntity<QuestionnaireResponseDto> getQuestionnaireResponseById(@PathVariable("id") String id) {
        throw new UnsupportedOperationException();
    }

    @PostMapping(value = "/v1/questionnaireresponse")
    public ResponseEntity<Void> submitQuestionnaireResponse(@RequestBody QuestionnaireResponseDto questionnaireResponseDto) {
        String questionnaireResponseId = null;

        // TODO - get cpr from user context ...
        String cpr = "0101010101";
        try {
            questionnaireResponseId = questionnaireResponseService.submitQuestionnaireResponse(dtoMapper.mapQuestionnaireResponseDto(questionnaireResponseDto), cpr);
        }
        catch(AccessValidationException | ServiceException e) {
            logger.error("Error creating CarePlan", e);
            throw toStatusCodeException(e);
        }

        URI location = locationHeaderBuilder.buildLocationHeader(questionnaireResponseId);
        return ResponseEntity.created(location).build();
    }
}
