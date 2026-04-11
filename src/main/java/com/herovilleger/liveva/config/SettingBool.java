package com.herovilleger.liveva.config;

public class SettingBool {
    private boolean val;
    private final boolean defaultVal;

    public SettingBool(boolean defaultVal) {
        this.val = defaultVal;
        this.defaultVal = defaultVal;
    }

    public boolean value() {
        return val;
    }

    public void set(boolean val) {
        this.val = val;
    }

    public void reset() {
        this.val = defaultVal;
    }
}