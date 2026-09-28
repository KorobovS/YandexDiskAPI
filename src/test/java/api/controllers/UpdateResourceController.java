package api.controllers;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static api.utils.Constants.BASE_URL;
import static api.utils.Constants.TOKEN;
import static io.restassured.RestAssured.given;

public class UpdateResourceController {

    private final RequestSpecification baseSpec;

    public UpdateResourceController() {
        this.baseSpec = given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + TOKEN);
    }

    @Step("Обновляю пользовательские свойства ресурса: path={path}, customProperties={customProperties}")
    public Response updateCustomProperties(String path, Map<String, String> customProperties) {
        return given(baseSpec)
                .queryParam("path", path)
                .body(Map.of("custom_properties", customProperties))
                .patch("/v1/disk/resources");
    }

    @Step("Обновляю пользовательские свойства ресурса с параметром fields={fields}: path={path}")
    public Response updateCustomPropertiesWithFields(String path,
                                                     Map<String, String> customProperties,
                                                     String fields) {
        return given(baseSpec)
                .queryParam("path", path)
                .queryParam("fields", fields)
                .body(Map.of("custom_properties", customProperties))
                .patch("/v1/disk/resources");
    }

    @Step("Удаляю пользовательское свойство через null: path={path}, key={key}")
    public Response deleteCustomProperty(String path, String key) {
        Map<String, String> props = new java.util.HashMap<>();
        props.put(key, null);

        return given(baseSpec)
                .queryParam("path", path)
                .body(Map.of("custom_properties", Map.of(key, props)))
                .patch("/v1/disk/resources");
    }

    @Step("Пытаюсь обновить ресурс без авторизации: path={path}")
    public Response updateResourceUnauthorized(String path, Map<String, String> customProperties) {
        return given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth invalid_token")
                .queryParam("path", path)
                .body(Map.of("custom_properties", customProperties))
                .patch("/v1/disk/resources");
    }

    @Step("Пытаюсь обновить ресурс с кастомным токеном: {token}, path={path}")
    public Response updateResourceWithToken(String path,
                                            Map<String, String> customProperties,
                                            String token) {
        return given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + token)
                .queryParam("path", path)
                .body(Map.of("custom_properties", customProperties))
                .patch("/v1/disk/resources");
    }

    @Step("Пытаюсь обновить ресурс с невалидным body: path={path}")
    public Response updateResourceWithInvalidBody(String path, String invalidBody) {
        return given(baseSpec)
                .queryParam("path", path)
                .contentType(ContentType.JSON)
                .body(invalidBody)
                .patch("/v1/disk/resources");
    }

    @Step("Пытаюсь обновить ресурс с пустым body: path={path}")
    public Response updateResourceWithEmptyBody(String path) {
        return given(baseSpec)
                .queryParam("path", path)
                .contentType(ContentType.JSON)
                .body("{}")
                .patch("/v1/disk/resources");
    }
}