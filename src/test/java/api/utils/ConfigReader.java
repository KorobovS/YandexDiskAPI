package api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.out.println("[ConfigReader] Файл config.properties не найден. Будут использованы только переменные окружения.");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Ошибка при чтении файла config.properties", ex);
        }
    }

    public static String getProperty(String key) {
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);

        if ((envValue == null || envValue.trim().isEmpty()) && "api.token".equals(key)) {
            envValue = System.getenv("YANDEX_DISK_TOKEN");
        }

        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        String propValue = properties.getProperty(key);
        if (propValue == null || propValue.trim().isEmpty()) {
            throw new RuntimeException("Свойство '" + key + "' не найдено. " +
                    "Убедитесь, что оно есть в config.properties или передано как переменная окружения (" +
                    envKey + " или YANDEX_DISK_TOKEN).");
        }

        return propValue.trim();
    }
}