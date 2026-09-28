package api.controllers;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static api.utils.Constants.BASE_URL;
import static api.utils.Constants.TOKEN;
import static io.restassured.RestAssured.given;

public class DeleteFileController {

    private final RequestSpecification baseSpec;

    public DeleteFileController() {
        this.baseSpec = given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + TOKEN);
    }

    @Step("Удаляю файл в корзину: {path}")
    public Response deleteFile(String path) {
        return given(baseSpec)
                .queryParam("path", path)
                .delete("/v1/disk/resources");
    }

    @Step("Удаляю файл навсегда (permanently=true): {path}")
    public Response deleteFilePermanently(String path) {
        return given(baseSpec)
                .queryParam("path", path)
                .queryParam("permanently", true)
                .delete("/v1/disk/resources");
    }

    @Step("Удаляю файл асинхронно (force_async=true): {path}")
    public Response deleteFileAsync(String path) {
        return given(baseSpec)
                .queryParam("path", path)
                .queryParam("force_async", true)
                .delete("/v1/disk/resources");
    }

    @Step("Удаляю файл с проверкой md5: {path}, md5={md5}")
    public Response deleteFileWithMd5(String path, String md5) {
        return given(baseSpec)
                .queryParam("path", path)
                .queryParam("md5", md5)
                .delete("/v1/disk/resources");
    }

    @Step("Удаляю файл с параметром fields={fields}")
    public Response deleteFileWithFields(String path, String fields) {
        return given(baseSpec)
                .queryParam("path", path)
                .queryParam("force_async", true)
                .queryParam("fields", fields)
                .delete("/v1/disk/resources");
    }

    @Step("Пытаюсь удалить файл без авторизации: {path}")
    public Response deleteFileUnauthorized(String path) {
        return given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth invalid_token")
                .queryParam("path", path)
                .delete("/v1/disk/resources");
    }

    @Step("Пытаюсь удалить файл с кастомным токеном: {token}")
    public Response deleteFileWithToken(String path, String token) {
        return given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + token)
                .queryParam("path", path)
                .delete("/v1/disk/resources");
    }

    @Step("Проверяю статус операции удаления: {href}")
    public Response getOperationStatus(String href) {
        return given(baseSpec).get(href);
    }
}