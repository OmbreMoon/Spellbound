package com.ombremoon.spellbound.client;

import com.ombremoon.spellbound.client.shader.Examples;

public class ClientStuff {
    private static ClientStuff instance;
    private final Examples examples;

    public static ClientStuff getInstance() {
        if (instance == null) {
            instance = new ClientStuff();
        }
        return instance;
    }

    public ClientStuff() {
        this.examples = new Examples();
    }

    public Examples getExamples() {
        return this.examples;
    }
}
