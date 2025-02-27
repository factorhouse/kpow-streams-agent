# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [1.0.0] - 2025-02-27

A major release of the StreamsAgent with some breaking changes.

### Changed
- Add new [KeyStrategy](https://javadoc.io/static/io.factorhouse/kpow-streams-agent/latest/io/factorhouse/kpow/key/KeyStrategy.html) interface: specifies how metrics data should be keyed when writing to Kpow's internal snapshots topic.
- Add two concrete implementations of `KeyStrategy`: [ClientIdKeyStrategy](https://javadoc.io/static/io.factorhouse/kpow-streams-agent/latest/io/factorhouse/kpow/key/ClientIdKeyStrategy.html) and [ClusterIdKeyStrategy](https://javadoc.io/static/io.factorhouse/kpow-streams-agent/latest/io/factorhouse/kpow/key/ClusterIdKeyStrategy.html).
- Add new [MetricFilters](https://javadoc.io/doc/io.factorhouse/kpow-streams-agent/latest/io/factorhouse/kpow/MetricFilter.html) class: allows developers to define which metrics should be included or excluded when reporting to Kpow's internal Kafka topic. Filters can be customized to suit the needs of specific deployments or use cases.

### Breaking
- Removed `io.operatr` namespace
- Moved deployment to [io.factorhouse/kpow-streams-agent](https://central.sonatype.com/artifact/io.factorhouse/kpow-streams-agent) on Maven
- `register` method of the [StreamsRegistry](https://javadoc.io/doc/io.factorhouse/kpow-streams-agent/latest/io/factorhouse/kpow/StreamsRegistry.html) class has an additional argument: `keyStrategy`. 

### Changed
- Move to `io.factorhouse` domain from `io.operatr`
- Keep old `io.operatr.kpow.StreamsRegistry` entry point for backwards compatibility
- Bump Kafka Streams to 3.6.1 (same as Kpow)
- Default producer `enable.idempotence` to false (avoid ACL issues with Kafka 3.2.0+)
- Bump other dependencies to latest
- Update readme and images

## [0.2.12] - 2024-04-10
### Changed
- Bump dependencies, fix CVE-2024-22871
 
## [0.2.11] - 2022-05-27
### Changed
- Add `compression.type` to allowed Producer properties
- Default Producer `compression.type` to `gzip`
- Bump dependencies

## [0.2.10] - 2022-02-03
### Changed
- Additional connection fields allowed for Producer properties 

## [0.2.9] - 2022-01-03
### Fixed
- Fix `unregister` method of `io.operatr.kpow.StreamsRegistry`
### Changed
- Bump dependencies

## [0.2.8] - 2021-05-10
### Changed
- Consistent `:captured` value in snapshots sent
- Improve snapshot logging+debugging
- Bump dependencies

## [0.2.7] - 2021-30-08
### Changed
- `cheshire` moved to dev dependencies

## [0.2.6] - 2021-01-07
### Changed
- `Properties` passed into `StreamsRegistry` constructor filtered to only relevant Kafka connection details. Eg, to reuse streams properties.

## [0.2.5] - 2021-25-06
### Fixed
- Fixed project details in `pom.xml`

## [0.2.4] - 2021-24-06
### Changed
- Explicitly set `-target` and `-source` javac options to `1.7`.

## [0.2.3] - 2021-24-06
### Fixed
- Updated `project.clj` to meet requirements of deploying to Maven central.

## [0.2.2] - 2021-24-06
### Changed
- Underlying `StreamsRegistry` thread pool constructed with a thread factory. Improves thread names and logging.

## [0.2.1] - 2021-24-06
### Fixed
- Fixed formatting of logging statement

## [0.2.0] - 2021-23-06
### Added
- `io.operatr.kpow.StreamsRegistry` (initial release)
