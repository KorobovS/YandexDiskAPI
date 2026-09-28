package api.controllers;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static api.utils.Constants.*;
import static io.restassured.RestAssured.given;

public class GetListFilesController {

    private final RequestSpecification requestSpecification;

    public GetListFilesController() {
        this.requestSpecification = given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + TOKEN);
    }

    @Step("Получаю список файлов с заголовком Accept: {contentType}")
    public Response getResourcesFiles(String contentType) {
        return given(requestSpecification)
                .accept(contentType)
                .get("/v1/disk/resources/files");
    }

    @Step("Получаю список файлов с параметром {parameter}={value}")
    public Response getResourcesFiles(String parameter, String value) {
        return given(requestSpecification)
                .queryParam(parameter, value)
                .get("/v1/disk/resources/files");
    }

    @Step("Получаю список файлов с параметром {parameter}={value}")
    public Response getResourcesFiles(String parameter, Long value) {
        return given(requestSpecification)
                .queryParam(parameter, value)
                .get("/v1/disk/resources/files");
    }

    @Step("Получаю список файлов с limit={limit} и offset={offset}")
    public Response getResourcesFiles(Long limit, Long offset) {
        return given(requestSpecification)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .get("/v1/disk/resources/files");
    }

    @Step("Получаю список файлов с media_type={mediaType}")
    public Response getResourcesFilesType(String mediaType) {
        return given(requestSpecification)
                .queryParam("media_type", mediaType)
                .get("/v1/disk/resources/files");
    }

    @Step("Получаю список файлов с токеном: {token}")
    public Response getResourcesFilesToken(String token) {
        return  given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + token)
                .get("/v1/disk/resources/files");
    }
}