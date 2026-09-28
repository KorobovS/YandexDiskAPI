package api.tests;

import api.controllers.GetListFilesController;
import api.utils.EnvironmentInfoGenerator;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Stream;

import static api.utils.JsonAssert.checkSchema;

@Epic("Файлы и папки")
public class GetListFilesTest {

    private final GetListFilesController controller = new GetListFilesController();

    @BeforeAll
    public static void createFileEnvironment() {
        EnvironmentInfoGenerator.generateEnvironmentFile();
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "application/json; charset=utf-8"})
    @Feature("Получение списка файлов")
    @Story("Позитивный сценарий: проверка заголовка Accept")
    @Tag("positive")
    public void testGetListFilesAcceptHeader(String acceptHeader) {
        Response response = controller.getResourcesFiles(acceptHeader);

        Allure.step("Проверяю статус код 200", () ->
                Assertions.assertEquals(200, response.getStatusCode()));

        Allure.step("Проверка валидности JSON Schema", () ->
                checkSchema("schemas/filesAndDirectory/get_list_file.json", response));

        Allure.step("Проверяю, что список файлов получен", () ->
                Assertions.assertNotNull(response.body().jsonPath().getList("items")));
    }

    @ParameterizedTest
    @MethodSource("limitOffsetParameters")
    @Feature("Получение списка файлов")
    @Story("Позитивный сценарий: пагинация (limit и offset)")
    @Tag("positive")
    public void testGetListFilesPagination(String parameter, Long value, int expectedMinSize) {
        Response response = controller.getResourcesFiles(parameter, value);

        Allure.step("Проверяю статус код 200", () ->
                Assertions.assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что параметр " + parameter + " применен", () ->
                Assertions.assertEquals(value, response.body().jsonPath().getLong(parameter)));

        Allure.step("Проверяю размер возвращаемого списка (динамическая проверка)", () -> {
            int actualSize = response.body().jsonPath().getList("items").size();

            if ("limit".equals(parameter) && value > 0) {
                Assertions.assertTrue(actualSize <= value,
                        "Размер списка " + actualSize + " не должен превышать limit " + value);
            } else {
                Assertions.assertEquals(expectedMinSize, actualSize);
            }
        });
    }

    static Stream<Arguments> limitOffsetParameters() {
        return Stream.of(
                Arguments.of("limit", 5L, 5),
                Arguments.of("limit", 20L, 20),
                Arguments.of("offset", 0L, 20),
                Arguments.of("offset", 5L, 20)
        );
    }

    @ParameterizedTest
    @MethodSource("limitAndOffsetParameters")
    @Feature("Получение списка файлов")
    @Story("Позитивный сценарий: одновременное использование limit и offset")
    @Tag("positive")
    public void testGetListFilesWithLimitAndOffset(Long limit, Long offset) {
        Response response = controller.getResourcesFiles(limit, offset);

        Allure.step("Проверяю статус код 200", () ->
                Assertions.assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю валидность JSON Schema", () ->
                checkSchema("schemas/filesAndDirectory/get_list_file.json", response));

        Allure.step("Проверяю, что параметр limit применён корректно", () ->
                Assertions.assertEquals(limit, response.body().jsonPath().getLong("limit")));

        Allure.step("Проверяю, что параметр offset применён корректно", () ->
                Assertions.assertEquals(offset, response.body().jsonPath().getLong("offset")));

        Allure.step("Проверяю, что размер ответа не превышает limit", () -> {
            int actualSize = response.body().jsonPath().getList("items").size();
            Assertions.assertTrue(actualSize <= limit,
                    "Размер списка " + actualSize + " не должен превышать limit " + limit);
        });
    }

    static Stream<Arguments> limitAndOffsetParameters() {
        return Stream.of(
                Arguments.of(5L, 0L),
                Arguments.of(5L, 5L),
                Arguments.of(10L, 0L),
                Arguments.of(10L, 10L),
                Arguments.of(3L, 2L),
                Arguments.of(20L, 0L)
        );
    }

    @Test
    @Feature("Получение списка файлов")
    @Story("Позитивный сценарий: проверка уникальности данных при пагинации")
    @Tag("positive")
    public void testGetListFilesPaginationUniqueness() {

        Response firstPage = controller.getResourcesFiles(5L, 0L);
        List<String> firstPageNames = firstPage.body().jsonPath().getList("items.name");

        Response secondPage = controller.getResourcesFiles(5L, 5L);
        List<String> secondPageNames = secondPage.body().jsonPath().getList("items.name");

        Allure.step("Проверяю, что первая страница получена", () ->
                Assertions.assertFalse(firstPageNames.isEmpty(), "Первая страница не должна быть пустой"));

        Allure.step("Проверяю, что вторая страница получена", () ->
                Assertions.assertFalse(secondPageNames.isEmpty(), "Вторая страница не должна быть пустой"));

        Allure.step("Проверяю, что данные на страницах не пересекаются", () -> {
            List<String> intersection = new ArrayList<>(firstPageNames);
            intersection.retainAll(secondPageNames);
            Assertions.assertTrue(intersection.isEmpty(),
                    "Файлы на разных страницах не должны повторяться. Найдены общие: " + intersection);
        });
    }

    @Test
    @Feature("Получение списка файлов")
    @Story("Позитивный сценарий: проверка сортировки по имени")
    @Tag("positive")
    public void testGetListFilesSorting() {
        Response response = controller.getResourcesFiles("sort", "name");

        Allure.step("Проверяю статус код 200", () ->
                Assertions.assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю, что файлы отсортированы по имени", () -> {
            List<String> actualNames = response.body().jsonPath().getList("items.name");
            List<String> sortedNames = new ArrayList<>(actualNames);
            Collections.sort(sortedNames);

            Assertions.assertEquals(sortedNames, actualNames,
                    "Список файлов должен быть отсортирован по алфавиту");
        });
    }

    @ParameterizedTest
    @MethodSource("mediaTypeParameters")
    @Feature("Получение списка файлов")
    @Story("Позитивный сценарий: фильтрация по типу медиа (media_type)")
    @Tag("positive")
    public void testGetListFilesByMediaType(String mediaType) {
        Response response = controller.getResourcesFilesType(mediaType);

        Allure.step("Проверяю статус код 200", () ->
                Assertions.assertEquals(200, response.getStatusCode()));

        Allure.step("Проверяю валидность JSON Schema", () ->
                checkSchema("schemas/filesAndDirectory/get_list_file.json", response));

        Allure.step("Проверяю, что все файлы в ответе соответствуют запрошенному типу медиа: " + mediaType, () -> {
            List<Map<String, Object>> items = response.body().jsonPath().getList("items");
            List<String> allowedTypes = Arrays.asList(mediaType.split(","));

            for (Map<String, Object> item : items) {

                String itemMediaType = (String) item.get("media_type");
                Assertions.assertTrue(
                        allowedTypes.contains(itemMediaType),
                        "Файл '" + item.get("name") + "' имеет media_type='" + itemMediaType +
                                "', но ожидался один из: " + allowedTypes
                );
            }
        });
    }

    static Stream<Arguments> mediaTypeParameters() {
        return Stream.of(
                Arguments.of("document"),
                Arguments.of("video"),
                Arguments.of("image"),
                Arguments.of("document,video"),
                Arguments.of("document,image")
        );
    }

    @ParameterizedTest
    @MethodSource("api.utils.StaticParameters#invalidTokenParameters")
    @Feature("Получение списка файлов")
    @Story("Негативный сценарий: невалидный или отсутствующий токен")
    @Tag("negative")
    public void testGetListFilesInvalidToken(String token) {
        Response response = controller.getResourcesFilesToken(token);

        Allure.step("Проверяю статус код 401", () ->
                Assertions.assertEquals(401, response.getStatusCode()));
    }

    @Test
    @Feature("Получение списка файлов")
    @Story("Негативный сценарий: невалидный тип медиа")
    @Tag("negative")
    public void testGetListFilesInvalidMediaType() {
        Response response = controller.getResourcesFilesType("invalid_type");

        Allure.step("Проверяю статус код 400 Bad Request", () ->
                Assertions.assertEquals(400, response.getStatusCode()));

        Allure.step("Проверка валидности схемы ошибки", () ->
                checkSchema("schemas/filesAndDirectory/error.json", response));
    }
}