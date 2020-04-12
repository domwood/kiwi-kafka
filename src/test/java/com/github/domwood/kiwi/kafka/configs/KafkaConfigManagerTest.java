package com.github.domwood.kiwi.kafka.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaConfigManagerTest {

    private KafkaConfigManager kafkaConfigManager;

    @BeforeEach
    public void beforeEach() {
        kafkaConfigManager = new KafkaConfigManager();
    }

    @Test
    private void notBaseConfigByDefault() {
        assertEquals(emptyMap(), kafkaConfigManager.generateAdminConfig(Optional.empty()));
        assertEquals(emptyMap(), kafkaConfigManager.generateConsumerConfig(Optional.empty()));
        assertEquals(emptyMap(), kafkaConfigManager.generateProducerConfig(Optional.empty()));
    }

    @Test
    public void testBaseConfigsForAdmin() {
        setUpBaseClientConfig("bootstrapServers", "localhost");

        Properties expected = expectedConfig("bootstrap.servers", "localhost");

        Properties observed = kafkaConfigManager.generateAdminConfig(Optional.of("anything"));

        assertEquals(expected, observed);
    }

    @Test
    public void testBaseConfigsForConsumer() {
        setUpBaseClientConfig("bootstrapServers", "localhost");

        Properties expected = expectedConfig("bootstrap.servers", "localhost");

        Properties observed = kafkaConfigManager.generateConsumerConfig(Optional.of("anything"));

        assertEquals(expected, observed);
    }

    @Test
    public void testBaseConfigsForProducer() {
        setUpBaseClientConfig("bootstrapServers", "localhost");

        Properties expected = expectedConfig("bootstrap.servers", "localhost");

        Properties observed = kafkaConfigManager.generateProducerConfig(Optional.of("anything"));

        assertEquals(expected, observed);
    }

    @Test
    public void testBaseConfigsForConsumerWithConsumerOverride() {
        setUpBaseClientConfig("bootstrapServers", "localhost");
        setUpBaseConsumerConfig("bootstrapServers", "remote:9090");

        Properties expectedAdmin = expectedConfig("bootstrap.servers", "localhost");
        Properties expectedConsumer = expectedConfig("bootstrap.servers", "remote:9090");

        Properties observedAdmin = kafkaConfigManager.generateAdminConfig(Optional.of("anything"));
        Properties observedConsumer = kafkaConfigManager.generateConsumerConfig(Optional.of("anything"));

        assertEquals(expectedAdmin, observedAdmin);
        assertEquals(expectedConsumer, observedConsumer);
    }

    @Test
    public void testClusterSpecificConfiguration() {
        setUpClusterConfiguration("test", "bootstrapServers", "localhost");

        Properties expected = expectedConfig("bootstrap.servers", "localhost");
        Properties observed = kafkaConfigManager.generateProducerConfig(Optional.of("test"));
        assertEquals(expected, observed);
    }

    @Test
    public void testClusterSpecificConfigurationForDifferentCluster() {
        setUpClusterConfiguration("test", "bootstrapServers", "localhost");

        Properties expected = new Properties();
        Properties observed = kafkaConfigManager.generateProducerConfig(Optional.of("nottest"));
        assertEquals(expected, observed);
    }

    @Test
    public void testClusterSpecificConfigurationWithBase() {
        setUpBaseConsumerConfig("bootstrapServers", "localhost");
        setUpClusterConfiguration("test", "bootstrapServers", "remote:8000");

        Properties expected = expectedConfig("bootstrap.servers", "remote:8000");
        Properties observed = kafkaConfigManager.generateConsumerConfig(Optional.of("test"));
        assertEquals(expected, observed);

    }

    @Test
    public void testClusterSpecificConfigurationWithBaseForDifferentCluster() {
        setUpBaseConsumerConfig("bootstrapServers", "localhost");
        setUpClusterConfiguration("test", "bootstrapServers", "remote:8000");

        Properties expected = expectedConfig("bootstrap.servers", "localhost");
        Properties observed = kafkaConfigManager.generateConsumerConfig(Optional.of("nottest"));
        assertEquals(expected, observed);
    }

    @Test
    public void testSpringConfigurationIsReplacedForAFewExamples() {
        HashMap<String, String> base = new HashMap<>();
        base.put("valueSerializer", "1");
        base.put("keySerializer", "2");
        base.put("SECURITYPROTOCOL", "3");
        base.put("SSLKEYSTORELOCATION", "4");
        base.put("randomstuff", "5");

        kafkaConfigManager.getBase().put("client", base);

        Properties expected = new Properties();
        expected.setProperty("value.serializer", "1");
        expected.setProperty("key.serializer", "2");
        expected.setProperty("security.protocol", "3");
        expected.setProperty("ssl.keystore.location", "4");
        expected.setProperty("randomstuff", "5");

        Properties observed = kafkaConfigManager.generateConsumerConfig(Optional.of("anything"));
        assertEquals(expected, observed);
    }

    @Test
    public void testSSLEndpointIdentificationOverride() {
        setUpBaseConsumerConfig("SSLENDPOINTIDENTIFICATIONALGORITHM", "none");

        Properties expected = expectedConfig("ssl.endpoint.identification.algorithm", "");
        Properties observed = kafkaConfigManager.generateConsumerConfig(Optional.of("anything"));
        assertEquals(expected, observed);
    }


    private void setUpClusterConfiguration(String clusterName, String key, String value) {
        HashMap<String, String> client = new HashMap<>();
        client.put(key, value);
        HashMap<String, Map<String, String>> cluster = new HashMap<>();
        cluster.put("client", client);
        kafkaConfigManager.getActiveClusterConfiguration().put(clusterName, cluster);
    }

    private void setUpBaseClientConfig(String key, String value) {
        HashMap<String, String> base = new HashMap<>();
        base.put(key, value);
        kafkaConfigManager.getBase().put("client", base);
    }

    private void setUpBaseConsumerConfig(String key, String value) {
        HashMap<String, String> consumer = new HashMap<>();
        consumer.put(key, value);
        kafkaConfigManager.getBase().put("consumer", consumer);
    }

    private Properties expectedConfig(String key, String value) {
        Properties expected = new Properties();
        expected.setProperty(key, value);
        return expected;
    }

}
