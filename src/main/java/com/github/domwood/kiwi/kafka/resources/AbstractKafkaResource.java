package com.github.domwood.kiwi.kafka.resources;

import com.github.domwood.kiwi.exceptions.KafkaResourceClientCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public abstract class AbstractKafkaResource<CLIENT> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private CLIENT client;
    protected final Properties config;

    public AbstractKafkaResource(Properties props){
        this.config = props;
    }

    public void discard(){
        try{
            if(this.client != null){
                closeClient();
            }
        }
        catch (Exception e){
            logger.error("Attempted to close and admin WebSocketService resource but failed ", e);
        }
        this.client = null;
    }

    protected abstract CLIENT createClient(Properties props);


    protected CLIENT getClient(){
        if(this.client == null){
            this.client = createClient(this.config);
        }
        return this.client;
    }

    protected abstract void closeClient() throws KafkaResourceClientCloseException;

}
