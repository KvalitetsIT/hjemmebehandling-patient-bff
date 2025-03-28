package dk.kvalitetsit.hjemmebehandling.controller;

import dk.kvalitetsit.hjemmebehandling.constants.errors.ErrorDetails;
import dk.kvalitetsit.hjemmebehandling.controller.exception.BadRequestException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ForbiddenException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.InternalServerErrorException;
import dk.kvalitetsit.hjemmebehandling.controller.exception.ResourceNotFoundException;
import dk.kvalitetsit.hjemmebehandling.service.exception.AccessValidationException;
import dk.kvalitetsit.hjemmebehandling.service.exception.ServiceException;

import static dk.kvalitetsit.hjemmebehandling.service.exception.ErrorKind.BAD_REQUEST;

public abstract class BaseController {
    protected RuntimeException toStatusCodeException(Exception e) {
        if (e.getClass() == AccessValidationException.class) {
            return toStatusCodeException((AccessValidationException) e);
        }
        if (e.getClass() == ServiceException.class) {
            return toStatusCodeException((ServiceException) e);
        }
        throw new InternalServerErrorException(ErrorDetails.INTERNAL_SERVER_ERROR);
    }

    private RuntimeException toStatusCodeException(AccessValidationException e) {
        return new ForbiddenException(ErrorDetails.ACCESS_VIOLATION);
    }

    private RuntimeException toStatusCodeException(ServiceException e) {
        if (e.getErrorKind() == BAD_REQUEST)  return fromErrorDetails(e.getErrorDetails());
        return new InternalServerErrorException(ErrorDetails.INTERNAL_SERVER_ERROR);
    }

    private RuntimeException fromErrorDetails(ErrorDetails e) {
        return switch (e) {
            case INCOMPLETE_RESPONSE -> throw new BadRequestException(e);
            case NO_ACTIVE_CAREPLAN_EXISTS, QUESTIONNAIRE_DOES_NOT_EXIST -> throw new ResourceNotFoundException(e);
            case ACCESS_VIOLATION -> throw new ForbiddenException(e);
            default -> new InternalServerErrorException(e);
        };
    }
}
