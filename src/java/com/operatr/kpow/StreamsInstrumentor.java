package com.operatr.kpow;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public class StreamsInstrumentor implements AutoCloseable {
    private final Object agent;

    public StreamsInstrumentor(Properties props) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("com.operatr.kpow.agent"));
        IFn agentFn = Clojure.var("com.operatr.kpow.agent", "init-agent");
        require.invoke(Clojure.read("com.operatr.kpow.serdes"));
        IFn serdesFn = Clojure.var("com.operatr.kpow.serdes", "transit-json-serializer");
        Serializer keySerializer = (Serializer) serdesFn.invoke();
        Serializer valSerializer = (Serializer) serdesFn.invoke();
        KafkaProducer producer = new KafkaProducer<>(props, keySerializer, valSerializer);
        agent = agentFn.invoke(producer);
    }

    public StreamsAgent register(KafkaStreams streams, Topology topology) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("com.operatr.kpow.agent"));
        IFn registerFn = Clojure.var("com.operatr.kpow.agent", "register");
        String id = (String) registerFn.invoke(agent, streams, topology);
        if (id != null) {
            return new StreamsAgent(id);
        } else {
            return null;
        }
    }

    public void unregister(StreamsAgent agent) {
        if (agent != null) {
            IFn require = Clojure.var("clojure.core", "require");
            require.invoke(Clojure.read("com.operatr.kpow.agent"));
            IFn unregisterFn = Clojure.var("com.operatr.kpow.agent", "unregister");
            unregisterFn.invoke(agent.getId());
        }
    }

    @Override
    public void close() throws Exception {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("com.operatr.kpow.agent"));
        IFn closeFn = Clojure.var("com.operatr.kpow.agent", "close-agent");
        closeFn.invoke(agent);
    }
}