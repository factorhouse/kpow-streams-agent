[![test](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml)

# kpow-streams-agent

kPow's Kafka Streams agent. Install this agent to monitor Kafka Streams applications with [kPow](https://kpow.io)

[Documentation](https://docs.kpow.io/features/kafka-streams)

![screenshot](docs/screenshot.png)

# Installation

kPow's streams agent can be found on Maven:

```xml
<dependency>
  <groupId>io.operatr</groupId>
  <artifactId>kpow-streams-agent</artifactId>
  <version>0.2.0</version>
  <type>bundle</type>
</dependency>
```

# Usage

To instrument a Kafka Streams application, create a new instance of a `StreamsRegistry` and register your `KafkaStreams` + `Topology` instances against it.

```java 
import io.operatr.kpow.StreamsRegistry;

Properties props = new Properties(); // Kafka Producer properties -- this is the Kafka cluster the metrics will be sent to (and where kPow should be installed).
StreamsRegistry registry = new StreamsRegistry(props); // The registry instance

Topology topology = new Topology(); // Your Kafka Streams topology
Properties streamsProps = new Properties(); // Your Kafka Streams config
KafkaStreams streams = new KafkaStreams(topology, streamsProps); // Your Kafka Streams instance

registry.register(streams, topology); // Register your Kafka Streams instance with the registry
```

Once configured, metrics will be periodically sent to kPow's internal snapshot topic. You will be able to monitor your streams application from within kPow and externally via [Prometheus Egress](https://docs.kpow.io/features/prometheus)

For more information read the [documentation](https://docs.kpow.io/features/kafka-streams)

# Copyright and License

Copyright © 2021 Operatr Pty Ltd. Distributed under the Eclipse Public License, the same as Clojure uses. See the file LICENSE
