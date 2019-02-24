package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaAdminResource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Properties adminConfig;
    private AdminClient adminClient;

    public KafkaAdminResource(Properties props){
        this.adminConfig = props;
        this.adminClient = null;
    }

    public AdminClient provisionResource() {
        return this.provisionResource(this.adminConfig);
    }

    public AdminClient provisionResource(Properties config) {
        if(this.adminClient == null){
            this.adminClient = createClient(config);
        }
        return this.adminClient;
    }

    public void discard(){
        try{
            if(this.adminClient != null){
                this.adminClient.close(20, TimeUnit.SECONDS);
            }
        }
        catch (Exception e){
            logger.error("Attempted to close and admin client resource but failed ", e);
        }
        this.adminClient = null;
    }

    private AdminClient createClient(Properties props){
        return AdminClient.create(props);
    }

}
