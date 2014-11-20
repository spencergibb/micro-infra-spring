package com.ofg.infrastructure.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.config.client.PropertySourceLocator;
import org.springframework.cloud.config.server.SpringApplicationEnvironmentRepository;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystemLocator implements PropertySourceLocator {

    private static final Logger log = LoggerFactory.getLogger(FileSystemLocator.class);
    public static final String CIPHER_PREFIX = "{cipher}";

    private final File propertiesFolder;
    private final AppCoordinates appCoordinates;
    private final TextEncryptor encryptor;

    public FileSystemLocator(File propertiesFolder, AppCoordinates appCoordinates, TextEncryptor encryptor) {
        this.propertiesFolder = propertiesFolder;
        this.appCoordinates = appCoordinates;
        this.encryptor = encryptor;
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        final SpringApplicationEnvironmentRepository springEnv = new SpringApplicationEnvironmentRepository();
        final List<File> propertiesPath = getConfigFiles();
        log.info("Loading configuration from {}", propertiesPath);
        springEnv.setSearchLocations(toSearchLocations(propertiesPath));
        final org.springframework.cloud.config.Environment loadedEnvs = springEnv.findOne(appCoordinates.getApplicationName(), "prod", null);
        return toPropertySource(loadedEnvs);
    }

    List<File> getConfigFiles() {
        return appCoordinates.getConfigFileNames(propertiesFolder);
    }

    private String[] toSearchLocations(List<File> propertiesPath) {
        final String[] files = new String[propertiesPath.size()];
        for (int i = 0; i < propertiesPath.size(); i++) {
            files[i] = propertiesPath.get(i).getAbsolutePath();
        }
        return files;
    }

    private PropertySource<?> toPropertySource(org.springframework.cloud.config.Environment loadedEnvs) {
        CompositePropertySource composite = new CompositePropertySource(FileSystemLocator.class.getSimpleName());
        for (org.springframework.cloud.config.PropertySource source : loadedEnvs.getPropertySources()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = decrypt((Map<String, Object>) source.getSource());
            composite.addPropertySource(new MapPropertySource(source.getName(), map));
        }
        return composite;
    }

    public File getConfigDir() {
        return appCoordinates.getConfigFolder(propertiesFolder);
    }

    private Map<String, Object> decrypt(Map<String, Object> sourceMap) {
        final Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
            result.put(entry.getKey(), decryptIfEncrypted(entry.getValue()));
        }
        return result;
    }

    private Object decryptIfEncrypted(Object obj) {
        if (obj.toString().startsWith(CIPHER_PREFIX)) {
            return decrypt(obj.toString());
        } else {
            return obj;
        }
    }

    private String decrypt(String encrypted) {
        final String encryptedStr = encrypted.substring(CIPHER_PREFIX.length());
        return encryptor.decrypt(encryptedStr);
    }
}
