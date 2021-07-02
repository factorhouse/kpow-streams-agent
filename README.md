[![test](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.operatr/kpow-streams-agent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.operatr%22%20AND%20a:%22kpow-streams-agent%22)

# [kPow](https://kpow.io) Streams Agent

This repository contains the kPow Streams Agent.

Use this agent to integrate your Kafka Streams applications with kPow and unlock the following features:

* Visualise Kafka Streams topologies in the kPow Streams UI.
* Monitor Kafka Streams metrics (e.g Stream-Thread, State Store, RocksDB, etc).
* See summaries of Kafka Streams activity for your Kafka cluster(s).
* Aggregate and Expose Kafka Streams metrics via kPow [Prometheus Endpoints](https://docs.kpow.io/features/prometheus) (for alerting, etc).
* (Soon) View kPow Insights of your Kafka Streams applications (outlier metrics, etc).

See the [kPow Kafka Streams Feature Guide](https://docs.kpow.io/features/kafka-streams) for full documentation.

---

![topology-ui](docs/topology-ui.png)

# Prerequisites

The kPow Streams Agent requires a running instance of kPow.

Evaluate kPow with the [kPow Local](https://github.com/operatr-io/kpow-local) repository or see our [Quick Start](https://docs.kpow.io/installation/quick-start) guide.

# Installation

The kPow Stream Agent can be found on [Maven Central](https://search.maven.org/artifact/io.operatr/kpow-streams-agent).

Include the agent as a dependency in your Kafka Streams application.

```xml
<dependency>
  <groupId>io.operatr</groupId>
  <artifactId>kpow-streams-agent</artifactId>
  <version>0.2.6</version>
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
* Produce snapshots to the kPow internal `__oprtr_snapshot_state` topic.

The StreamsRegistry **does not talk directly to kPow**. kPow reads streams data from the snapshot topic.

# Configuration

The `StreamsRegistry` `Properties` contains configuration to create the snapshot producer.

The StreamsRegistry configures its own Serdes on the snapshot producer, you do not need to set them.

Producer configuration means any of the following fields: 

* bootstrap.servers
* ssl.truststore.type
* ssl.truststore.password
* ssl.truststore.location
* ssl.truststore.certificates
* ssl.trustmanager.algorithm
* ssl.secure.random.implementation
* ssl.provider
* ssl.protocol
* ssl.keystore.type
* ssl.keystore.password
* ssl.keystore.location
* ssl.keystore.key
* ssl.keystore.certificate.chain
* ssl.keymanager.algorithm
* ssl.key.password
* ssl.endpoint.identification.algorithm
* ssl.enabled.protocols
* ssl.cipher.suites
* security.protocol
* sasl.mechanism
* sasl.jaas.config
* sasl.login.callback.handler.class

For more details visit the [Producer](https://kafka.apache.org/documentation/#producerconfigs) section of the Apache Kafka documentation.

### Minimum Required ACLs

If you secure your Kafka Cluster with ACLs, the user provided in the Producer configuration must have permission to write to the internal kPow topic.

```
./kafka-acls.sh \
  --bootstrap-server 127.0.0.1:9092 \
  --command-config client.conf \
  --add --allow-principal User:<your-producer-user> --operation Write --topic '__oprtr_snapshot_state'
```

### Produce to the Primary Cluster

When managing a single Kafka Cluster you can reuse the properties from your Kafka Streams application to create your StreamsRegisty. This is because the kPow internal topic `___oprtr_snapshot_compute` lives in the cluster that your Kafka Streams application connects to.

When managing multiple Kafka Clusters configure your StreamsRegistry to produce snapshots to the **primary** Cluster that contains the internal kPow snapshot topics. This is the first cluster in your kPow configuration.

### Single-Cluster kPow

Reuse your Kafka Streams `Properties` to create your StreamsRegistry.

```java
Properties streamsProps = new Properties();
KafkaStreams streams = new KafkaStreams(topology, streamsProps);

StreamsRegistry registry = new StreamsRegistry(streamsProps);
...
```

### Multi-Cluster kPow

Use a `Properties` with your **primary** cluster configuration to create your StreamsRegistry.

```java
Properties streamsProps = createMyStreamProperties();
KafkaStreams streams = new KafkaStreams(topology, streamsProps); 

Properties primaryProps = createMyPrimaryClusterProducerProperties();
StreamsRegistry registry = new StreamsRegistry(primaryProps);
...
```

See the [kPow Multi-Cluster Feature Guide](https://docs.kpow.io/config/multi-cluster) for more information.

### Multi-Cluster kPow Feedback Requested

Is the requirement to produce to the primary kPow cluster difficult for you?

Please [let us know](mailto:support@operatr.io) - we are considering the option of always writing to the same cluster as your Kafka Streams connects to and having kPow gather snapshots from each cluster.

### Register Multiple Kafka Streams Instances

You can register multiple Kafka Streams instances on the same StreamsRegistry.

```java
KafkaStreams dedupeStreams = new KafkaStreams(dedupeTopology, dedupeProps);
KafkaStreams paymentStreams = new KafkaStreams(paymentTopology, paymentProps);
registry.register(paymentStreams, paymentTopology);
registry.register(dedupeStreams, dedupeTopology);
```

## Troubleshooting 

### The Workflows UI is showing "Configure Streams Topology"

This could happen for a few reasons:

1. `register` method has not been called.
2. Invalid connection details passed to `StreamsRegistry` constructor. If this is the case you will see Kafka producer exceptions in the logs of your streams application.
3. Telemetry is still being calculated. After a fresh deployment, it might take up to 2 minutes for initial streams telemetry to be calculated. 

You can verify `StreamsRegistry` is sending telemetry to your Kafka Cluster by using Data Inspect in kPow:

* Select topic `__oprtr_snapshot_state`
* Choose `Transit / JSON` as the key deserializer
* Enter the following kJQ filter for the key: `.[0] == :streams`

![Data Inspect](docs/data-inspect.png)

# Get Help

If you have any issues contact [support@operatr.io](mailto:support@operatr.io).

# Copyright and License

Copyright © 2021 Operatr Pty Ltd. 

Distributed under the Apache-2.0 License, the same as Apache Kafka.
