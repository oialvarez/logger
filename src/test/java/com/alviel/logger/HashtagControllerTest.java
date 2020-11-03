package com.alviel.logger;

import io.restassured.RestAssured;
import java.util.Collections;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.test.context.ContextConfiguration;

import static io.restassured.RestAssured.given;

@RunWith(JUnitPlatform.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = LoggerApplication.class)
class HashtagControllerTest {
    private static final String CONTENT_TYPE = "application/json";
    public static final String APPLICATION_VND_ERROR_JSON = "application/vnd.error+json";

    @LocalServerPort
    protected int serverPort;

    @MockBean
    private HashtagRepository hashtagRepository;

    @BeforeEach
    public void init() {
        RestAssured.port = serverPort;
    }

    @Test
    void update_idsAreDifferent_exceptionThrow() {
        VndErrors vndErrors = given()
                .when()
                .body(givenHastag(100L, null))
                .contentType(CONTENT_TYPE)
                .put("/hashtags/1")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(APPLICATION_VND_ERROR_JSON)
                .extract()
                .as(VndErrors.class);
        assertErrorResponse(vndErrors, "Ambiguous hashtagId");
    }

    @Test
    void update_noDescriptionSent_exceptionThrow() {
        VndErrors vndErrors = given()
                .when()
                .body(givenHastag(1L, null))
                .contentType(CONTENT_TYPE)
                .put("/hashtags/1")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(APPLICATION_VND_ERROR_JSON)
                .extract()
                .as(VndErrors.class);
        assertErrorResponse(vndErrors, "Description not provided");
    }

    @Test
    void update_noResourceFound_exceptionThrow() {
        Mockito.when(hashtagRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        VndErrors vndErrors = given()
                .when()
                .body(givenHastag(1L, "desc"))
                .contentType(CONTENT_TYPE)
                .put("/hashtags/1")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .contentType(APPLICATION_VND_ERROR_JSON)
                .extract()
                .as(VndErrors.class);
        assertErrorResponse(vndErrors, "Hashtag not found");
    }

    @Test
    void update_cantUpdate_exceptionThrow() {
        Mockito.when(hashtagRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(givenHastag(1, "lala")));
        Mockito.when(hashtagRepository.save(Mockito.any()))
                .thenThrow(new DataAccessResourceFailureException("db error"));
        VndErrors vndErrors = given()
                .when()
                .body(givenHastag(1L, "desc"))
                .contentType(CONTENT_TYPE)
                .put("/hashtags/1")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .contentType(APPLICATION_VND_ERROR_JSON)
                .extract()
                .as(VndErrors.class);
        assertErrorResponse(vndErrors, "db error");
    }

    @Test
    void update_success_returnResult() {
        Mockito.when(hashtagRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(givenHastag(1, "lala")));
        String expectedDesc = "desc";
        Mockito.when(hashtagRepository.save(Mockito.any()))
                .thenReturn(givenHastag(1, expectedDesc));
        Hashtag desc = given()
                .when()
                .body(givenHastag(1L, expectedDesc))
                .contentType(CONTENT_TYPE)
                .put("/hashtags/1")
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(CONTENT_TYPE)
                .extract()
                .as(Hashtag.class);
        Assertions.assertThat(desc.getDescription()).isEqualTo(expectedDesc);
    }

    private void assertErrorResponse(VndErrors vndErrors, String expectedMessage) {
        Assertions.assertThat(vndErrors)
                .extracting("logref")
                .isNotEmpty();
        Assertions.assertThat(vndErrors)
                .extracting("message")
                .containsOnlyElementsOf(Collections.singletonList(expectedMessage));
    }

    private Hashtag givenHastag(long hastagId, String description) {
        Hashtag hashtag = new Hashtag();
        hashtag.setId(hastagId);
        hashtag.setDescription(description);
        return hashtag;
    }
}