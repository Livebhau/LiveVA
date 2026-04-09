package com.herovilleger.liveva.config;

public class Feature {
    private boolean active;
    private final String configKey;

    public Feature(String configKey, boolean defaultValue) {
        this.configKey = configKey;
        this.active = defaultValue;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        com.herovilleger.liveva.LiveModClient.saveConfig();
    }

    public String getConfigKey() {
        return configKey;
    }
}