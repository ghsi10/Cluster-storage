package com.cluster.storage.service;


import com.cluster.storage.exceptions.KeyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KeyStoreService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Configuration
public class KeyStoreServiceTest {

    @Value("#{'${cluster.nodes}'.split(',')}")
    private String[] nodes;
    @SpyBean
    private KeyStoreService keyStoreService;
    @MockBean
    private WebClient.Builder webClientBuilder;
    private static final String val = "{ \"bloop\": \"0\" }";
    private static final String key = "random";

    @Test
    public void testKeyStore() throws Exception {
        keyStoreService.storeKey(key, val);

        Assert.assertEquals("Local KeyStore did not return the stored value.",
                val, keyStoreService.getKey(key));
    }

    @Test
    public void testLocalKeyStore() throws Exception {
        keyStoreService.storeKey(key, val);
        Assert.assertEquals("Local KeyStore did not return the stored value.",
                val, keyStoreService.getLocalKey(key));
    }

    @Test(expected = KeyNotFoundException.class)
    public void testLocalKeyStoreNonExistentKey() throws Exception {
        keyStoreService.getLocalKey(key);
    }

    @Test
    public void testCalculateNodeFirstNodeKey() {
        final String firstNodeKey = "random";
        Assert.assertEquals("should return first node for the given key",
                0, keyStoreService.calcNodeId(firstNodeKey));
    }

    @Test
    public void testCalculateNodeSecondNodeKey() {
        final String secondNodeKey = "idan";
        Assert.assertEquals("should return first node for the given key",
                1, keyStoreService.calcNodeId(secondNodeKey));
    }

    @Test
    public void testRemoteKeyStoreGetKey() throws Exception {
        // this key gives a negative hash which does not belong to this configuration
        final String negativeHashKey = "idan";
        assert negativeHashKey.hashCode() > 0;
        doReturn(val).when(keyStoreService).getRemoteKey(anyString(), anyInt());
        Assert.assertEquals("Remote KeyStore did not return the stored value.",
                val, keyStoreService.getKey(negativeHashKey));
    }

    @Test
    public void testRemoteKeyStoreSetKey() throws Exception {
        // this key gives a negative hash which does not belong to this configuration
        final String negativeHashKey = "idan";
        assert negativeHashKey.hashCode() > 0;
        doNothing().when(keyStoreService).storeRemoteKey(anyString(), anyString(), anyInt());
        doReturn(val).when(keyStoreService).getRemoteKey(anyString(), anyInt());
        keyStoreService.storeKey(negativeHashKey, val);
        Assert.assertEquals("Local KeyStore did not return the stored value.",
                val, keyStoreService.getRemoteKey(key, 0));
    }

    @Test(expected = WebClientResponseException.class)
    public void testRemoteNodeException() throws Exception {
        doThrow(WebClientResponseException.class).when(keyStoreService).getRemoteKey(anyString(), anyInt());
        keyStoreService.getRemoteKey("randomKey", 0);
    }
}
