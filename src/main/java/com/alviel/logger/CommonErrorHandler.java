package com.alviel.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonErrorHandler {
    @ExceptionHandler({ BadRequestException.class })
    public ResponseEntity<VndErrors> handle(BadRequestException e) {
        return getVndErrorsResponseEntity(e, HttpStatus.BAD_REQUEST);
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
        MediaType mt = MediaType.valueOf("application/vnd.error+json");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mt);
        return new ResponseEntity<>(new VndErrors(vndErrors), headers, status);
    }

}
