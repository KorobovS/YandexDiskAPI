package api.tests;

import api.controllers.DeleteFileController;
import api.controllers.GetFileController;
import api.controllers.GetTrashResourcesController;
import api.controllers.UploadFileURLController;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static api.utils.Constants.filePath;
import static api.utils.JsonAssert.checkSchema;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Файлы и папки")
public class DeleteFileTest {

    private final DeleteFileController deleteController = new DeleteFileController();
    private final UploadFileURLController uploadController = new UploadFileURLController();
    private final GetFileController getFileController = new GetFileController();
    private final GetTrashResourcesController trashController = new GetTrashResourcesController();

    @BeforeEach
    public void setUp() {
        filePath = "/test_delete_" + System.currentTimeMillis() + ".txt";
        String fileUrl = "https://resizer.mail.ru/p/128497d2-9c10-5e99-9dba-2e82aa2b73a9/dpr:154/AQAKmtZI_UUYZvtfnTrre-4OlKaVzdnRiYBto8727N9g2iUkwkoGh3j-ik6wrFEbuH-qyFtrtW5_DTgLnkvEuDXT2f4.jpg";

        Response uploadResponse = uploadController.uploadFile(filePath, fileUrl);
        assertEquals(202, uploadResponse.getStatusCode(), "Файл должен быть принят на загрузку");

        String href = uploadResponse.body().jsonPath().getString("href");
        waitForUploadSuccess(href, Duration.ofMinutes(2));
    }

    @AfterEach
    public void tearDown() {
        try {
            Response trashCheck = trashController.getResource(filePath);
            if (trashCheck.getStatusCode() == 200) {
                deleteController.deleteFilePermanently(filePath);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    @Feature("Удаление файла")
    @Story("Позитивный сценарий: удаление файла в корзину")
    @Tag("positive")
    public void testDeleteFileToTrash() {
        Response response = deleteController.deleteFile(filePath);

        Allure.step("Проверяю статус код 204", () ->
                assertEquals(204, response.getStatusCode()));
    }

    @Test
    @Feature("Удаление файла")
    @Story("Позитивный сценарий: удаление файла навсегда (permanently=true)")
    @Tag("positive")
    public void testDeleteFilePermanently() {
        Response response = deleteController.deleteFilePermanently(filePath);

        Allure.step("Проверяю статус код 204", () ->
                assertEquals(204, response.getStatusCode()));

        Allure.step("Проверяю, что файл отсутствует на Диске", () -> {
            Response diskResponse = getFileController.getResource(filePath);
            assertEquals(404, diskResponse.getStatusCode());
        });

        Allure.step("Проверяю, что файл отсутствует в Корзине", () -> {
            Response trashResponse = trashController.getResource(filePath);
            assertEquals(404, trashResponse.getStatusCode(),
                    "Файл не должен попасть в Корзину при permanently=true");
        });
    }

    @Test
    @Feature("Удаление файла")
    @Story("Позитивный сценарий: асинхронное удаление (force_async=true)")
    @Tag("positive")
    public void testDeleteFileAsync() {
        Response response = deleteController.deleteFileAsync(filePath);
        int statusCode = response.getStatusCode();

        Allure.step("Проверяю статус код (202 или 204)", () ->
                assertTrue(statusCode == 202 || statusCode == 204,
                        "Ожидаем 202 (асинхронно) или 204 (синхронно), получено: " + statusCode));

        if (statusCode == 202) {
            Allure.step("Удаление выполняется асинхронно — опрашиваю статус операции", () -> {
                String href = response.body().jsonPath().getString("href");
                assertNotNull(href, "При статусе 202 в ответе должна быть ссылка на операцию");
                waitForDeleteOperationSuccess(href, Duration.ofMinutes(2));
            });
        }

        Allure.step("Проверяю, что файл удалён с Диска", () -> {
            Response diskResponse = getFileController.getResource(filePath);
            assertEquals(404, diskResponse.getStatusCode());
        });
    }

    @Test
    @Feature("Удаление файла")
    @Story("Позитивный сценарий: удаление с правильным md5")
    @Tag("positive")
    public void testDeleteFileWithCorrectMd5() {
        Response fileInfo = getFileController.getResource(filePath);
        assertEquals(200, fileInfo.getStatusCode());
        String md5 = fileInfo.body().jsonPath().getString("md5");
        assertNotNull(md5, "У файла должен быть md5");

        Response response = deleteController.deleteFileWithMd5(filePath, md5);

        Allure.step("Проверяю статус код 204", () ->
                assertEquals(204, response.getStatusCode()));

        Allure.step("Проверяю, что файл успешно удален с Диска", () -> {
            Response diskResponse = getFileController.getResource(filePath);
            assertEquals(404, diskResponse.getStatusCode());
        });
    }

    @Test
    @Feature("Удаление файла")
    @Story("Позитивный сценарий: удаление с параметром fields")
    @Tag("positive")
    public void testDeleteFileWithFields() {
        Response response = deleteController.deleteFileWithFields(filePath, "name,size,type");

        int statusCode = response.getStatusCode();
        Allure.step("Проверяю статус код (202 или 204)", () ->
                assertTrue(statusCode == 202 || statusCode == 204,
                        "Ожидаем 202 или 204, получили: " + statusCode));

        if (statusCode == 202) {
            String href = response.body().jsonPath().getString("href");
            Allure.step("При асинхронном удалении (202) проверяю наличие ссылки на операцию", () -> {
                assertNotNull(href, "Поле href должно присутствовать при статусе 202");
            });
            waitForDeleteOperationSuccess(href, Duration.ofMinutes(2));
        } else {
            Allure.step("При синхронном удалении (204) тело ответа пустое, это ожидаемое поведение", () -> {
                assertTrue(response.body().asString().isEmpty(), "Тело ответа должно быть пустым при 204");
            });
        }

        Allure.step("Проверяю, что файл удалён с Диска", () -> {
            Response diskResponse = getFileController.getResource(filePath);
            assertEquals(404, diskResponse.getStatusCode());
        });
    }

    @Test
    @Feature("Удаление файла")
    @Story("Негативный сценарий: удаление несуществующего файла")
    @Tag("negative")
    public void testDeleteNonExistentFile() {
        String nonExistentPath = "/non_existent_" + System.currentTimeMillis() + ".txt";

        Response response = deleteController.deleteFile(nonExistentPath);

        Allure.step("Проверяю статус код 404", () ->
                assertEquals(404, response.getStatusCode()));

        Allure.step("Проверяю схему ошибки", () ->
                checkSchema("schemas/filesAndDirectory/error.json", response));
    }

    @Test
    @Feature("Удаление файла")
    @Story("Негативный сценарий: неверный md5")
    @Tag("negative")
    public void testDeleteFileWithWrongMd5() {
        String wrongMd5 = "00000000000000000000000000000000";

        Response response = deleteController.deleteFileWithMd5(filePath, wrongMd5);

        Allure.step("Проверяю статус код 409 Conflict", () ->
                assertEquals(409, response.getStatusCode()));

        Allure.step("Проверяю схему ошибки", () ->
                checkSchema("schemas/filesAndDirectory/error.json", response));
    }

    @Test
    @Feature("Удаление файла")
    @Story("Негативный сценарий: удаление без авторизации")
    @Tag("negative")
    public void testDeleteFileUnauthorized() {
        Response response = deleteController.deleteFileUnauthorized(filePath);

        Allure.step("Проверяю статус код 401", () ->
                assertEquals(401, response.getStatusCode()));
    }

    @Test
    @Feature("Удаление файла")
    @Story("Негативный сценарий: повторное удаление уже удалённого файла")
    @Tag("negative")
    public void testDeleteAlreadyDeletedFile() {
        Response firstDelete = deleteController.deleteFile(filePath);
        assertEquals(204, firstDelete.getStatusCode());

        Response secondDelete = deleteController.deleteFile(filePath);

        Allure.step("Проверяю статус код 404 при повторном удалении", () ->
                assertEquals(404, secondDelete.getStatusCode()));
    }

    @ParameterizedTest
    @MethodSource("api.utils.StaticParameters#invalidTokenParameters")
    @Feature("Удаление файла")
    @Story("Негативный сценарий: невалидные токены")
    @Tag("negative")
    public void testDeleteFileWithInvalidToken(String token) {
        Response response = deleteController.deleteFileWithToken(filePath, token);

        Allure.step("Проверяю статус код 401 для токена: '" + token + "'", () ->
                assertEquals(401, response.getStatusCode()));
    }

    private void waitForUploadSuccess(String href, Duration timeout) {
        waitForOperation(href, timeout, "success");
    }

    private void waitForDeleteOperationSuccess(String href, Duration timeout) {
        waitForOperation(href, timeout, "success");
    }

    private void waitForOperation(String href, Duration timeout, String expectedStatus) {
        Duration pollInterval = Duration.ofSeconds(2);
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        String status = null;

        do {
            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Ожидание прервано", e);
            }

            Response statusResponse = deleteController.getOperationStatus(href);
            status = statusResponse.body().jsonPath().getString("status");

            Allure.step("Текущий статус операции: " + status, () -> {});

            if (System.currentTimeMillis() > deadline) {
                Allure.step("Таймаут операции", () -> {});
                fail("Операция не завершилась за " + timeout + ". Последний статус: " + status);
            }

        } while (!"success".equals(status) && !"failed".equals(status));

        assertEquals(expectedStatus, status,
                "Операция завершилась со статусом: " + status);
    }
}