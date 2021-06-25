[![test](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/operatr-io/streams-agent/actions/workflows/test.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.operatr/kpow-streams-agent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.operatr%22%20AND%20a:%22kpow-streams-agent%22)

# [kPow](https://kpow.io) Streams Agent

kPow's Kafka Streams agent. Install this agent to monitor Kafka Streams applications with [kPow](https://kpow.io)

[Documentation](https://docs.kpow.io/features/kafka-streams)

![screenshot](docs/screenshot.png)

# Installation

kPow's streams agent can be found on [Maven Central](https://search.maven.org/artifact/io.operatr/kpow-streams-agent):

```xml
<dependency>
  <groupId>io.operatr</groupId>
  <artifactId>kpow-streams-agent</artifactId>
  <version>0.2.4</version>
  <type>bundle</type>
</dependency>
```

# Prerequisites

kPow's Streams Agent requires a running instance of kPow. 

To get up and running with kPow, visit the [kpow-local](https://github.com/operatr-io/kpow-local) repo or visit our [Quick Start](https://docs.kpow.io/installation/quick-start) guide.

# Usage

To instrument a Kafka Streams application, create a new instance of a `StreamsRegistry` and register your `KafkaStreams` + `Topology` instances against it.

```java 
import io.operatr.kpow.StreamsRegistry;

// Your Kafka Streams topology
Topology topology = new Topology(); 

// Your Kafka Streams config
Properties streamsProps = new Properties();
 
// Your Kafka Streams instance
KafkaStreams streams = new KafkaStreams(topology, streamsProps); 

// kPow Producer properties for the Kafka cluster that Streams metrics will be sent (and where kPow should be installed).
Properties props = new Properties(); 

// kPow Streams Registry to periodically capture and send metrics with the Producer properties above
StreamsRegistry registry = new StreamsRegistry(props);

// Register your Kafka Streams instance with the kPow StreamsRegistry
registry.register(streams, topology); 

// Start your Kafka Streams application
streams.start();
```

Once configured, metrics will be periodically sent to kPow's internal snapshot topic. You will be able to monitor your streams application from within kPow and externally via [Prometheus egress](https://docs.kpow.io/features/prometheus)

For more information read the [documentation](https://docs.kpow.io/features/kafka-streams)

# Copyright and License

Copyright © 2021 Operatr Pty Ltd. 

Distributed under the Apache-2.0 License, the same as Apache Kafka.
