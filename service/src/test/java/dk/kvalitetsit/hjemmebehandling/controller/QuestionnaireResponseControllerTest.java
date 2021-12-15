package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.CarePlanDto;
import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.api.QuestionnaireResponseDto;
import dk.kvalitetsit.hjemmebehandling.constants.ExaminationStatus;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContext;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ForbiddenException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.InternalServerErrorException;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.model.CarePlanModel;
import dk.kvalitetsit.hjemmebehandling.model.QuestionnaireResponseModel;
import dk.kvalitetsit.hjemmebehandling.service.QuestionnaireResponseService;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import dk.kvalitetsit.hjemmebehandling.types.PageDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.swing.text.html.Option;
import java.net.URI;
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
        Optional<Integer> pageNumber = Optional.of(1);
        Optional<Integer> pageSize = Optional.of(10);

        // Act

        // Assert
        assertThrows(BadRequestException.class, () -> subject.getQuestionnaireResponsesByCarePlanId(carePlanId, pageNumber, pageSize));
    }

    @Test
    public void getQuestionnaireResponses_responsesPresent_200() throws Exception {
        // Arrange
        String carePlanId = "careplan-1";
        Optional<Integer> pageNumber = Optional.of(1);
        Optional<Integer> pageSize = Optional.of(10);

        QuestionnaireResponseModel responseModel1 = new QuestionnaireResponseModel();
        QuestionnaireResponseModel responseModel2 = new QuestionnaireResponseModel();
        QuestionnaireResponseDto responseDto1 = new QuestionnaireResponseDto();
        QuestionnaireResponseDto responseDto2 = new QuestionnaireResponseDto();

        Mockito.when(questionnaireResponseService.getQuestionnaireResponses(carePlanId)).thenReturn(List.of(responseModel1, responseModel2));
        Mockito.when(dtoMapper.mapQuestionnaireResponseModel(responseModel1)).thenReturn(responseDto1);
        Mockito.when(dtoMapper.mapQuestionnaireResponseModel(responseModel2)).thenReturn(responseDto2);

        // Act
        ResponseEntity<List<QuestionnaireResponseDto>> result = subject.getQuestionnaireResponsesByCarePlanId(carePlanId, pageNumber, pageSize);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        assertTrue(result.getBody().contains(responseDto1));
        assertTrue(result.getBody().contains(responseDto2));
    }

    @Test
    public void getQuestionnaireResponses_accessViolation_403() throws Exception {
        // Arrange
        String carePlanId = "careplan-1";
        Optional<Integer> pageNumber = Optional.of(1);
        Optional<Integer> pageSize = Optional.of(10);

        Mockito.when(questionnaireResponseService.getQuestionnaireResponses(carePlanId)).thenThrow(AccessValidationException.class);

        // Act

        // Assert
        assertThrows(ForbiddenException.class, () -> subject.getQuestionnaireResponsesByCarePlanId(carePlanId, pageNumber, pageSize));
    }

    @Test
    public void getQuestionnaireResponses_failureToFetch_500() throws Exception {
        // Arrange
        String carePlanId = "careplan-1";
        Optional<Integer> pageNumber = Optional.of(1);
        Optional<Integer> pageSize = Optional.of(10);

        Mockito.when(questionnaireResponseService.getQuestionnaireResponses(carePlanId)).thenThrow(new ServiceException("error", ErrorKind.INTERNAL_SERVER_ERROR, ErrorDetails.INTERNAL_SERVER_ERROR));

        // Act

        // Assert
        assertThrows(InternalServerErrorException.class, () -> subject.getQuestionnaireResponsesByCarePlanId(carePlanId, pageNumber, pageSize));
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
        ResponseEntity<Void> result = subject.submitQuestionnaireResponse(questionnaireResponseDto);

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

        Mockito.when(questionnaireResponseService.submitQuestionnaireResponse(questionnaireResponseModel, cpr)).thenThrow(new ServiceException("error", ErrorKind.BAD_REQUEST, ErrorDetails. INCOMPLETE_RESPONSE));

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
        var userContext = new UserContext();
        userContext.setCpr(cpr);
        Mockito.when(userContextProvider.getUserContext()).thenReturn(userContext);
    }
}