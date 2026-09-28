package api.controllers;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static api.utils.Constants.BASE_URL;
import static api.utils.Constants.TOKEN;
import static io.restassured.RestAssured.given;

public class UploadFileURLController {

    private final RequestSpecification requestSpecification;

    public UploadFileURLController() {
        this.requestSpecification = given().baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + TOKEN);
    }

    public Response getStatus(String href) {
        return given(requestSpecification).get(href);
    }

    @Step("Загружаю файл в Диск по URL: path={0}, url={1}")
    public Response uploadFile(String filePath, String fileUrl) {
        return given(requestSpecification)
                .queryParam("path", filePath)
                .queryParam("url", fileUrl)
                .post("/v1/disk/resources/upload");
    }

    @Step("Загружаю файл с параметром disable_redirects={0}")
    public Response uploadFileWithDisableRedirects(String filePath, String fileUrl, boolean disableRedirects) {
        return given(requestSpecification)
                .queryParam("path", filePath)
                .queryParam("url", fileUrl)
                .queryParam("disable_redirects", disableRedirects)
                .post("/v1/disk/resources/upload");
    }

    @Step("Загружаю файл с параметром fields={0}")
    public Response uploadFileWithFields(String filePath, String fileUrl, String fields) {
        return given(requestSpecification)
                .queryParam("path", filePath)
                .queryParam("url", fileUrl)
                .queryParam("fields", fields)
                .post("/v1/disk/resources/upload");
    }

    @Step("Пытаюсь загрузить файл без параметра path")
    public Response uploadFileWithoutPath(String fileUrl) {
        return given(requestSpecification)
                .queryParam("url", fileUrl)
                .post("/v1/disk/resources/upload");
    }

    @Step("Пытаюсь загрузить файл без параметра url")
    public Response uploadFileWithoutUrl(String filePath) {
        return given(requestSpecification)
                .queryParam("path", filePath)
                .post("/v1/disk/resources/upload");
    }

    @Step("Пытаюсь загрузить файл с невалидным URL")
    public Response uploadFileWithInvalidUrl(String filePath, String invalidUrl) {
        return given(requestSpecification)
                .queryParam("path", filePath)
                .queryParam("url", invalidUrl)
                .post("/v1/disk/resources/upload");
    }

    @Step("Пытаюсь загрузить файл с несуществующим URL")
    public Response uploadFileWithNonExistentUrl(String filePath, String nonExistentUrl) {
        return given(requestSpecification)
                .queryParam("path", filePath)
                .queryParam("url", nonExistentUrl)
                .post("/v1/disk/resources/upload");
    }

    @Step("Пытаюсь загрузить файл с кастомным токеном: {token}")
    public Response uploadFileToken(String token, String filePath, String fileUrl) {
        return given()
                .baseUri(BASE_URL)
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .header("authorization", "OAuth " + token)
                .queryParam("path", filePath)
                .queryParam("url", fileUrl)
                .post("/v1/disk/resources/upload");
    }
}