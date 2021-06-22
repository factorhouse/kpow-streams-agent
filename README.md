# streams-agent

kPow's Kafka Streams agent. Install this 

[Documentation](https://docs.kpow.io/features/kafka-streams)

# Installation

kPow's streams agent can be found on Maven:

``` 
com.operatr/streams-agent
```

# Usage

```java 
import com.operatr.kpow.StreamsRegistry;

Properties props = new Properties(); // Kafka Producer properties -- this is the Kafka cluster the metrics will be sent to.
StreamsRegistry registry = new StreamsRegistry(props); // The registry instance

Topology topology = new Topology(); // Your Kafka Streams topology
KafkaStreams streams = new KafkaStreams(); // Your Kafka Streams instance

(.register registry streams topology); // Register your Kafka Streams instance with the registry
```

For more information read the [documentation](https://docs.kpow.io/features/kafka-streams)

# Copyright and License

Copyright © 2021 Operatr Pty Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
https://www.apache.org/licenses/LICENSE-2.0
```

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

