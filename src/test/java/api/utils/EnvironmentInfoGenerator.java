package api.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class EnvironmentInfoGenerator {

    public static void generateEnvironmentFile() {
        Properties properties = new Properties();

        try {
            Response response = RestAssured.given()
                    .accept(ContentType.JSON)
                    .get(Constants.BASE_URL);

            if (response.getStatusCode() == 200) {
                String apiVersion = response.jsonPath().getString("api_version");
                String build = response.jsonPath().getString("build");
                properties.setProperty("API.version", apiVersion);
                properties.setProperty("API.build", build);
            }
        } catch (Exception e) {
            System.err.println("Не удалось получить информацию из API: " + e.getMessage());
            properties.setProperty("API.version", "***");
            properties.setProperty("API.build", "***");
        }

        properties.setProperty("Test.Framework", "JUnit 5");
        properties.setProperty("HTTP.Client", "REST Assured 5.5.0");
        properties.setProperty("Report.Tool", "Allure 2.29.1");
        properties.setProperty("Build.Tool", "Maven");
        properties.setProperty("Java.Version", System.getProperty("java.version"));

        try {
            Path allureResultsDir = Paths.get("target/allure-results");
            Files.createDirectories(allureResultsDir);

            Path envFile = allureResultsDir.resolve("environment.properties");
            try (OutputStream output = new FileOutputStream(envFile.toFile())) {
                properties.store(output, "Yandex Disk API Test Environment");
            }

            System.out.println("✓ Файл environment.properties успешно создан в target/allure-results/");

        } catch (IOException e) {
            System.err.println("Ошибка при создании environment.properties: " + e.getMessage());
        }
    }
}
