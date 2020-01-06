package com.cluster.storage.exceptions;

public class KeyNotFoundException extends Exception {

    private final String keyName;

    public KeyNotFoundException(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }
}
