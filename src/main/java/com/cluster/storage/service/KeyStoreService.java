package com.cluster.storage.service;

import com.cluster.storage.exceptions.KeyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KeyStoreService {

    @Value("#{'${cluster.nodes}'.split(',')}")
    private String[] nodes;
    @Value("${cluster.id}")
    private int nodeId;
    private final WebClient.Builder webClientBuilder;
    private final Map<String, String> keyValStore;

    @Autowired
    public KeyStoreService(WebClient.Builder webClientBuilder) {
        this.keyValStore = new ConcurrentHashMap<>();
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * @param key   String key to save key inside key store.
     * @param value The value to save inside the store, under the specified key param.
     */
    public void storeKey(String key, String value) {
        int nodeId = calcNodeId(key);

        if (nodeId == this.nodeId)
            keyValStore.put(key, value);
        else
            storeRemoteKey(key, value, nodeId);
    }

    /**
     * @param key String key to retrieve from local and remote key stores.
     * @return stored value under the specified key, or null if key was not found.
     */
    public String getKey(String key) throws KeyNotFoundException {
        int nodeId = calcNodeId(key);
        return nodeId == this.nodeId ? getLocalKey(key) : getRemoteKey(key, nodeId);
    }

    /**
     * @param key String key to retrieve from the local key store.
     * @return stored value under the specified key.
     * @throws KeyNotFoundException if the specified key was not found.
     */
    public String getLocalKey(String key) throws KeyNotFoundException {
        String val = keyValStore.get(key);
        if (val == null)
            throw new KeyNotFoundException(key);
        return val;
    }

    /**
     * @param key String key to retrieve from the remote key store.
     * @return The value of the given key.
     * @throws KeyNotFoundException if key was not found.
     */
    String getRemoteKey(String key, int remoteNodeId) throws KeyNotFoundException {
        // build new client for every request to enable concurrency.
        return webClientBuilder.build()
                .get()
                .uri("http://" + nodes[remoteNodeId] + "/api/local/" + key)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, (resp) -> Mono.error(new KeyNotFoundException(key)))
                .bodyToMono(String.class)
                .block();
    }

    /**
     * @param key String key to store in the remote key store.
     */
    void storeRemoteKey(String key, String value, int remoteNodeId) {
        // build new client for every request to enable concurrency.
        webClientBuilder.build()
                .post()
                .uri("http://" + nodes[remoteNodeId] + "/api/" + key)
                .body(BodyInserters.fromObject(value))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    /**
     * @param key the key to calculate its shard id.
     * @return the id of the node in the cluster that the supplied key belongs to.
     */
    int calcNodeId(String key) {
        return (int) ((key.hashCode() + (long) Integer.MAX_VALUE) / ((Integer.MAX_VALUE) * 2L / nodes.length));
    }
}
