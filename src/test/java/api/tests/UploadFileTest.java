package api.tests;

import api.controllers.DeleteFileController;
import api.controllers.UploadFileURLController;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static api.utils.JsonAssert.checkSchema;

@Epic("Файлы и папки")
public class UploadFileTest {

    private final UploadFileURLController controller = new UploadFileURLController();
    private static String uploadedFilePath;

    @AfterAll
    public static void cleanup() {
        if (uploadedFilePath != null) {
            Allure.step("Очистка тестовых файлов", () -> {
                new DeleteFileController().deleteFile(uploadedFilePath);
            });
        }
    }

    @Test
    @Feature("Загрузка файла по URL")
    @Story("Позитивный сценарий: загрузка изображения")
    @Tag("positive")
    public void testUploadImageFile() {

        uploadedFilePath = "/test_upload_" + System.currentTimeMillis() + ".jpg";
        String fileUrl = "https://resizer.mail.ru/p/128497d2-9c10-5e99-9dba-2e82aa2b73a9/dpr:154/AQAKmtZI_UUYZvtfnTrre-4OlKaVzdnRiYBto8727N9g2iUkwkoGh3j-ik6wrFEbuH-qyFtrtW5_DTgLnkvEuDXT2f4.jpg";

        Response response = Allure.step("Загружаю файл асинхронно", () ->
                controller.uploadFile(uploadedFilePath, fileUrl)
        );

        Allure.step("Проверяю статус код 202", () ->
                Assertions.assertEquals(202, response.getStatusCode())
        );

        Allure.step("Проверяю схему ответа", () ->
                checkSchema("schemas/filesAndDirectory/upload_file.json", response)
        );

        String href = response.body().jsonPath().getString("href");
        Allure.step("В ответе должна быть ссылка на операцию", () ->
                Assertions.assertNotNull(href, "href отсутствует в ответе")
        );

        waitForOperationSuccess(href, Duration.ofMinutes(3));
    }

    @Test
    @Feature("Загрузка файла по URL")
    @Story("Позитивный сценарий: загрузка с параметром fields")
    @Tag("positive")
    public void testUploadFileWithFields() {
        String filePath = "/test_fields_" + System.currentTimeMillis() + ".txt";
        String fileUrl = "https://httpbin.org/bytes/100";

        Response response = controller.uploadFileWithFields(filePath, fileUrl, "name,size,type");

        Allure.step("Проверяю статус код 202", () ->
                Assertions.assertEquals(202, response.getStatusCode())
        );

        String href = response.body().jsonPath().getString("href");
        Allure.step("В ответе должна быть ссылка на операцию", () ->
                Assertions.assertNotNull(href)
        );

        waitForOperationSuccess(href, Duration.ofMinutes(2));
    }

    @Test
    @Feature("Загрузка файла по URL")
    @Story("Позитивный сценарий: загрузка с disable_redirects=true")
    @Tag("positive")
    public void testUploadFileWithDisableRedirects() {
        String filePath = "/test_redirects_" + System.currentTimeMillis() + ".txt";
        String fileUrl = "https://httpbin.org/bytes/50";

        Response response = controller.uploadFileWithDisableRedirects(filePath, fileUrl, true);

        Allure.step("Проверяю статус код 202", () ->
                Assertions.assertEquals(202, response.getStatusCode())
        );

        String href = response.body().jsonPath().getString("href");
        Allure.step("В ответе должна быть ссылка на операцию", () ->
                Assertions.assertNotNull(href)
        );

        waitForOperationSuccess(href, Duration.ofMinutes(2));
    }

    @ParameterizedTest
    @MethodSource("missingParameters")
    @Feature("Загрузка файла по URL")
    @Story("Негативный сценарий: отсутствие обязательных параметров")
    @Tag("negative")
    public void testUploadFileMissingParameters(String testDescription, Response response, int expectedStatus) {
        Allure.step("Проверяю: " + testDescription, () -> {
            Assertions.assertEquals(expectedStatus, response.getStatusCode());
            checkSchema("schemas/filesAndDirectory/error.json", response);
        });
    }

    static Stream<Arguments> missingParameters() {
        UploadFileURLController testController = new UploadFileURLController();
        return Stream.of(
                Arguments.of(
                        "Отсутствует параметр path",
                        testController.uploadFileWithoutPath("https://resizer.mail.ru/p/128497d2-9c10-5e99-9dba-2e82aa2b73a9/dpr:154/AQAKmtZI_UUYZvtfnTrre-4OlKaVzdnRiYBto8727N9g2iUkwkoGh3j-ik6wrFEbuH-qyFtrtW5_DTgLnkvEuDXT2f4.jpg"),
                        400
                ),
                Arguments.of(
                        "Отсутствует параметр url",
                        testController.uploadFileWithoutUrl("/test_file.txt"),
                        400
                )
        );
    }

    @Test
    @Feature("Загрузка файла по URL")
    @Story("Негативный сценарий: невалидный URL")
    @Tag("negative")
    public void testUploadFileWithInvalidUrl() {
        String filePath = "/test_invalid_url_" + System.currentTimeMillis() + ".txt";
        String invalidUrl = "not-a-valid-url";

        Response response = controller.uploadFileWithInvalidUrl(filePath, invalidUrl);

        Allure.step("Проверяю статус код 400", () ->
                Assertions.assertEquals(400, response.getStatusCode())
        );

        Allure.step("Проверяю схему ошибки", () ->
                checkSchema("schemas/filesAndDirectory/error.json", response)
        );
    }

    @Test
    @Feature("Загрузка файла по URL")
    @Story("Негативный сценарий: несуществующий URL")
    @Tag("negative")
    public void testUploadFileWithNonExistentUrl() {
        String filePath = "/test_nonexistent_" + System.currentTimeMillis() + ".txt";
        String nonExistentUrl = "https://this-domain-definitely-does-not-exist-12345.com/file.txt";

        Response response = controller.uploadFileWithNonExistentUrl(filePath, nonExistentUrl);

        Allure.step("Проверяю статус код", () ->
                // Может быть 400 (невалидный URL) или 202 (принято, но загрузка провалится)
                Assertions.assertTrue(
                        response.getStatusCode() == 400 || response.getStatusCode() == 202,
                        "Ожидаем 400 или 202, получили: " + response.getStatusCode()
                )
        );

        // Если вернули 202, проверяем что операция завершится с ошибкой
        if (response.getStatusCode() == 202) {
            String href = response.body().jsonPath().getString("href");
            waitForOperationFailed(href, Duration.ofMinutes(2));
        }
    }

    @ParameterizedTest
    @MethodSource("api.utils.StaticParameters#invalidTokenParameters")
    @Feature("Загрузка файла по URL")
    @Story("Негативный сценарий: неавторизованный запрос")
    @Tag("negative")
    public void testUploadFileUnauthorized(String token) {
        String filePath = "/test_unauthorized.txt";
        String fileUrl = "https://httpbin.org/bytes/10";

        Response response = controller.uploadFileToken(token, filePath, fileUrl);

        Allure.step("Проверяю статус код 401", () ->
                Assertions.assertEquals(401, response.getStatusCode())
        );
    }

    private void waitForOperationSuccess(String href, Duration timeout) {
        waitForOperation(href, timeout, "success");
    }

    private void waitForOperationFailed(String href, Duration timeout) {
        waitForOperation(href, timeout, "failed");
    }

    private void waitForOperation(String href, Duration timeout, String expectedStatus) {
        Duration pollInterval = Duration.ofSeconds(2);
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        String status = null;
        Response statusResponse = null;

        do {
            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Ожидание прервано", e);
            }

            statusResponse = controller.getStatus(href);
            status = statusResponse.body().jsonPath().getString("status");

            Allure.step("Текущий статус операции: " + status, () -> {
            });

            if (System.currentTimeMillis() > deadline) {
                Assertions.fail("Операция не завершилась за " + timeout + ". Последний статус: " + status);
            }

        } while (!"success".equals(status) && !"failed".equals(status));

        if ("success".equals(expectedStatus)) {
            Assertions.assertEquals("success", status,
                    "Операция завершилась со статусом: " + status);
        } else {
            Assertions.assertEquals("failed", status,
                    "Операция должна была завершиться с ошибкой");
        }
    }
}