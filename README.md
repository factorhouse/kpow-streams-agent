[![test](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.operatr/kpow-streams-agent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.operatr%22%20AND%20a:%22kpow-streams-agent%22)

# [kPow](https://kpow.io) Streams Agent

This repository contains the kPow Streams Agent. This agent is available in [Maven Central](https://search.maven.org/artifact/io.operatr/kpow-streams-agent).

Use this agent to integrate your Kafka Streams applications with kPow and unlock the following features:

* Visualise Kafka Streams topologies in the kPow Streams UI.
* Monitor Kafka Streams metrics (e.g Stream-Thread, State Store, RocksDB, etc).
* See summaries of Kafka Streams activity for your Kafka cluster(s).
* Expose Kafka Streams metrics in the kPow [Prometheus Endpoints](https://docs.kpow.io/features/prometheus) (for alerting, etc).
* (Soon) View kPow Insights of your Kafka Streams applications (outlier metrics, etc).

See the [kPow Kafka Streams Feature Guide](https://docs.kpow.io/features/kafka-streams) for full documentation.

---

![topology-ui](docs/topology-ui.png)

# Prerequisites

The kPow Streams Agent requires a running instance of kPow.

Evaluate kPow with the [kpow-local](https://github.com/operatr-io/kpow-local) repository or visit our [Quick Start](https://docs.kpow.io/installation/quick-start) guide.

# Installation

The kPow Stream Agent can be found on [Maven Central](https://search.maven.org/artifact/io.operatr/kpow-streams-agent):

Include the agent as a dependency in your Kafka Streams application.

```xml
<dependency>
  <groupId>io.operatr</groupId>
  <artifactId>kpow-streams-agent</artifactId>
  <version>0.2.5</version>
  <type>bundle</type>
</dependency>
```

# Integration

In your application, just before you start your KafkaStreams instance:

* Create a new [io.operatr.kpow.StreamsRegistry](https://github.com/operatr-io/kpow-streams-agent/blob/main/src/java/io/operatr/kpow/StreamsRegistry.java) instance.
* Register your KafkaStreams and Topology instances with the StreamsRegistry.

```java 
import io.operatr.kpow.StreamsRegistry;

// Your Kafka Streams topology
Topology topology = createMyTopology(); 

// Your Kafka Streams config
Properties props = new createMyStreamProperties();
 
// Your Kafka Streams instance
KafkaStreams streams = new KafkaStreams(topology, props); 

// Create a kPow StreamsRegistry
StreamsRegistry registry = new StreamsRegistry(props);

// Register your KafkaStreams and Topology instances with the StreamsRegistry
registry.register(streams, topology); 

// Start your Kafka Streams application
streams.start();
```

The StreamsRegistry is a *single-threaded process* that performs these actions **once every minute**:

* Capture metadata about each registered Kafka Streams application.
* Produce snapshots to the kpow internal `__oprtr_snapshot_state` topic.

The StreamsRegistry **does not talk directly to kPow**. kPow reads streams data from the snapshot topic.

The StreamsRegistry captures metadata about your Kafka Streams application and produces snapshots to the `__oprtr_snapshot_state` topic once every minute.

To instrument a Kafka Streams application, create a new instance of a `StreamsRegistry` and register your `KafkaStreams` + `Topology` instances against it.

Once configured, metrics will be periodically sent to kPow's internal snapshot topic. You will be able to monitor your streams application from within kPow and externally via [Prometheus egress](https://docs.kpow.io/features/prometheus)



## Deployment scenarios

The `Properties` object passed to the `StreamsRegistry` instance only requires your Kafka Connection details. This configuration is used to initialize a Kafka Producer that periodically sends streams telemetry to an internal kPow topic.

Appropriate key/value serializers will be appropriately set once the instance has been constructed.

Kafka connection fields include any of the following: `bootstrap.servers, ssl.truststore.type, ssl.truststore.password, ssl.truststore.location, ssl.truststore.certificates, ssl.trustmanager.algorithm, ssl.secure.random.implementation, ssl.provider, ssl.protocol, ssl.keystore.type, ssl.keystore.password, ssl.keystore.location, ssl.keystore.key, ssl.keystore.certificate.chain, ssl.keymanager.algorithm, ssl.key.password, ssl.endpoint.identification.algorithm, ssl.enabled.protocols, ssl.cipher.suites, security.protocol, sasl.mechanism, sasl.login.callback.handler.class, sasl.jaas.config`

For more details visit the [Producer configs](https://kafka.apache.org/documentation/#producerconfigs) section of the Apache Kafka documentation.

This section outlines different deployment scenarios and how you might want to configure your `Properties` object.

### Simple

If kPow is configured to monitor only a single Kafka cluster, you can reuse your Kafka Streams `Properties` configuration:

```java
Properties streamsProps = new Properties();
KafkaStreams streams = new KafkaStreams(topology, streamsProps); 
StreamsRegistry registry = new StreamsRegistry(streamsProps);
```

### Multi-cluster kPow + Streams Registry

The `Properties` instance you pass to `StreamsRegistry` must contain configuration details for your **primary** Kafka cluster. Your primary Kafka cluster is the cluster housing internal kPow topics like `__oprtr_snapshot_state`.

Visit [kPow's documentation](https://docs.kpow.io/config/multi-cluster) to read more about multi-cluster.

### Registring multiple KafkaStreams instances for a single app

You can call the `register` method many times to register multiple streams apps:

```java
KafkaStreams dedupeStreams = new KafkaStreams(dedupeTopology, dedupeProps);
KafkaStreams paymentStreams = new KafkaStreams(paymentTopology, paymentProps);
registry.register(paymentStreams, paymentTopology);
registry.register(dedupeStreams, dedupeTopology);
```

## Troubleshooting 

### The Workflows UI is showing "Configure Streams Topology" for my consumer

This could happen for a few reasons:

1. `register` method has not been called.
2. Invalid connection details passed to `StreamsRegistry` constructor. If this is the case you will see Kafka producer exceptions in the logs of your streams application.
3. Telemetry is still being calculated. After a fresh deployment, it might take up to 2 minutes for initial streams telemetry to be calculated. 

You can verify `StreamsRegistry` is sending telemetry to your Kafka Cluster by using Data Inspect in kPow:

* Select topic `__oprtr_snapshot_state`
* Choose `Transit / JSON` as the key deserializer
* Enter the following kJQ filter for the key: `.[0] == :streams`

![Data Inspect](docs/data-inspect.png)

# Copyright and License

Copyright © 2021 Operatr Pty Ltd. 

Distributed under the Apache-2.0 License, the same as Apache Kafka.
