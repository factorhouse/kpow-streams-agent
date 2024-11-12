package io.factorhouse.kpow;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import io.factorhouse.kpow.key_strategies.KeyStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.ArrayList;
import java.util.Properties;

public class StreamsRegistry implements AutoCloseable {

    public static class StreamsAgent {
        private final String _id;

        StreamsAgent(String id) {
            _id = id;
        }

        public String getId() {
            return _id;
        }
    }

    private final Object agent;

    public static Properties filterProperties(Properties props) {
        ArrayList<String> allowedKeys = new ArrayList<>();
        allowedKeys.add("ssl.enabled.protocols");
        allowedKeys.add("sasl.client.callback.handler.class");
        allowedKeys.add("ssl.endpoint.identification.algorithm");
        allowedKeys.add("ssl.provider");
        allowedKeys.add("ssl.truststore.location");
        allowedKeys.add("ssl.keystore.key");
        allowedKeys.add("ssl.key.password");
        allowedKeys.add("ssl.protocol");
        allowedKeys.add("ssl.keystore.password");
        allowedKeys.add("sasl.login.class");
        allowedKeys.add("ssl.trustmanager.algorithm");
        allowedKeys.add("ssl.keystore.location");
        allowedKeys.add("sasl.login.callback.handler.class");
        allowedKeys.add("ssl.truststore.certificates");
        allowedKeys.add("ssl.cipher.suites");
        allowedKeys.add("ssl.truststore.password");
        allowedKeys.add("ssl.keymanager.algorithm");
        allowedKeys.add("ssl.keystore.type");
        allowedKeys.add("ssl.secure.random.implementation");
        allowedKeys.add("ssl.truststore.type");
        allowedKeys.add("sasl.jaas.config");
        allowedKeys.add("ssl.keystore.certificate.chain");
        allowedKeys.add("sasl.mechanism");
        allowedKeys.add("sasl.oauthbearer.jwks.endpoint.url");
        allowedKeys.add("sasl.oauthbearer.token.endpoint.url");
        allowedKeys.add("sasl.kerberos.service.name");
        allowedKeys.add("security.protocol");
        allowedKeys.add("bootstrap.servers");

        Properties nextProps = new Properties();
        for (String key : allowedKeys) {
            if (props.containsKey(key)) {
                nextProps.setProperty(key, String.valueOf(props.get(key)));
            }
        }

        String compressionType = props.getProperty("compression.type", "gzip");
        nextProps.setProperty("compression.type", compressionType);

        String idempotence = props.getProperty("enable.idempotence", "false");
        nextProps.setProperty("enable.idempotence", idempotence);

        return nextProps;
    }

    public StreamsRegistry(Properties props, MetricFilter metricsFilter) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("io.factorhouse.kpow.agent"));
        IFn agentFn = Clojure.var("io.factorhouse.kpow.agent", "init-registry");
        require.invoke(Clojure.read("io.factorhouse.kpow.serdes"));
        IFn serdesFn = Clojure.var("io.factorhouse.kpow.serdes", "transit-json-serializer");
        Serializer keySerializer = (Serializer) serdesFn.invoke();
        Serializer valSerializer = (Serializer) serdesFn.invoke();
        Properties producerProps = filterProperties(props);
        KafkaProducer producer = new KafkaProducer<>(producerProps, keySerializer, valSerializer);
        agent = agentFn.invoke(producer, metricsFilter);
    }

    public StreamsRegistry(Properties props) {
        this(props, StreamsRegistry.defaultMetricFilter());
    }

    public static MetricFilter defaultMetricFilter() {
        return new MetricFilter()
                .acceptNameStartsWith("foo")
                .deny();
    }

    public StreamsAgent register(KafkaStreams streams, Topology topology, KeyStrategy keyStrategy) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("io.factorhouse.kpow.agent"));
        IFn registerFn = Clojure.var("io.factorhouse.kpow.agent", "register");
        String id = (String) registerFn.invoke(agent, streams, topology, keyStrategy);
        if (id != null) {
            return new StreamsAgent(id);
        } else {
            return null;
        }
    }

    public void unregister(StreamsAgent streamsAgent) {
        if (streamsAgent != null) {
            IFn require = Clojure.var("clojure.core", "require");
            require.invoke(Clojure.read("io.factorhouse.kpow.agent"));
            IFn unregisterFn = Clojure.var("io.factorhouse.kpow.agent", "unregister");
            unregisterFn.invoke(agent, streamsAgent.getId());
        }
    }

    @Override
    public void close() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("io.factorhouse.kpow.agent"));
        IFn closeFn = Clojure.var("io.factorhouse.kpow.agent", "close-registry");
        closeFn.invoke(agent);
    }
}
