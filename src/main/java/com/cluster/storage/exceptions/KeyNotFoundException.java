package com.cluster.storage.exceptions;

public class KeyNotFoundException extends Exception {

    private final String keyName;

    public KeyNotFoundException(String keyName) {
        super();
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }
}
