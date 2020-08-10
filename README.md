[![Javadocs](http://javadoc.io/badge/org.ow2.authzforce/authzforce-ce-xacml-json-model.svg)](http://javadoc.io/doc/org.ow2.authzforce/authzforce-ce-xacml-json-model)

# XACML/JSON Request and Response JSON schema (XACML/JSON Profile standard) and validation
This project provides JSON schemas for validating XACML Requests/Responses according to JSON Profile of XACML 3.0:
- [Request.schema.json](src/main/resources/org/ow2/authzforce/xacml/json/model/Request.schema.json) for validating XACML/JSON Requests;
- [Response.schema.json](src/main/resources/org/ow2/authzforce/xacml/json/model/Response.schema.json) for validating XACML/JSON Responses.

The project also provides a [library](src/main/java/org/ow2/authzforce/xacml/json/model/XacmlJsonUtils.java) for validating XACML Requests/Responses against both these JSON schemas and security constraints in order to mitigate JSON parsing Denial-of-Service attacks, e.g. such as string size, array size, number of keys, depth (see [LimitsCheckingJSONObject class](src/main/java/org/ow2/authzforce/xacml/json/model/LimitsCheckingJSONObject.java)).

Check the [test classes](src/test/java) to find out usage examples.

# XACML/JSON Policy JSON schema and validation

Although the standard JSON Profile of XACML does not define a JSON format for XACML Policy(Set), this AuthzForce project defines such format in a JSON schema which can be used for validation:
[Policy.schema.json](src/main/resources/org/ow2/authzforce/xacml/json/model/Policy.schema.json).

There are a few high-level differences between this JSON schema and the standard XACML/XML schema for Policies/PolicySets:
* A more generic Policy model: 
  * No distinction between Policy and PolicySet in JSON schema, so a Policy and PolicySet definitions are merged, and Policy may enclose Policies (like a PolicySet may enclose PolicySets)
  * Policy/Rule's Version is optional in JSON schema
  * Policy may have a Condition (in addition to Target), like a Rule
  * Match may have VariableReference instead of AttributeDesignator/AttributeSelector
* Type PepActionExpression replaces Obligation/Advice with a boolean property "required" to make the difference (=true for Obligation, =false for Advice)
* DataType defined at Attribute level, not AttributeValue level, like in standard XACML/JSON Profile
* Apply must have at least one arg to the function
* Several XACML/XML features are not translatable to JSON, or require a non-standard - possibly complex - convention or workaround to be translated to JSON, due to limitations of JSON, JSON Schema or of the implementation library (everit json-schema). See next section in this document for more info.

More info:
- http://json-schema.org/draft-06/json-schema-release-notes.html#q-what-happened-to-all-the-discussions-around-re-using-schemas-with-additionalproperties
- https://github.com/everit-org/json-schema/issues/184#issuecomment-393419878, should be fixed in next draft 08: https://github.com/json-schema-org/json-schema-org.github.io/issues/77

# XACML/XML - XACML/JSON conversion

Before we present the various utilities for XACML/XML to XACML/JSON conversion, you should **be aware of the following limitations of JSON when compared to XML** as this may cause information loss in any XML-to-JSON conversion:
- No multi-line string (used in XACML Description and AttributeValue elements)
- No equivalent for XML mixed content (may be used in AttributeValues)
- No comments (like XML comments).
- No support for (object-oriented) inheritance/extension/polymorphism (may be used in AttributeValues): https://github.com/json-schema-org/json-schema-spec/issues/348
- Much fewer built-in types, e.g. no date/time type: https://github.com/json-schema-org/json-schema-spec/issues/199
- Other issues considered for next JSON schema drafts:
https://github.com/json-schema-org/json-schema-spec/milestones

## Converting XACML/XML Requests/Responses to JSON
This project provides XSLT stylesheets for XACML/XML Requests (and Responses) to XACML/JSON conversion according to JSON Profile of XACML:

- [xacml-request-xml-to-json.xsl](src/test/resources/xacml-request-xml-to-json.xsl): XSLT stylesheet for XACML Request conversion;
- [xacml-response-xml-to-json.xsl](src/test/resources/xacml-response-xml-to-json.xsl): XSLT stylesheet for XACML Response conversion;
- [xacml-common-xml-to-json.xsl](src/test/resources/xacml-common-xml-to-json.xsl): XSLT stylesheet shared therefore required by previous stylesheets.

In order to use these, first download all of them to the same folder (or just do a `git clone` of the project), then pass the `xacml-request-xml-to-json.xsl` (resp. `xacml-response-xml-to-json.xsl`) file as stylesheet parameter to the XSLT processor for Request (resp. Response) conversion. 

**WARNING: the XSLT processor must support XSLT 3.0 or later**, e.g. [Saxon](https://www.saxonica.com/products/products.xml) 9.8+.

Here is a command-line example to convert a XACML/XML Request with Saxon for Java (you may find the SAXON jar on [SAXONICA's website](https://www.saxonica.com/download/java.xml) or in your local Maven repository if you built this project from source already, e.g. `~/.m2/repository/net/sf/saxon/Saxon-HE/9.8.0-15/Saxon-HE-9.8.0-15.jar`):

```
$ java -jar Saxon-HE-9.8.0-15.jar -xsl:/path/to/xacml-request-xml-to-json.xsl -s:/path/to/xacml-request.xml -o:/path/to/request.json
```
*Remove the `-o` option if you don't want the output to a file but directly to the console.*

## Converting XACML/XML Policy(Set) to JSON and vice versa
As mentioned before, this AuthzForce project provides its own JSON format for XACML Policy(Set) in a JSON schema. It also provides stylesheets to convert from the standard XACML/XML Policy format to this JSON format, and vice versa:

- [xacml-policy-xml-to-json.xsl](src/test/resources/xacml-policy-xml-to-json.xsl): XSLT stylesheet for converting XACML Policy(Set) from XML (standard) to JSON (custom AuthzForce format);
- [xacml-policy-json-to-xml.xsl](src/test/resources/xacml-policy-json-to-xml.xsl): XSLT stylesheet for converting XACML Policy(Set) from JSON (custom AuthzForce format) to XML (standard);
- [xacml-common-xml-to-json.xsl](src/test/resources/xacml-common-xml-to-json.xsl): XSLT stylesheet shared therefore required by previous stylesheets.

In order to use these, first download all of them to the same folder (or just do a `git clone` of the project), then pass the `xacml-policy-xml-to-json.xsl` (resp. `xacml-policy-json-o-xml.xsl`) file as stylesheet parameter to the XSLT processor for XML-to-JSON (resp. JSON-to-XML) conversion. 

**WARNING: the XSLT processor must support XSLT 3.0 or later**, e.g. [Saxon](https://www.saxonica.com/products/products.xml) 9.8+.

For the JSON-to-XML conversion, you have to specify the input JSON file as `inJsonFile` stylesheet parameter, and force the XSLT processor to use the default initial template (`xsl:initial-template`). You should not have to specify a source XML file. If you have to, use a [dummy one](src/test/resources/dummy.xml).

Here is a command-line example to convert a XACML Policy from XML to JSON with Saxon for Java (you may find the SAXON jar on [SAXONICA's website](https://www.saxonica.com/download/java.xml) or in your local Maven repository if you built this project from source already, e.g. `~/.m2/repository/net/sf/saxon/Saxon-HE/9.8.0-15/Saxon-HE-9.8.0-15.jar`):

```
$ java -jar Saxon-HE-9.8.0-15.jar -xsl:/path/to/xacml-policy-xml-to-json.xsl -s:/path/to/Policy.xml -o:/path/to/Policy.json
```
*Remove the `-o` option if you don't want the output to a file but directly to the console.*

... and an example to convert back the Policy from JSON to XML (e.g. `Policy.json -> Policy.xml`):

```
$ java -jar Saxon-HE-9.8.0-15.jar -xsl:/path/to/xacml-policy-json-to-xml.xsl -it -o:/path/to/Policy.xml inJsonFile=/path/to/Policy.json
```
*Remove the `-o` option if you don't want the output to a file but directly to the console.*

# Disabling JSON Profile compliance
When using any of the aforementioned XML-to-JSON stylesheets, you may disable compliance with the JSON Profile of XACML by specifying the stylesheet parameter (same way as `inJsonProfile`): `useJsonProfile=no`.
As a result, the JSON output will be modified as follows: 
1. The property name for the array of attribute values will be `Values` instead of `Value` (makes more sense, doesn't it?);
1. Property names will be in lower camel case instead of upper camel case, which is a more common practice among JSON API specifications;
1. A few property names will be shortened.

# XSD-to-JSON-schema generation
[JSON schemas](src/main/resources/org/ow2/authzforce/xacml/json/model) were bootstrapped with XML-schema-to-JSON generation tool `xsd2json2`. Then significant refactoring and adaptation to draft 6 of JSON schema has been done.

## Notes on using `xsd2json2`
WARNING: xsd2json2 supports only draft 4 but it is close to draft 6.
Before using xsd2json, first install SWI-Prolog package that is required to avoid issue: https://github.com/fnogatz/xsd2json/issues/22

```
$ sudo apt install swi-prolog
$ npm install -g xsd2json2
$ xsd2json2 -v ~/git/authzforce-ce-parent.git/xacml-model/src/main/resources/xacml-core-v3-schema-wd-17.xsd > xacml-core-v3-schema-wd-17.jsonschema
```

## OTHER XSD-to-JSON GENERATION TOOLS TESTED THEN DISCARDED
### jsonschema.net
For info, you can generate first draft of JSON schema from request.json on https://jsonschema.net/#/editor but it only supports draf 04 and this is not really useful because you would need to spend time to make a Request covering all possibilities to build a good schema. (Is that even possible?)

### JSONix schema compiler
See https://github.com/highsource/jsonix-schema-compiler/wiki/JSON-Schema-Generation
Not a good idea actually, because this changes many things compared to original XML and tries to keep distinction between XML attributes and elements and refers to JSONix custom non-standard types for each XML type, which is not what we want, 
```
$ sudo npm install -g jsonix-schema-compiler
$ java -jar /usr/local/lib/node_modules/jsonix-schema-compiler/lib/jsonix-schema-compiler-full.jar -generateJsonSchema ~/git/authzforce-ce-parent.git/xacml-model/src/main/resources/xacml-core-v3-schema-wd-17.xsd
```

This generates .js and oasis_names_....jsonschema files.

# TODO
