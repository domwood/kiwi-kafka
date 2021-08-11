package com.github.domwood.kiwi.testutils;

import com.google.common.collect.ImmutableMap;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TestKafkaServer implements BeforeAllCallback, AfterAllCallback {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KafkaServerStartable kafkaServer;
    private TestingServer zookeeper;

    public static Integer testBrokerId = 46;
    public static String kafkaHost = "localhost";
    public static Integer kafkaPort = null;
    public static Integer zookeeperPort = null;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        zookeeperPort = randomAddress();
        Map<String, Object> extraProps = ImmutableMap.of("admin.enableServer", "false");
        InstanceSpec spec = new InstanceSpec(getTemporaryFile("zookeeper-tmp"), zookeeperPort, -1, -1, true, -1, -1, -1, extraProps, null);

        zookeeper = new TestingServer(spec, true);
        zookeeper.start();

        logger.info("Started Test Zookeeper server on " + zookeeper.getConnectString());

        Map<String, String> kafkaServerConfig = new HashMap<>();
        kafkaPort = randomAddress();

        kafkaServerConfig.put("log.dir", getTemporaryFile("kafka-tmp").getPath());
        kafkaServerConfig.put("auto.create.topics.enable", "false");
        kafkaServerConfig.put("port", kafkaPort.toString());
        kafkaServerConfig.put("host.name","localhost");
        kafkaServerConfig.put("advertised.host.name",kafkaHost);
        kafkaServerConfig.put("zookeeper.connect", zookeeper.getConnectString());
        kafkaServerConfig.put("offsets.topic.replication.factor", "1");
        kafkaServerConfig.put("min.insync.replicas", "1");
        kafkaServerConfig.put("default.replication.factor", "1");
        kafkaServerConfig.put("broker.id", String.valueOf(testBrokerId));

        KafkaConfig serverConfig = new KafkaConfig(kafkaServerConfig);

        String kafkaAddress = formatAddress(kafkaHost, kafkaPort);
        logger.info("Starting Test Kafka server on " + kafkaAddress);

        kafkaServer = new KafkaServerStartable(serverConfig);
        kafkaServer.startup();

        System.setProperty("kafka.base.client.bootstrapServers", formatAddress(kafkaHost, kafkaPort));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws IOException {
        logger.info("Shutting down test kafka server");

        kafkaServer.shutdown();
        kafkaServer.awaitShutdown();

        logger.info("Shutting down test zookeeper server");

        zookeeper.stop();
    }

    private static File getTemporaryFile(String name) throws IOException {
        File tempFolder = Files.createTempDirectory(name).toFile();
        tempFolder.deleteOnExit();
        return tempFolder;
    }

    private static Integer randomAddress() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private String formatAddress(String hostname, Integer port){
        return String.format("%s:%s", hostname, port);
    }

}
