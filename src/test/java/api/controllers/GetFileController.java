package api.controllers;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static api.utils.Constants.BASE_URL;
import static api.utils.Constants.TOKEN;
import static io.restassured.RestAssured.given;

public class GetFileController {

    private final RequestSpecification baseSpec;

    public GetFileController() {
        this.baseSpec = given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + TOKEN);
    }

    @Step("Получаю информацию о ресурсе: {path}")
    public Response getResource(String path) {
        return given(baseSpec)
                .queryParam("path", path)
                .get("/v1/disk/resources");
    }
}