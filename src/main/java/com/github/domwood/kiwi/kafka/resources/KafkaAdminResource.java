package com.github.domwood.kiwi.kafka.resources;

import org.apache.kafka.clients.admin.AdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class KafkaAdminResource extends KafkaResource<AdminClient>{

    public KafkaAdminResource(Properties props) {
        super(props);
    }

    protected AdminClient createClient(Properties props){
        return AdminClient.create(props);
    }

    @Override
    protected void closeClient() throws Exception {
        this.client.close(10, TimeUnit.SECONDS);
    }

}
