package com.github.domwood.kiwi.kafka.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public abstract class KafkaResource<CLIENT> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected CLIENT client;
    protected final Properties config;

    public KafkaResource(Properties props){
        this.config = props;
        this.client = null;
    }

    public CLIENT provisionResource() {
        return this.provisionResource(this.config);
    }

    public CLIENT provisionResource(Properties config) {
        if(this.client == null){
            this.client = createClient(config);
        }
        return this.client;
    }

    public void discard(){
        try{
            if(this.client != null){
                closeClient();
            }
        }
        catch (Exception e){
            logger.error("Attempted to close and admin client resource but failed ", e);
        }
        this.client = null;
    }

    protected abstract CLIENT createClient(Properties props);

    protected abstract void closeClient() throws Exception;
}
