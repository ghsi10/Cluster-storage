package com.cluster.storage.controller;

import com.cluster.storage.exceptions.KeyNotFoundException;
import com.cluster.storage.service.KeyStoreService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(KeyStoreController.class)
public class KeyStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private KeyStoreService keyStoreService;

    @Test
    public void testStoreKey() throws Exception {
        final String key = "bloop";
        final String value = "{\"shooby\": \"poolb\"}";
        doNothing().when(keyStoreService).storeKey(key, value);
        mockMvc.perform(post("/api/{key}", key)
                .contentType(APPLICATION_JSON_UTF8)
                .content(value))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetKey() throws Exception {
        final String key = "bloop";
        final String value = "{\"shooby\": \"poolb\"}";
        doReturn(value).when(keyStoreService).getKey(key);
        mockMvc.perform(get("/api/{key}", key))
                .andExpect(status().isOk())
                .andExpect(content().string(value));
    }

    @Test
    public void testGetNonExistentKey() throws Exception {
        final String key = "bloop";
        Mockito.doThrow(KeyNotFoundException.class).when(keyStoreService).getKey(key);
        mockMvc.perform(get("/api/{key}", key))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetLocalKey() throws Exception {
        final String key = "bloop";
        final String value = "{\"shooby\": \"poolb\"}";
        doReturn(value).when(keyStoreService).getLocalKey(key);
        mockMvc.perform(get("/api/local/{key}", key))
                .andExpect(status().isOk())
                .andExpect(content().string(value));
    }

    @Test
    public void testGetNonExistentLocalKey() throws Exception {
        final String key = "random";
        doThrow(new KeyNotFoundException(key)).when(keyStoreService).getLocalKey(key);
        mockMvc.perform(get("/api/local/{key}", key))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRemoteNodeException() throws Exception {
        final String key = "random";
        doThrow(WebClientResponseException.class).when(keyStoreService).getKey(key);
        mockMvc.perform(get("/api/{key}", key))
                .andExpect(status().is5xxServerError());
    }
}
