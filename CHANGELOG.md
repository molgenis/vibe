# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Added
- Implemented CI through Travis.
- Added a CHANGELOG.md file.

### Changed
### Deprecated
### Removed
### Fixed
### Security

## [5.1.4] - 2021-07-07
### Changed
- Updated to new database version:
  - DisGeNET: v6.0.0 → v7.0.0 (also still includes some data from v5.0.0)
  - Orphadata HOOM: 1.3 → 1.5
  - Semanticscience Integrated Ontology: 1.43 → 1.51
  
### Fixed
- Fixed error messages not always showing fully.
## [5.0.3] - 2021-04-13
### Fixed
- Fixed a bug causing an error when using the simple output-format and no results were found.

## [5.0.1] - 2021-02-02
### Changed
- Switched from TDB to HDT as main database storage format
    - \#59 Allow concurrent usage of database among separate JVMs
    - \#69 Allow database to be used from a read-only partition
    - Database now requires less storage size while maintaining similar performance
- Updated documentation

### Fixed
- Implemented workaround where within `BiologicalEntityCollection` a different `combinationsMap` would generate the same hashCode

[5.1.4]: https://github.com/molgenis/vibe/compare/vibe-5.0.3...vibe-5.1.4
[5.0.3]: https://github.com/molgenis/vibe/compare/vibe-5.0.1...vibe-5.0.3
[5.0.1]: https://github.com/molgenis/vibe/compare/vibe-4.0.2...vibe-5.0.3