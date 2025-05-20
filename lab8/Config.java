package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    public static String HOST;
    public static int PORT;

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            if (input == null) {
                throw new IOException("Unable to find " + CONFIG_FILE);
            }
            prop.load(input);
            HOST = prop.getProperty("host");
            PORT = Integer.parseInt(prop.getProperty("port"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
