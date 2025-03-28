package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.model.QuestionnaireResponseModel;
import dk.kvalitetsit.hjemmebehandling.service.QuestionnaireResponseService;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import org.hl7.fhir.r4.model.ResourceType;
import org.openapitools.api.QuestionnaireResponseApi;
import org.openapitools.model.CallToActionDTO;
import org.openapitools.model.QuestionnaireResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class QuestionnaireResponseController extends BaseController implements QuestionnaireResponseApi {
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireResponseController.class);

    private final QuestionnaireResponseService questionnaireResponseService;
    private final DtoMapper dtoMapper;
    private final LocationHeaderBuilder locationHeaderBuilder;
    private final UserContextProvider userContextProvider;

    public QuestionnaireResponseController(QuestionnaireResponseService questionnaireResponseService, DtoMapper dtoMapper, LocationHeaderBuilder locationHeaderBuilder, UserContextProvider userContextProvider) {
        this.questionnaireResponseService = questionnaireResponseService;
        this.dtoMapper = dtoMapper;
        this.locationHeaderBuilder = locationHeaderBuilder;
        this.userContextProvider = userContextProvider;
    }



    @Override
    public ResponseEntity<List<QuestionnaireResponseDto>> getQuestionnaireResponsesByCarePlanId(String carePlanId, List<String> questionnaireIds, Integer pageNumber, Integer pageSize) {
        if (carePlanId == null || questionnaireIds == null || questionnaireIds.isEmpty()) {
            throw new BadRequestException(ErrorDetails.PARAMETERS_INCOMPLETE);
        }

        try {
            PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
            List<QuestionnaireResponseModel> questionnaireResponses = questionnaireResponseService.getQuestionnaireResponses(carePlanId, questionnaireIds, pageDetails);

            return ResponseEntity.ok(questionnaireResponses.stream().map(dtoMapper::mapQuestionnaireResponseModel).collect(Collectors.toList()));
        } catch (AccessValidationException | ServiceException e) {
            logger.error("Could not look up questionnaire responses by careplan id", e);
            throw toStatusCodeException(e);
        }
    }

    @Override
    public ResponseEntity<QuestionnaireResponseDto> getQuestionnaireResponseById(String id) {
        Optional<QuestionnaireResponseModel> questionnaireResponse = Optional.empty();

        try {
            questionnaireResponse = questionnaireResponseService.getQuestionnaireResponseById(new QualifiedId(id, ResourceType.QuestionnaireResponse));
        } catch (AccessValidationException | ServiceException e) {
            logger.error("Could not retrieve QuestionnaireResponse", e);
            throw toStatusCodeException(e);
        }

        if (questionnaireResponse.isEmpty()) {
            throw new ResourceNotFoundException(String.format("QuestionnaireResponse with id %s not found.", id), ErrorDetails.QUESTIONNAIRE_RESPONSE_DOES_NOT_EXIST);
        }
        return ResponseEntity.ok(dtoMapper.mapQuestionnaireResponseModel(questionnaireResponse.get()));
    }


    @Override
    public ResponseEntity<List<QuestionnaireResponseDto>> getQuestionnaireResponsesForMultipleCarePlans(List<String> carePlanIds, List<String> questionnaireIds, Integer pageNumber, Integer pageSize) {
        if (carePlanIds == null || carePlanIds.isEmpty() || questionnaireIds == null || questionnaireIds.isEmpty()) {
            throw new BadRequestException(ErrorDetails.PARAMETERS_INCOMPLETE);
        }

        try {
            PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
            List<QuestionnaireResponseModel> questionnaireResponses = questionnaireResponseService.getQuestionnaireResponsesForMultipleCarePlans(carePlanIds, questionnaireIds, pageDetails);

            return ResponseEntity.ok(questionnaireResponses.stream().map(dtoMapper::mapQuestionnaireResponseModel).collect(Collectors.toList()));
        } catch (AccessValidationException e) {
            logger.error("Could not look up questionnaire responses by careplan id", e);
            throw toStatusCodeException(e);
        }
    }

    @Override
    public ResponseEntity<CallToActionDTO> submitQuestionnaireResponse(QuestionnaireResponseDto questionnaireResponseDto) {
        String questionnaireResponseId = null;
        String callToAction = null;

        // TODO: handle 'Optional.get()' without 'isPresent()' check
        String cpr = userContextProvider.getUserContext().getCpr().get();
        try {
            QuestionnaireResponseModel questionnaireResponseModel = dtoMapper.mapQuestionnaireResponseDto(questionnaireResponseDto);
            questionnaireResponseId = questionnaireResponseService.submitQuestionnaireResponse(questionnaireResponseModel, cpr);

            callToAction = questionnaireResponseService.getCallToAction(questionnaireResponseModel);

        } catch (AccessValidationException | ServiceException e) {
            logger.error("Error creating CarePlan", e);
            throw toStatusCodeException(e);
        }

        URI location = locationHeaderBuilder.buildLocationHeader(questionnaireResponseId);
        CallToActionDTO responseCallToAction = new CallToActionDTO().callToAction(callToAction);
        return ResponseEntity.created(location).body(responseCallToAction);
    }
}
