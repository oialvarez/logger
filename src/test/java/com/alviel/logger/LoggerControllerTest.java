package com.alviel.logger;

import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.test.context.ContextConfiguration;

import static io.restassured.RestAssured.given;

@RunWith(JUnitPlatform.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = LoggerApplication.class)
class LoggerControllerTest {
    private static final String CONTENT_TYPE = "application/json";
    public static final String APPLICATION_VND_ERROR_JSON = "application/vnd.error+json";
    public static final long EXISTING_HASHTAG_ID = 10L;
    public static final long NEW_HASHTAG_ID = 1L;

    @LocalServerPort
    protected int serverPort;

    @BeforeEach
    public void init() {
        RestAssured.port = serverPort;
    }

    @Test
    void create_requiredAttributesNotFound_exceptionThrow() {
        VndErrors vndErrors = given()
                .when()
                .body(givenLoggerWithoutAttributes())
                .contentType(CONTENT_TYPE)
                .post("/logs")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(APPLICATION_VND_ERROR_JSON)
                .extract()
                .as(VndErrors.class);
        assertErrorResponse(vndErrors,
                "Origin is required", "Details is required", "Hashtags are required");
    }

    @Test
    void create_noHastagFound_shouldAddIt() {
        LogExposure newLog = given()
                .when()
                .body(givenLogger("#newhashtag"))
                .contentType(CONTENT_TYPE)
                .post("/logs")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(CONTENT_TYPE)
                .extract()
                .as(LogExposure.class);
        Assertions.assertThat(newLog.getHashtags().get(0)).isEqualTo("#newhashtag");
    }

    @Test
    void create_hashtagFound_shouldReuseIt() {
        LogExposure newLog = given()
                .when()
                .body(givenLogger("#users"))
                .contentType(CONTENT_TYPE)
                .post("/logs")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(CONTENT_TYPE)
                .extract()
                .as(LogExposure.class);
        Assertions.assertThat(newLog.getHashtags().get(0)).isEqualTo("#users");
    }

    private LogExposure givenLogger(String hashtag) {
        LogExposure request = new LogExposure();
        request.setHost("http://host");
        request.setOrigin("origin");
        request.setDetails("details");
        request.setStacktrace("stacktrace");
        List<String> hashtags = new ArrayList<>();
        hashtags.add(hashtag);
        request.setHashtags(hashtags);
        return request;
    }

    private LogExposure givenLoggerWithoutAttributes() {
        return new LogExposure();
    }

    private void assertErrorResponse(VndErrors vndErrors, String... expectedMessage) {
        Assertions.assertThat(vndErrors)
                .extracting("logref")
                .isNotEmpty();
        Assertions.assertThat(vndErrors)
                .extracting("message")
                .containsOnlyElementsOf(Arrays.asList(expectedMessage));
    }

    @Test
    void list_whenFound_shouldSendResult() {
        LogList list = given()
                .when()
                .contentType(CONTENT_TYPE)
                .get("/logs")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(CONTENT_TYPE)
                .extract()
                .as(LogList.class);
        Assertions.assertThat(list.getLogs().size()).isGreaterThan(0);
    }

}