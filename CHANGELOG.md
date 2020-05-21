# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. This project adheres to [Semantic Versioning](http://semver.org).


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



