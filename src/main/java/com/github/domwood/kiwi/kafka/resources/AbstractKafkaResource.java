package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public abstract class AbstractKafkaResource<CLIENT> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected CLIENT client;
    protected final ImmutableMap<Object, Object> unmodifiedProperties;

    public AbstractKafkaResource(Properties props) {
        this.unmodifiedProperties = ImmutableMap.copyOf(props);
    }

    public void discard() {
        try {
            closeClient();
        } catch (Exception e) {
            logger.error("Attempted to close and admin WebSocketService resource but failed ", e);
        }
    }

    protected abstract CLIENT createClient(ImmutableMap<Object, Object> props);


    protected CLIENT getClient() {
        if (this.client == null) {
            this.client = createClient(this.unmodifiedProperties);
        }
        return this.client;
    }

    protected abstract void closeClient() throws KafkaResourceClientCloseException;

}
