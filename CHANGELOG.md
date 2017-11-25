# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. This project adheres to [Semantic Versioning](http://semver.org).


## 12.0.0
### Changed
- Parent project: 6.0.0 -> 7.0.0
- Renamed PDP extension interfaces and base implementations:
	* (Base|Closeable)AttributeProviderModule >
(Base|Closeable)DesignatedAttributeProvider
	* (Base)RequestFilter -> (Base)DecisionRequestPreprocessor
	* DecisionResultFilter -> DecisionResultPostprocessor
	* CloseablePdp -> CloseablePdpEngine
	* (Immutable)PdpDecisionRequest -> (Immutable)DecisionRequest
	* PdpDecisionResult -> DecisionResult
	* PdpDecisionRequest(Factory|Builder) -> DecisionRequest(Factory|Builder)
	* (Base|Closeable)(Static)RefPolicyProviderModule -> (Base|Closeable)(Static)RefPolicyProvider
	* RootPolicyProviderModule -> RootPolicyProvider
	* (Base)DatatypeFactory(Registry) -> (Base)AttributeValueFactory(Registry) (using new class AttributeDatatype subclass of Datatype)
- Uses of IdReferenceType (for Policy(Set)IdReference) replaced by new interface PrimaryPolicyMetadata (identifies Policy uniquely) in all APIs where necessary
- Moved JaxbXacmlUtils utility class out to authzforce-ce-xacml-model project (renamed to Xacml3JaxbHelper)
- New extensible framework for PDP engine adapters, e.g. for specific types of input/output (SerDes), PDP engine itself made agnostic of request/response serialization formats 
	* New package org.ow2.authzforce.core.pdp.api.io for classes related to input/output (SerDes) adapter, e.g. from/to XACML-XML
	* New interface PdpEngineInoutAdapter (default implementation is XACML/XML using JAXB API, XACML/JSON one moved to separate project)
- More optimal implementation of XACML integer values: 3 possible
GenericInteger interface implementations depending on maximum (size)
(ArbitrarilyBigInteger for java BigIntegers, MediumInteger for java Integers, and LongInteger for java Longs), with value caching (like Java
Integer/Long). This optimizes memory usage / CPU computation when dealing with XACML integers small enough to fit in Java Integers/Longs.
- Class naming conventions regarding acronyms (only first letter should be uppercase, see also
https://google.github.io/styleguide/javaguide.html#s5.3-camel-case), for example:
	* AnyURIValue -> AnyUriValue
	* AttributeFQN -> AttributeFqn
	* AttributeFQNs -> AttributeFqns
	* CloseablePDP -> CloseablePdp
	* JaxbXACMLUtils -> JaxbXacmlUtils
	* PDPEngine -> PdpEngine
	* XMLUtils -> XmlUtils...


## 11.0.0 
### Changed 
- StaticRefPolicyProviderModule interface to abstract class
- Renamed RefPolicyProvider.Utils class (utility methods for Policy Provider implementations) to RefPolicyProvider.Helper

### Added 
- BaseStaticRefPolicyProviderModule class as convenient base class for static Policy Provider (StaticRefPolicyProviderModule ) implementations


## 10.0.0
### Added
- Class AttributeSource and AttributeSources: source of attribute values, e.g. the Request, the PDP, an AttributeProvider module, etc.
- Class AttributeBag: new kind of Bag that represents an attribute bag (values) with metadata such as value source (AttributeSource) 
- Interface EvaluationContext: new methods to attach one or more context listeners, and get back the attached listener(s)
- New Expression interface implementations: AttributeDesignatorExpression (XACML AttributeDesignator evaluator) and AttributeSelectorExpression (XACML Attribute Selector evaluator)

### Changed
- Changed POM parent version: 6.0.0.
- Changed DecisionResultFilter interface methods
- Changed RequestFilter interface methods
- Changed DecisionCache interface methods by adding EvaluationContext parameter for context-dependent caches
- Changed RefPolicyProvider interface methods
- Changed PDPEngine interface methods
- Changed EvaluationContext interface methods
- Changed Expression interface methods
- Changed VersionPatterns class methods to return new PolicyVersionPattern class that helps manipulate XACML VersionMatchTypes
- Refactoring:
  - Renamed class IndividualDecisionRequest to IndividualXACMLRequest (XACML-specific model of Individual Decision Request)
  - Renamed class IndividualPdpDecisionRequest to PdpDecisionRequest (individual request in XACML-agnostic AuthzForce model)
  - Renamed class AttributeGUID(s) to AttributeFQN(s) (Fully Qualified Name is more appropriate than GUID)
  - Renamed class MutableBag to MutableAttributeBag


## 9.1.0
### Changed
- Changed parent version: v5.1.0:
	- License: GPL v3.0 replaced with Apache License v2.0
	- Project URL: 'https://tuleap.ow2.org/projects/authzforce' replaced with 'https://authzforce.ow2.org'
	- GIT repository URL base: 'https://tuleap.ow2.org/plugins/git/authzforce' replaced with 'https://gitlab.ow2.org/authzforce'
- Return type of `Datatype#getTypeParameter()`: `Datatype<?>` replaced with `Optional<Datatype<?>>`
- Return type of `AttributeGUID#getIssuer()`: `String<?>` replaced with `Optional<String<?>>`


## 9.0.0
### Changed
- Changed parent version: 4.1.1 -> 5.0.0
	-> Changed dependency versions: SLF4J: 1.7.6 -> 1.7.22; Guava: 20.0 -> 21.0
- Renamed class Pdp to PDPEngine and added methods to evaluate one or multiple Individual Decision Requests using more efficient API than XACML-schema-derived Request
- Renamed class PdpDecisionInput to PdpDecisionRequest -> changed DecisionCache API
- Changed DecisionResultFilter API


## 8.2.0
### Changed
- Parent project version: 4.1.1 (upgrades owasp dep check mvn plugin
version: 1.4.4 -> 1.4.4.1)
- LOG CRLF INJECTION issue (reported by find-sec-bugs) no longer fixed in code but assumed handled
by logback configuration (see Layout pattern 'replace' keyword in logback documentation)


## 8.1.0
### Changed
- Parent project version: 4.0.0 -> 4.1.0 => Saxon-HE dependency version 9.7.0-11 -> 9.7.0-14

### Fixed
- Security issues reported by find-sec-bugs plugin


## 8.0.0
### Added
- Extension mechanism to switch HashMap/HashSet implementation; default implementation is based on native JRE and Guava.
- AtomicValue interface for atomic/primitive values, implemented by Function and AttributeValue
- Public class PrimitiveDatatype for primitive value datatypes
- ConstantExpression interface (replaces ValueExpression) for all constant Value expression
- FunctionExpression interface, Expression wrapper for Functions (Function no longer extends Expression but AtomicValue) like Value
- Function datatype constant in StandardDatatypes class, used as formal parameter type for functions in higher-order functions
- Maven plugin owasp-dependency-check to check vulnerabilities in dependencies 

### Changed
- Function no longer extends Expression but AtomicValue since Function Expression is now materialized by new FunctionExpression interface
- Expression interface: method boolean isStatic() replaced by getValue() to get the constant result if expression is static/constant (instead of calling evaluate(null) which forces callers the complexity of handling IndeterminateEvaluationException), null if not
- ExpressionFactory interface: Function return types replaced with FunctionExpression (new interface)
- FirstOrderFunctionCall abstract class (base class for first-order function call implementations): changed to interface and abstract class logic moved to new BaseFirstOrderFunctionCall class,
- DatatypeFactory interface: removed method isExpressionStatic(), now useless since we have new Expression#getValue() method 
- CombiningAlg (combining algorithm interface) Evaluator interface: more generic
- Maven parent project version: 3.4.0 -> 4.0.0:
	- **Java version: 1.7 -> 1.8** (maven.compiler.source/target property)
	- Guava dependency version: 18.0 -> 20.0
	- Saxon-HE dependency version: 9.6.0-5 -> 9.7.0-11
	- com.sun.mail:javax.mail v1.5.4 changed to com.sun.mail:mailapi v1.5.6

### Removed
- ValueExpression interface, replaced by ConstantExpression
- Dependency on Koloboke, replaced by extension mechanism mentioned in *Added* section that would allow to switch from the default HashMap/HashSet implementation to Koloboke-based.


## 7.1.1
### Fixed
- Javadoc issues


## 7.1.0
### Fixed
- Bag.equals() ignoring duplicates (like XACML set-equals function). Fixed by using Guava Multiset as backend structure and Multiset.equals(), to comply with the mathematical definition of a bag/multiset and XACML definition which is basically the same.
- BaseStaticRootPolicyProviderModule keeping a reference to static refPolicyProvider, although policies are to be resolved statically at initialization time, after that, it is no longer needed. Fix: remove BaseStaticRootPolicyProviderModule to force RootPoliyPovider modules to manage their refPolicyProvider and free memory after use.

### Added 
- Bag.elements() method, returns a Multiset (Guava) view of a bag's elements, useful in particular to implement functions with bags like XACML set-*

### Removed
- BaseStaticRootPolicyProviderModule class removed (see fix above)


## 7.0.0
### Added
- Dependency: com.koloboke:koloboke-impl-jdk6-7:1.0.0 for better (performance and API) HashMap/HashSet. More info:
http://java-performance.info/hashmap-overview-jdk-fastutil-goldman-sachs-hppc-koloboke-trove-january-2015/

### Changed
- CombiningAlg.Evaluator (Combining Algorithm evaluator interface): 
  - Return type changed to ExtendedDecision (Decision, Status, Extended Indeterminate if Decision is Indeterminate), simpler than formerly DecisionResult
  - evaluate() takes 2 extra "out" parameters: UpdatablePepActions and UpdatableApplicablePolicies used to add/return PEP actions and applicable policies collected during evaluation
- DecisionCache interface: input PdpDecisionInput and output PdpDecisionResult allow to handle 2 new fields: named attributes and extra Content nodes used during evaluation; thus enabling smarter caching possibilities
- EvaluationContext interface: addApplicablePolicy(...) replaced by isApplicablePolicyIdListRequested() because applicable policies are now collected in the new "out" parameter above and in the evaluation results (DecisionResult) returned by Policy evaluators
- Deprecated Expression#getJAXBElement() usually used to get the original XACML from which the Expression was parsed (no longer considered useful)
- Bag#equals() re-implemented like XACML function set-equals
- Change implementation of unmodifidable lists to Guava ImmutableList
- Made all implementations of DecisionResult immutable


## 6.0.0
### Changed 
- Project parent version (3.4.0): all JAXB-annotated classes derived from XACML schema now implements java.io.Serializable interface. This affects subclasses StatusHelper, CombinerParameterEvaluator and concrete XXXValue classes (extending XACML AttributeValue)
- All method parameters made final when applicable
- IndividualDecisionRequest#isApplicablePolicyIdentifiersReturned() method renamed to isApplicablePolicyIdListReturned()

### Removed
- CombiningAlgSet and FunctionSet classes (GitHub issue #1), now useless.


## 5.0.0
### Changed
- Attribute Provider Extension interface (CloseableAttributeProviderModule interface): new parameter to pass global PDP environment properties to AttributeProvider extensions

## 4.0.2
### Fixed
- Code-style issues reported by Codacy

## 4.0.1
### Fixed
- Issues reported by Codacy


## 4.0.0
### Changed
- FirstOrderBagFunctions#getFunctions(): changed parameters to only one of type DatatypeFactory<AV> for simplification

### Fixed
- Current year in license header


## 3.8.0
### Added
- Implementations of XACML 3.0 Core standard data types
- Re-usable/abstract classes for XACML comparison/conversion/higher-order/set/bag functions

### Fixed
- Javadoc of DecisionResult#getExtendedIndeterminate() method


## 3.7.0
### Changed
- PDP extensions that are static root policy providers should now implement StaticRootPolicyProviderModule class, instead of RootPolicyProviderModule.Static class
- PDP extensions that are static ref-policy providers should now implement StaticRefPolicyProvider class, instead of RefPolicyProvider class with isStatic() method returning true
- (Static)RootPolicyProviderModule and (Static)RefPolicyProviderModule#get(...) return type is now (Static)TopLevelPolicyElementEvaluator instead of IPolicyEvaluator interface (removed)

### Added
- Interface method PolicyEvaluator#getExtraPolicyMetadata(): provides version of the evaluated Policy(Set) and policies referenced (directly/indirectly) from this Policy(Set)
- Interface method PolicyEvaluator#getPolicyElementType(): provides the type of top-level policy element (Policy or PolicySet).
- Interface method DecisionResult#getExtendedIndeterminate(): provides Extended Indeterminate value (to be used when #getDecision() returns "Indeterminate")


## 3.6.1
### Added
- Initial release on Github



