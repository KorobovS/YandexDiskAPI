package api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Не удалось найти файл config.properties в classpath");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Ошибка при чтении файла config.properties", ex);
        }
    }

    public static String getProperty(String key) {
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }

        String propValue = properties.getProperty(key);
        if (propValue == null || propValue.trim().isEmpty()) {
            throw new RuntimeException("Свойство '" + key + "' не найдено или пустое в config.properties");
        }

        return propValue.trim();
    }
}