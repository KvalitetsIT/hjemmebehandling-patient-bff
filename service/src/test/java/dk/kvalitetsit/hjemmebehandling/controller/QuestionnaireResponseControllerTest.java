package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.CallToActionDTO;
import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ForbiddenException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.InternalServerErrorException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.model.QualifiedId;
import dk.kvalitetsit.hjemmebehandling.model.QuestionnaireResponseModel;
import dk.kvalitetsit.hjemmebehandling.service.QuestionnaireResponseService;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.QuestionnaireResponseDto;
import org.openapitools.model.UserContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class QuestionnaireResponseControllerTest {
    @InjectMocks
    private QuestionnaireResponseController subject;

    @Mock
    private QuestionnaireResponseService questionnaireResponseService;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private LocationHeaderBuilder locationHeaderBuilder;

    @Mock
    private UserContextProvider userContextProvider;

    @Test
    public void getQuestionnaireResponses_cprParameterMissing_400() {
        // Arrange
        String carePlanId = null;
        int pageNumber = 1;
        int pageSize = 10;

        // Act

        // Assert
        assertThrows(BadRequestException.class, () -> subject.getQuestionnaireResponsesByCarePlanId(carePlanId, new ArrayList<>(), pageNumber, pageSize));
    }

    @Test
    public void getQuestionnaireResponses_responsesPresent_200() throws Exception {
        // Arrange
        String carePlanId = "careplan-1";
        String questionnaireId = "questionnaire-1";
        Integer pageNumber = 1;
        Integer pageSize = 10;

        QuestionnaireResponseModel responseModel1 = new QuestionnaireResponseModel();
        QuestionnaireResponseModel responseModel2 = new QuestionnaireResponseModel();
        QuestionnaireResponseDto responseDto1 = new QuestionnaireResponseDto();
        QuestionnaireResponseDto responseDto2 = new QuestionnaireResponseDto();

        PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
        Mockito.when(questionnaireResponseService.getQuestionnaireResponses(carePlanId, List.of(questionnaireId), pageDetails)).thenReturn(List.of(responseModel1, responseModel2));
        Mockito.when(dtoMapper.mapQuestionnaireResponseModel(responseModel1)).thenReturn(responseDto1);
        Mockito.when(dtoMapper.mapQuestionnaireResponseModel(responseModel2)).thenReturn(responseDto2);

        // Act
        ResponseEntity<List<QuestionnaireResponseDto>> result = subject.getQuestionnaireResponsesByCarePlanId(carePlanId, List.of(questionnaireId), pageNumber, pageSize);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertTrue(result.getBody().contains(responseDto1));
        assertTrue(result.getBody().contains(responseDto2));
    }


    @Test
    public void getQuestionnaireResponsesForMultipleCarePlans_responsesPresent_200() throws Exception {

        // Arrange
        List<String> carePlanIds = List.of("careplan-1", "careplan-2");
        List<String> questionnaireId = List.of("questionnaire-1", "questionnaire-2");
        Integer pageNumber = 1;
        Integer pageSize = 10;

        QuestionnaireResponseModel responseModel1 = new QuestionnaireResponseModel();
        QuestionnaireResponseModel responseModel2 = new QuestionnaireResponseModel();

        responseModel1.setCarePlanId(new QualifiedId("CarePlan/" + carePlanIds.get(0)));
        responseModel2.setCarePlanId(new QualifiedId("CarePlan/" + carePlanIds.get(1)));

        QuestionnaireResponseDto responseDto1 = new QuestionnaireResponseDto();
        QuestionnaireResponseDto responseDto2 = new QuestionnaireResponseDto();

        PageDetails pageDetails = new PageDetails(pageNumber, pageSize);

        Mockito.when(questionnaireResponseService.getQuestionnaireResponsesForMultipleCarePlans(carePlanIds, questionnaireId, pageDetails)).thenReturn(List.of(responseModel1, responseModel2));
        Mockito.when(dtoMapper.mapQuestionnaireResponseModel(responseModel1)).thenReturn(responseDto1);
        Mockito.when(dtoMapper.mapQuestionnaireResponseModel(responseModel2)).thenReturn(responseDto2);

        // Act
        ResponseEntity<List<QuestionnaireResponseDto>> result = subject.getQuestionnaireResponsesForMultipleCarePlans(carePlanIds, questionnaireId, pageNumber, pageSize);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertTrue(result.getBody().contains(responseDto1));
        assertTrue(result.getBody().contains(responseDto2));
    }

    @Test
    public void getQuestionnaireResponsesForMultipleCarePlans_accessViolation_403() throws Exception {
        // Arrange
        List<String> carePlanIds = List.of("careplan-1", "careplan-2");
        List<String> questionnaireId = List.of("questionnaire-1", "questionnaire-2");

        int pageNumber = 1;
        int pageSize = 10;
        PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
        Mockito.when(questionnaireResponseService.getQuestionnaireResponsesForMultipleCarePlans(carePlanIds, questionnaireId, pageDetails)).thenThrow(AccessValidationException.class);

        // Act + Assert
        assertThrows(ForbiddenException.class, () -> subject.getQuestionnaireResponsesForMultipleCarePlans(carePlanIds, questionnaireId, pageNumber, pageSize));
    }

    @Test
    public void getQuestionnaireResponses_accessViolation_403() throws Exception {
        // Arrange
        String carePlanId = "careplan-1";
        String questionnaireId = "questionnaire-1";

        int pageNumber = 1;
        int pageSize = 10;
        PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
        Mockito.when(questionnaireResponseService.getQuestionnaireResponses(carePlanId, List.of(questionnaireId), pageDetails)).thenThrow(AccessValidationException.class);

        // Act

        // Assert
        assertThrows(ForbiddenException.class, () -> subject.getQuestionnaireResponsesByCarePlanId(carePlanId, List.of(questionnaireId), pageNumber, pageSize));
    }

    @Test
    public void getQuestionnaireResponses_failureToFetch_500() throws Exception {
        // Arrange
        String carePlanId = "careplan-1";
        String questionnaireId = "questionnaire-1";
        int pageNumber = 1;
        int pageSize = 10;

        PageDetails pageDetails = new PageDetails(pageNumber, pageSize);
        Mockito.when(questionnaireResponseService.getQuestionnaireResponses(carePlanId, List.of(questionnaireId), pageDetails)).thenThrow(new ServiceException("error", ErrorKind.INTERNAL_SERVER_ERROR, ErrorDetails.INTERNAL_SERVER_ERROR));

        // Act

        // Assert
        assertThrows(InternalServerErrorException.class, () -> subject.getQuestionnaireResponsesByCarePlanId(carePlanId, List.of(questionnaireId), pageNumber, pageSize));
    }

    @Test
    public void getQuestionnaireResponseById_responsePresent_200() throws Exception {
        // Arrange
        String questionnaireResponseId = "questionnaireresponse-1";

        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();
        Mockito.when(questionnaireResponseService.getQuestionnaireResponseById(new QualifiedId(questionnaireResponseId, ResourceType.QuestionnaireResponse))).thenReturn(Optional.of(questionnaireResponseModel));
        Mockito.when(dtoMapper.mapQuestionnaireResponseModel(questionnaireResponseModel)).thenReturn(questionnaireResponseDto);

        // Act
        ResponseEntity<QuestionnaireResponseDto> result = subject.getQuestionnaireResponseById(questionnaireResponseId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(questionnaireResponseDto, result.getBody());
    }


    @Test
    public void getQuestionnaireResponseById_responseMissing_404() throws Exception {
        // Arrange
        String questionnaireResponseId = "questionnaireresponse-1";

        Mockito.when(questionnaireResponseService.getQuestionnaireResponseById(new QualifiedId(questionnaireResponseId, ResourceType.QuestionnaireResponse))).thenReturn(Optional.empty());

        // Act

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> subject.getQuestionnaireResponseById(questionnaireResponseId));
    }


    @Test
    public void getQuestionnaireResponseById_accessViolation_403() throws Exception {
        // Arrange
        String questionnaireResponseId = "questionnaireresponse-1";

        Mockito.doThrow(AccessValidationException.class).when(questionnaireResponseService).getQuestionnaireResponseById(new QualifiedId(questionnaireResponseId, ResourceType.QuestionnaireResponse));

        // Act

        // Assert
        assertThrows(ForbiddenException.class, () -> subject.getQuestionnaireResponseById(questionnaireResponseId));
    }

    @Test
    public void getQuestionnaireResponseById_failure_500() throws Exception {
        // Arrange
        String questionnaireResponseId = "questionnaireresponse-1";

        Mockito.doThrow(new ServiceException("error", ErrorKind.INTERNAL_SERVER_ERROR, ErrorDetails.INTERNAL_SERVER_ERROR))
                .when(questionnaireResponseService).getQuestionnaireResponseById(new QualifiedId(questionnaireResponseId, ResourceType.QuestionnaireResponse));

        // Act

        // Assert
        assertThrows(InternalServerErrorException.class, () -> subject.getQuestionnaireResponseById(questionnaireResponseId));
    }

    @Test
    public void submitQuestionnaireResponse_success_201() throws Exception {
        // Arrange
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();

        String cpr = "0101010101";
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();
        Mockito.when(dtoMapper.mapQuestionnaireResponseDto(questionnaireResponseDto)).thenReturn(questionnaireResponseModel);
        Mockito.when(questionnaireResponseService.submitQuestionnaireResponse(questionnaireResponseModel, cpr)).thenReturn("questionnaireresponse-1");

        String location = "http://localhost:8080/api/v1/questionnaireresponse/questionnaireresponse-1";
        Mockito.when(locationHeaderBuilder.buildLocationHeader("questionnaireresponse-1")).thenReturn(URI.create(location));

        setupUserContext(cpr);

        // Act
        ResponseEntity<CallToActionDTO> result = subject.submitQuestionnaireResponse(questionnaireResponseDto);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getHeaders().get("Location"));
        assertEquals(location, result.getHeaders().get("Location").get(0));
    }

    @Test
    public void submitQuestionnaireResponse_accessViolation_403() throws Exception {
        // Arrange
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();

        String cpr = "0101010101";
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();
        Mockito.when(dtoMapper.mapQuestionnaireResponseDto(questionnaireResponseDto)).thenReturn(questionnaireResponseModel);

        Mockito.when(questionnaireResponseService.submitQuestionnaireResponse(questionnaireResponseModel, cpr)).thenThrow(AccessValidationException.class);

        setupUserContext(cpr);

        // Act

        // Assert
        assertThrows(ForbiddenException.class, () -> subject.submitQuestionnaireResponse(questionnaireResponseDto));
    }

    @Test
    public void submitQuestionnaireResponse_badRequest_400() throws Exception {
        // Arrange
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();

        String cpr = "0101010101";
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();
        Mockito.when(dtoMapper.mapQuestionnaireResponseDto(questionnaireResponseDto)).thenReturn(questionnaireResponseModel);

        Mockito.when(questionnaireResponseService.submitQuestionnaireResponse(questionnaireResponseModel, cpr)).thenThrow(new ServiceException("error", ErrorKind.BAD_REQUEST, ErrorDetails.INCOMPLETE_RESPONSE));

        setupUserContext(cpr);

        // Act

        // Assert
        assertThrows(BadRequestException.class, () -> subject.submitQuestionnaireResponse(questionnaireResponseDto));
    }

    @Test
    public void submitQuestionnaireResponse_failure_500() throws Exception {
        // Arrange
        QuestionnaireResponseDto questionnaireResponseDto = new QuestionnaireResponseDto();

        String cpr = "0101010101";
        QuestionnaireResponseModel questionnaireResponseModel = new QuestionnaireResponseModel();
        Mockito.when(dtoMapper.mapQuestionnaireResponseDto(questionnaireResponseDto)).thenReturn(questionnaireResponseModel);

        Mockito.when(questionnaireResponseService.submitQuestionnaireResponse(questionnaireResponseModel, cpr)).thenThrow(new ServiceException("error", ErrorKind.INTERNAL_SERVER_ERROR, ErrorDetails.INTERNAL_SERVER_ERROR));

        setupUserContext(cpr);

        // Act

        // Assert
        assertThrows(InternalServerErrorException.class, () -> subject.submitQuestionnaireResponse(questionnaireResponseDto));
    }

    private void setupUserContext(String cpr) {
        var userContext = new UserContext()
                .cpr(cpr);
        Mockito.when(userContextProvider.getUserContext()).thenReturn(userContext);
    }
}