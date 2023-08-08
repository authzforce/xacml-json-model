# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. This project adheres to [Semantic Versioning](http://semver.org).


## 3.0.5
### Fixed
- CVEs by upgrading:
  - Parent project (authzforce-ce-parent): 8.5.0
  - Maven dependencies:
    - authzforce-ce-xacml-model: 8.5.0 
    - com.github.everit-org.json-schema/org.everit.json.schema -> com.github.erosb/everit-json-schema: 1.14.2
    - Spring Core: 5.3.29
- `authzforce-ce-xacml-model` dependency: missing `XacmlAttributeId` enum value for standard XACML 3.0 Core attribute `urn:oasis:names:tc:xacml:2.0:resource:target-namespace` (used for `<Content>` processing) has been added

## 3.0.4
### Fixed 
- CVE-2021-22696 and CVE-2021-3046 fixed by upgrading **authzforce-ce-parent to v8.0.3**
- Fix for https://github.com/authzforce/server/issues/64 (loading schemas in offline mode fails)


## 3.0.3
### Fixed
- CVE-2021-22118: updated parent version to 8.0.2 -> Spring to 5.2.15


## 3.0.2
### Fixed
- Backward compatibility on XacmlJsonUtils#canonicalizeResponse(), i.e. avoid breaking compatibility for reverse dependencies


## 3.0.1
### Fixed
- JSON schema identifiers in XACML/JSON schemas: updated to valid links on github
- XacmlJsonUtils#canonicalizeResponse() to better identify similar XACML/JSON responses


## 3.0.0
### Changed
- Upgraded parent project version: 8.0.0
- Upgraded to Java 11 (Java 8 no longer supported)
- Upgraded spring-core: 5.2.10


## 2.3.0
### Changed
- Upgraded parent project version: 7.6.0
  - Upgraded dependencies:
  	- spring-core: 5.1.14
  	- org.everit.json.schema: 1.12.1


## 2.2.0
### Added
- JSON schema for XACML Policy(Set)
- XSLT stylesheets for XACML XML-to-JSON Request/Response/Policy/PolicySet conversion

### Fixed
- `XacmlJsonUtils#canonicalizeResponse(...)` method: remove IncludeInResult properties in Response (useless, esp. for comparison)

## 2.1.1
### Fixed
- CVE affecting Spring v4.3.18: upgraded to 4.3.20


## 2.1.0
### Changed
- Parent project version (authzforce-ce-parent):7.5.0
	- Managed dependency versions:
		- Spring: 4.3.18 (fixes CVE)
- Copyright company name


## 2.0.0
### Changed
- Parent project version (authzforce-ce-parent):7.3.0
	- Managed dependency versions:
		- Spring: 4.3.14.RELEASE
- Renamed Xacml3JsonUtils class to XacmlJsonUtils
- Replaced format uri with uri-reference in JSON schemas to match XML anyURI

### Added
- Added maven build plugins: dependency-check-maven, pmd, findbugs, surefire, licence
- New JSON schema in file `Policy.schema.json` for XACML-equivalent JSON policies (actually a superset of XACML model), ie. non-standard XACML/JSON schema defined by AuthzForce


## 1.1.0
### Changed
- Parent project version (authzforce-ce-parent): 7.0.0 -> 7.1.0
	- Managed dependency versions:
		- org.everit.json.schema: 1.6.0 -> 1.6.1
		- guava: 21.0 -> 22.0
		- json: 20170516 -> 20171018


## 1.0.0
### Added
- Initial release on GitHub



