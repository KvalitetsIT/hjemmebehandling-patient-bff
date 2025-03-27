package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.api.DtoMapper;
import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.context.UserContextProvider;
import dk.kvalitetsit.hjemmebehandling.controller.exception.InternalServerErrorException;
import dk.kvalitetsit.hjemmebehandling.controller.http.LocationHeaderBuilder;
import dk.kvalitetsit.hjemmebehandling.service.CarePlanService;
import dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.UserContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CarePlanControllerTest {
    private static final String REQUEST_URI = "http://localhost:8080";
    @InjectMocks
    private CarePlanController subject;
    @Mock
    private CarePlanService carePlanService;
    @Mock
    private DtoMapper dtoMapper;
    @Mock
    private LocationHeaderBuilder locationHeaderBuilder;
    @Mock
    private UserContextProvider userContextProvider;

    @BeforeEach
    public void setup() {
        var userContext = new UserContext().cpr("0101010101");
        Mockito.when(userContextProvider.getUserContext()).thenReturn(userContext);
    }

    /*
        @Test
        public void getActiveCarePlan_carePlanPresent_200() throws Exception {
            // Arrange
            String cpr = "0101010101";

            CarePlanModel carePlanModel = new CarePlanModel();
            CarePlanDto carePlanDto = new CarePlanDto();
            Mockito.when(carePlanService.getActiveCarePlan(cpr)).thenReturn(Optional.of(carePlanModel));
            Mockito.when(dtoMapper.mapCarePlanModel(carePlanModel)).thenReturn(carePlanDto);

            // Act
            ResponseEntity<CarePlanDto> result = subject.getActiveCarePlan();

            // Assert
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(carePlanDto, result.getBody());
        }

        @Test
        public void getActiveCarePlan_carePlanMissing_404() throws Exception {
            // Arrange
            String cpr = "0101010101";
            Mockito.when(carePlanService.getActiveCarePlan(cpr)).thenReturn(Optional.empty());

            // Act

            // Assert
            assertThrows(ResourceNotFoundException.class, () -> subject.getActiveCarePlan());
        }
    */
    @Test
    public void getActiveCarePlan_failure_500() throws Exception {
        // Arrange
        String cpr = "0101010101";
        Mockito.doThrow(new ServiceException("error", ErrorKind.INTERNAL_SERVER_ERROR, ErrorDetails.INTERNAL_SERVER_ERROR)).when(carePlanService).getActiveCarePlans(cpr);

        // Act

        // Assert
        assertThrows(InternalServerErrorException.class, () -> subject.getActiveCarePlans());
    }
}