package com.envyful.api.database.impl;

import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.type.RedisDatabaseDetails;
import com.envyful.api.database.Database;
import com.envyful.api.database.impl.redis.Subscribe;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SimpleLettuceDatabase implements Database {

    private final RedisClient pool;
    private final RedisURI uri;
    private final StatefulRedisPubSubConnection<String, String> subscribeConnection;
    private final StatefulRedisPubSubConnection<String, String> publishConnection;
    private final Map<String, List<BiConsumer<String, String>>> subscriptions = Maps.newConcurrentMap();

    public SimpleLettuceDatabase(RedisDatabaseDetails details) {
        this(details.getIp(), details.getPort(), details.getPassword());
    }

    public SimpleLettuceDatabase(String host, int port, String password) {
        this.uri = RedisURI.builder().withHost(host).withPort(port).withPassword(password).build();
        this.pool = RedisClient.create(this.uri);
        this.subscribeConnection = pool.connectPubSub();
        this.publishConnection = pool.connectPubSub();

        subscribeConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                for (BiConsumer<String, String> handler : subscriptions.getOrDefault(channel, Collections.emptyList())) {
                    handler.accept(channel, message);
                }
            }
        });
    }

    @Override
    public StatefulRedisConnection<String, String> getRedis() throws UnsupportedOperationException {
        return this.pool.connect();
    }

    @Override
    public RedisClient getClient() throws UnsupportedOperationException {
        return this.pool;
    }

    @Override
    public RedisURI getURI() throws UnsupportedOperationException {
        return this.uri;
    }

    @Override
    public void publish(String channel, String message) throws UnsupportedOperationException {
        this.publishConnection.async().publish(channel, message);
    }

    @Override
    public void close() {
        this.pool.close();
    }

    @Override
    public void subscribe(Object o) throws UnsupportedOperationException {
        for (Method declaredMethod : o.getClass().getDeclaredMethods()) {
            Subscribe subscribe = declaredMethod.getAnnotation(Subscribe.class);

            if (subscribe == null) {
                continue;
            }

            this.subscribeConnection.async().subscribe(subscribe.value());

            for (String s : subscribe.value()) {
                this.subscriptions.computeIfAbsent(s, ___ -> Lists.newArrayList())
                        .add((channel, message) -> {
                            try {
                                declaredMethod.invoke(o, channel, message);
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                UtilLogger.getLogger().error("Jedis error in '{}' for '{}'", channel, message);
                                e.printStackTrace();
                            }
                        });
            }
        }
    }
}
