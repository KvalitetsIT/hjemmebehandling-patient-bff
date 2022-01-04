package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.QuestionnaireResponseDto;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.model.QuestionnaireResponseModel;
import dk.kvalitetsit.hjemmebehandling.service.QuestionnaireResponseService;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.ResourceType;
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
    private UserContextProvider userContextProvider;

    public QuestionnaireResponseController(QuestionnaireResponseService questionnaireResponseService, DtoMapper dtoMapper, LocationHeaderBuilder locationHeaderBuilder, UserContextProvider userContextProvider) {
        this.questionnaireResponseService = questionnaireResponseService;
        this.dtoMapper = dtoMapper;
        this.locationHeaderBuilder = locationHeaderBuilder;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping(value = "/v1/questionnaireresponses/{carePlanId}")
    public ResponseEntity<List<QuestionnaireResponseDto>> getQuestionnaireResponsesByCarePlanId(@PathVariable("carePlanId") String carePlanId, @RequestParam("page_number") Integer pageNumber, @RequestParam("page_size") Integer pageSize) {
        if(carePlanId == null) {
            throw new BadRequestException(ErrorDetails.PARAMETERS_INCOMPLETE);
        }

        try {
            PageDetails pageDetails = new PageDetails(pageNumber,pageSize);
            List<QuestionnaireResponseModel> questionnaireResponses = questionnaireResponseService.getQuestionnaireResponses(carePlanId,pageDetails);

            return ResponseEntity.ok(questionnaireResponses.stream().map(qr -> dtoMapper.mapQuestionnaireResponseModel(qr)).collect(Collectors.toList()));
        }
        catch(AccessValidationException | ServiceException e) {
            logger.error("Could not look up questionnaire responses by careplan id", e);
            throw toStatusCodeException(e);
        }
    }

    @GetMapping(value = "/v1/questionnaireresponse/{id}")
    public ResponseEntity<QuestionnaireResponseDto> getQuestionnaireResponseById(@PathVariable("id") String id) {
        // Look up the QuestionnaireResponse
        Optional<QuestionnaireResponseModel> questionnaireResponse = Optional.empty();

        try {
            questionnaireResponse = questionnaireResponseService.getQuestionnaireResponseById(new QualifiedId(id, ResourceType.QuestionnaireResponse));
        }
        catch(AccessValidationException | ServiceException e) {
            logger.error("Could not retrieve QuestionnaireResponse", e);
            throw toStatusCodeException(e);
        }

        if(!questionnaireResponse.isPresent()) {
            throw new ResourceNotFoundException(String.format("QuestionnaireResponse with id %s not found.", id), ErrorDetails.QUESTIONNAIRE_RESPONSE_DOES_NOT_EXIST);
        }
        return ResponseEntity.ok(dtoMapper.mapQuestionnaireResponseModel(questionnaireResponse.get()));
    }

    @PostMapping(value = "/v1/questionnaireresponse")
    public ResponseEntity<Void> submitQuestionnaireResponse(@RequestBody QuestionnaireResponseDto questionnaireResponseDto) {
        String questionnaireResponseId = null;

        String cpr = userContextProvider.getUserContext().getCpr();
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
