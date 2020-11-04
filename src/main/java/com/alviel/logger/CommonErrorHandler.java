package com.alviel.logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class CommonErrorHandler {
    @ExceptionHandler({ BadRequestException.class })
    public ResponseEntity<VndErrors> handle(BadRequestException e) {
        return getVndErrorsResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VndErrors> handle(MethodArgumentNotValidException e) {
        ValidationError error = fromBindingErrors(e.getBindingResult());
        return getVndErrorsResponseEntity(error, BAD_REQUEST);
    }

    ValidationError fromBindingErrors(Errors errors) {
        ValidationError error = new ValidationError(
                "Validation failed. " + errors.getErrorCount() + " error(s)");
        for (ObjectError objectError : errors.getAllErrors()) {
            error.addValidationError(objectError.getDefaultMessage());
        }
        return error;
    }

    @ExceptionHandler({ ResourceNotFoundException.class })
    public ResponseEntity<VndErrors> handle(ResourceNotFoundException e) {
        return getVndErrorsResponseEntity(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<VndErrors> handle(Exception e) {
        return getVndErrorsResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<VndErrors> getVndErrorsResponseEntity(Throwable e, HttpStatus status) {
        List<VndErrors.VndError> vndErrors = new ArrayList<>();
        vndErrors.add(new VndErrors.VndError(UUID.randomUUID().toString(), e.getMessage()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.error+json"));
        return new ResponseEntity<>(new VndErrors(vndErrors), headers, status);
    }

    private ResponseEntity<VndErrors> getVndErrorsResponseEntity(ValidationError error,
                                                                 HttpStatus status) {
        List<VndErrors.VndError> vndErrors = new ArrayList<>();
        error.getErrors().stream()
                .map(message -> new VndErrors.VndError(UUID.randomUUID().toString(), message))
                .forEach(vndErrors::add);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.error+json"));
        return new ResponseEntity<>(new VndErrors(vndErrors), headers, status);
    }

    static class ValidationError {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<String> errors = new ArrayList<>();

        private final String errorMessage;

        public ValidationError(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public void addValidationError(String error) {
            errors.add(error);
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

}
