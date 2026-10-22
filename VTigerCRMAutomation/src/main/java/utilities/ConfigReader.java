package utilities;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static Properties props = new Properties();

    static {
        try {
            InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties");
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key).trim();
    }
}
