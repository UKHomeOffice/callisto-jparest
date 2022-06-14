package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static Properties ctsConfiguration;
    private static Properties tempCredentialProperties = new Properties();
    private static Properties localProperties = new Properties();

    private Configuration() {
    }

    public static String get(String string) {
        return ctsConfiguration.getProperty(string);
    }

    public static String readCredentials(String propertyFile, String property) {
        tempCredentialProperties.clear();

        Properties vaultConfig;
        try {
            InputStream propertyFileInputStream = getClassPathResourceStream(propertyFile);

            try {
                loadProperties(tempCredentialProperties, propertyFileInputStream);
            } catch (Throwable var12) {
                if (propertyFileInputStream != null) {
                    try {
                        propertyFileInputStream.close();
                    } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                    }
                }

                throw var12;
            }

            if (propertyFileInputStream != null) {
                propertyFileInputStream.close();
            }
        } catch (IOException var13) {
            LOGGER.error("Could not load credentials properties file" + var13.getMessage());
            throw new RuntimeException("Could not load credentials properties file", var13);
        } finally {
            vaultConfig = tempCredentialProperties;
        }

        return vaultConfig.getProperty(property);
    }

    public static void initConfig() {
        String chartName = (String)Optional.ofNullable(System.getenv("CHART_NAME")).orElse("default");
        String propertyFile = "properties/" + chartName + ".properties";

        try {
            InputStream propertyFileInputStream = getClassPathResourceStream(propertyFile);

            try {
                loadProperties(localProperties, propertyFileInputStream);
            } catch (Throwable var11) {
                if (propertyFileInputStream != null) {
                    try {
                        propertyFileInputStream.close();
                    } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                    }
                }

                throw var11;
            }

            if (propertyFileInputStream != null) {
                propertyFileInputStream.close();
            }
        } catch (IOException var12) {
            LOGGER.error("Could not load properties" + var12.getMessage());
            throw new RuntimeException("Could not load properties", var12);
        } finally {
            ctsConfiguration = localProperties;
        }

    }

    private static void loadProperties(Properties props, InputStream inputStream) {
        try {
            props.load(inputStream);
        } catch (IOException var3) {
            LOGGER.error(var3.getMessage());
        }

    }

    public static InputStream getClassPathResourceStream(String classpathResourceLoc) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResourceLoc);
    }

    public static void setConfigurationProperty(String key, String value) {
        ctsConfiguration.setProperty(key, value);
    }

    static {
        initConfig();
    }
}
