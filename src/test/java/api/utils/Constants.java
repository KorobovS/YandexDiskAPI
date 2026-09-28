package api.utils;

public abstract class Constants {

    public static final String BASE_URL = ConfigReader.getProperty("api.base.url");
    public static final String TOKEN = ConfigReader.getProperty("api.token");

    public static String filePath;
}
