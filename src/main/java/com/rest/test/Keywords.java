package com.rest.test;

import java.io.IOException;
import java.util.List;
import redis.clients.jedis.Jedis;

public class Keywords {
    private final Jedis client;
    private static final String HOT_KEYS = "hots:keys";

    public Keywords(String host, int port) throws IOException {
        this.client = new Jedis(host, port);
    }

    public HotKeyword put(String key) {
        if(!this.client.exists(key)) {
            this.client.lpush(HOT_KEYS, key);
        }
        return new HotKeyword(key, this.client.incr(key));
    }

    public HotKeyword get(String key) {
        if(!this.client.exists(key)) {
            return null;
        }
        return new HotKeyword(key, this.client.incrBy(key, 0));
    }

    public String[] list() {
        return this.client.lrange(HOT_KEYS, 0, -1).toArray(new String[]{});
    }
}
