package api.tests;

import api.controllers.DeleteFileController;
import api.controllers.GetFileController;
import api.controllers.UpdateResourceController;
import api.controllers.UploadFileURLController;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static api.utils.Constants.BASE_URL;
import static api.utils.Constants.filePath;
import static api.utils.JsonAssert.checkSchema;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Файлы и папки")
public class UpdateResourceTest {

    private final UpdateResourceController updateController = new UpdateResourceController();
    private final UploadFileURLController uploadController = new UploadFileURLController();
    private final GetFileController getResourceController = new GetFileController();

    @BeforeEach
    public void setUp() {
        filePath = "/test_update_" + System.currentTimeMillis() + ".txt";
        String fileUrl = "https://resizer.mail.ru/p/128497d2-9c10-5e99-9dba-2e82aa2b73a9/dpr:154/AQAKmtZI_UUYZvtfnTrre-4OlKaVzdnRiYBto8727N9g2iUkwkoGh3j-ik6wrFEbuH-qyFtrtW5_DTgLnkvEuDXT2f4.jpg";

        Response uploadResponse = uploadController.uploadFile(filePath, fileUrl);
        assertEquals(202, uploadResponse.getStatusCode(), "Файл должен быть принят на загрузку");

        String href = uploadResponse.body().jsonPath().getString("href");
        waitForUploadSuccess(href, Duration.ofMinutes(2));
    }

    @AfterEach
    public void tearDown() {
        try {
            Response deleteResponse = getResourceController.getResource(filePath);
            if (deleteResponse.getStatusCode() == 200) {
                new DeleteFileController().deleteFile(filePath);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Позитивный сценарий: добавление новых custom_properties")
    @Tag("positive")
    public void testAddCustomProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("author", "Sergey");
        properties.put("department", "QA");
        properties.put("project", "YandexDisk");

        Response response = updateController.updateCustomProperties(filePath, properties);

        Allure.step("Проверяю статус код 200", () ->
                assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что custom_properties добавлены", () -> {
            Map<String, Object> customProps = response.body().jsonPath()
                    .getMap("custom_properties");

            assertEquals("Sergey", customProps.get("author"));
            assertEquals("QA", customProps.get("department"));
            assertEquals("YandexDisk", customProps.get("project"));
        });

        Allure.step("Проверяю, что основные поля ресурса не изменились", () -> {
            String actualPath = response.body().jsonPath().getString("path");
            assertTrue(actualPath.endsWith(filePath),
                    "Ожидалось, что путь закончится на " + filePath + ", но получен: " + actualPath);
            assertEquals("file", response.body().jsonPath().getString("type"));
        });
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Позитивный сценарий: перезапись существующих custom_properties")
    @Tag("positive")
    public void testOverwriteCustomProperties() {
        Map<String, String> initialProps = Map.of("version", "1.0", "status", "draft");
        updateController.updateCustomProperties(filePath, initialProps);

        Map<String, String> updatedProps = Map.of("version", "2.0", "status", "published");
        Response response = updateController.updateCustomProperties(filePath, updatedProps);

        Allure.step("Проверяю статус код 200", () ->
                assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что свойства перезаписаны", () -> {
            Map<String, Object> customProps = response.body().jsonPath()
                    .getMap("custom_properties");

            assertEquals("2.0", customProps.get("version"), "version должен быть обновлён");
            assertEquals("published", customProps.get("status"), "status должен быть обновлён");
        });
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Позитивный сценарий: добавление и перезапись одновременно")
    @Tag("positive")
    public void testAddAndOverwriteCustomProperties() {
        updateController.updateCustomProperties(filePath, Map.of("existing", "value1"));

        Map<String, String> mixedProps = Map.of("existing", "value2", "new_property", "new_value");
        Response response = updateController.updateCustomProperties(filePath, mixedProps);

        Allure.step("Проверяю статус код 200", () ->
                assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что оба свойства присутствуют", () -> {
            Map<String, Object> customProps = response.body().jsonPath()
                    .getMap("custom_properties");

            assertEquals("value2", customProps.get("existing"), "existing должен быть перезаписан");
            assertEquals("new_value", customProps.get("new_property"), "new_property должен быть добавлен");
        });
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Позитивный сценарий: удаление custom_properties через null")
    @Tag("positive")
    public void testDeleteCustomPropertyWithNull() {
        Map<String, String> initialProps = new HashMap<>();
        initialProps.put("keep_this", "value1");
        initialProps.put("delete_this", "value2");
        updateController.updateCustomProperties(filePath, initialProps);

        Response response = updateController.deleteCustomProperty(filePath, "delete_this");

        Allure.step("Проверяю статус код 400", () ->
                assertEquals(400, response.getStatusCode()));
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Позитивный сценарий: обновление с параметром fields")
    @Tag("positive")
    public void testUpdateWithFields() {
        Map<String, String> properties = Map.of("tag", "important");

        Response response = updateController.updateCustomPropertiesWithFields(
                filePath, properties, "name,size,custom_properties");

        Allure.step("Проверяю статус код 200", () ->
                assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что в ответе есть запрошенные поля", () -> {
            assertNotNull(response.body().jsonPath().getString("name"), "Поле name должно присутствовать");
            assertNotNull(response.body().jsonPath().getLong("size"), "Поле size должно присутствовать");
            assertNotNull(response.body().jsonPath().getMap("custom_properties"),
                    "custom_properties должно присутствовать");
        });

        Allure.step("Проверяю, что custom_properties обновлены", () -> {
            Map<String, Object> customProps = response.body().jsonPath()
                    .getMap("custom_properties");
            assertEquals("important", customProps.get("tag"));
        });
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Позитивный сценарий: обновление нескольких свойств одновременно")
    @Tag("positive")
    public void testUpdateMultipleProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        properties.put("key3", "value3");
        properties.put("key4", "value4");
        properties.put("key5", "value5");

        Response response = updateController.updateCustomProperties(filePath, properties);

        Allure.step("Проверяю статус код 200", () ->
                assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что все свойства добавлены", () -> {
            Map<String, Object> customProps = response.body().jsonPath()
                    .getMap("custom_properties");

            assertEquals(5, customProps.size(), "Должно быть 5 свойств");
            assertEquals("value1", customProps.get("key1"));
            assertEquals("value2", customProps.get("key2"));
            assertEquals("value3", customProps.get("key3"));
            assertEquals("value4", customProps.get("key4"));
            assertEquals("value5", customProps.get("key5"));
        });
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Негативный сценарий: обновление несуществующего ресурса")
    @Tag("negative")
    public void testUpdateNonExistentResource() {
        String nonExistentPath = "/non_existent_" + System.currentTimeMillis() + ".txt";
        Map<String, String> properties = Map.of("key", "value");

        Response response = updateController.updateCustomProperties(nonExistentPath, properties);

        Allure.step("Проверяю статус код 404", () ->
                assertEquals(404, response.getStatusCode()));

        Allure.step("Проверяю схему ошибки", () ->
                checkSchema("schemas/filesAndDirectory/error.json", response));
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Негативный сценарий: обновление без авторизации")
    @Tag("negative")
    public void testUpdateResourceUnauthorized() {
        Map<String, String> properties = Map.of("key", "value");

        Response response = updateController.updateResourceUnauthorized(filePath, properties);

        Allure.step("Проверяю статус код 401", () ->
                assertEquals(401, response.getStatusCode()));
    }

    @ParameterizedTest
    @MethodSource("api.utils.StaticParameters#invalidTokenParameters")
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Негативный сценарий: невалидные токены")
    @Tag("negative")
    public void testUpdateResourceWithInvalidToken(String token) {
        Map<String, String> properties = Map.of("key", "value");

        Response response = updateController.updateResourceWithToken(filePath, properties, token);

        Allure.step("Проверяю статус код 401 для токена: '" + token + "'", () ->
                assertEquals(401, response.getStatusCode()));
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Негативный сценарий: невалидный формат body")
    @Tag("negative")
    public void testUpdateResourceWithInvalidBody() {
        String invalidBody = "not a valid json";

        Response response = updateController.updateResourceWithInvalidBody(filePath, invalidBody);

        Allure.step("Проверяю статус код 400 или 415", () ->
                assertTrue(response.getStatusCode() == 400 || response.getStatusCode() == 415,
                        "Ожидаем 400 или 415, получено: " + response.getStatusCode()));
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Негативный сценарий: пустой body")
    @Tag("negative")
    public void testUpdateResourceWithEmptyBody() {
        Response response = updateController.updateResourceWithEmptyBody(filePath);

        Allure.step("Проверяю статус код 200", () ->
                assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что в ответе вернулись данные ресурса", () ->
                assertNotNull(response.body().jsonPath().getString("name")));
    }

    @Test
    @Feature("Обновление пользовательских данных ресурса")
    @Story("Негативный сценарий: body без custom_properties")
    @Tag("negative")
    public void testUpdateResourceWithoutCustomProperties() {
        Response response = updateController.updateResourceWithInvalidBody(
                filePath, "{\"some_other_field\": \"value\"}");

        Allure.step("Проверяю статус код 200", () ->
                assertEquals(200, response.getStatusCode()));
    }

    private void waitForUploadSuccess(String href, Duration timeout) {
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

            Response statusResponse = given()
                    .baseUri(BASE_URL)
                    .header("authorization", "OAuth " + api.utils.Constants.TOKEN)
                    .get(href);
            status = statusResponse.body().jsonPath().getString("status");

            if (System.currentTimeMillis() > deadline) {
                fail("Операция загрузки не завершилась за " + timeout + ". Последний статус: " + status);
            }

        } while (!"success".equals(status) && !"failed".equals(status));

        assertEquals("success", status, "Операция загрузки завершилась со статусом: " + status);
    }
}