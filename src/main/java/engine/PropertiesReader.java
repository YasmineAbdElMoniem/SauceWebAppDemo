package engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads configuration from src/test/resources/config.properties
 * (with JVM -D overrides).
 * Keys:
 * - base.url
 * - username
 * - password
 * - browser                  (chrome|firefox|edge)
 * - headless                 (true|false)
 * - incognito                (true|false)
 * - page.load.timeout.sec    (int)
 */
public class PropertiesReader {

    public final String baseUrl;
    public final String username;
    public final String password;

    public final String browser;
    public final boolean headless;
    public final boolean incognito;
    public final int pageLoadTimeoutSec;

    public PropertiesReader() throws IOException {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream("src/test/resources/config.properties")) {
            prop.load(in);
        }

        baseUrl = get("base.url", prop);
        username = get("username", prop);
        password = get("password", prop);

        browser = get("browser", prop);
        headless = Boolean.parseBoolean(get("headless", prop));
        incognito = Boolean.parseBoolean(get("incognito", prop));

        pageLoadTimeoutSec = Integer.parseInt(get("page.load.timeout.sec", prop));
    }

    private static String get(String key, Properties p) {
        String sys = System.getProperty(key);
        return (sys != null && !sys.isBlank()) ? sys : p.getProperty(key, "");
    }
}