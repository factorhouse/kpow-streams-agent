# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

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
