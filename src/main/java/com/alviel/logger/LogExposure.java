package com.alviel.logger;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class LogExposure {
    private Long id;
    @URL(message = "Invalid host")
    private String host;
    @NotEmpty(message = "Origin is required")
    private String origin;
    @NotEmpty(message = "Details is required")
    private String details;
    private String stacktrace;
    @NotEmpty(message = "Hashtags are required")
    private List<String> hashtags;
}
