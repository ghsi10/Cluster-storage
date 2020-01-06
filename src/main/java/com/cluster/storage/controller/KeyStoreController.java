package com.cluster.storage.controller;

import com.cluster.storage.exceptions.KeyNotFoundException;
import com.cluster.storage.service.KeyStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("/api")
public class KeyStoreController {

    private final KeyStoreService keyStoreService;

    public KeyStoreController(KeyStoreService keyStoreService) {
        this.keyStoreService = keyStoreService;
    }

    @PostMapping("/{key}")
    public void storeKey(@PathVariable String key, @RequestBody String reqBody) {
        keyStoreService.storeKey(key, reqBody);
    }

    @GetMapping("/{key}")
    public String getKey(@PathVariable String key) throws KeyNotFoundException {
        return keyStoreService.getKey(key);
    }

    @ExceptionHandler(KeyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String keyNotFoundExceptionHandler(KeyNotFoundException e) {
        return "Key: \"" + e.getKeyName() + "\" was not found";
    }

    @ExceptionHandler(WebClientResponseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String webClientResponseExceptionHandler() {
        return "Internal server error communicating with other cluster node";
    }
}
