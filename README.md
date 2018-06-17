[![Javadocs](http://javadoc.io/badge/org.ow2.authzforce/authzforce-ce-xacml-json-model.svg)](http://javadoc.io/doc/org.ow2.authzforce/authzforce-ce-xacml-json-model)

This project provides [JSON schemas](src/main/resources/org/ow2/authzforce/xacml/json/model) for validating XACML Request/Response according to JSON Profile of XACMl 3.0, as well as a [library](src/main/java/org/ow2/authzforce/xacml/json/model/Xacml3JsonUtils.java) for validating XACML Requests/Responses against:

1. [Those JSON schemas](src/main/resources/org/ow2/authzforce/xacml/json/model);
2. Security constraints in order to mitigate JSON parsing Denial-of-Service attacks, e.g. such as string size, array size, number of keys, depth (see [LimitsCheckingJSONObject class](src/main/java/org/ow2/authzforce/xacml/json/model/LimitsCheckingJSONObject.java)).

Check the [test classes](src/test/java) to find out usage examples. 

# XSD-to-JSON-schema generation
[JSON schemas](src/main/resources/org/ow2/authzforce/xacml/json/model) were bootstrapped with XML-schema-to-JSON generation tool `xsd2json2`. Then significant refactoring and adaptation to draft 6 of JSON schema has been done.

# JSON schema limitations in comparison with XML
- No support for (object-oriented) inheritance/polymorphism: https://github.com/json-schema-org/json-schema-spec/issues/348
- Other issues considered for draft 7 (current version is draft 6):
https://github.com/json-schema-org/json-schema-spec/milestone/5
- Much fewer built-in types, e.g. no date type: https://github.com/json-schema-org/json-schema-spec/issues/199

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

# Differences between Policy's JSON schema and XML schema from XACML spec
* Policy/Rule's Version is optional in JSON schema
* Policy's PolicyIssuer does not have a Content element
* Policy may have a Condition (in addition to Target)
* Rule may have VariableDefinitions 
* Match may have VariableReference (in addition to AttributeDesignator/AttributeSelector)
* Apply must have at least one arg to the function
* Type PepActionExpression replaces Obligation/Advice with a boolean property "required" to make the difference (=true for Obligation, =false for Advice)
* Datatype defined at Attribute level, not AttributeValue level, like in XACML/JSON Profile
* Parts of model happen to be not feasible or more complex to achieve than XACML/XML schema due to limitations of JSON schema or of the implementation library (everit json-schema). More info:
http://json-schema.org/draft-06/json-schema-release-notes.html#q-what-happened-to-all-the-discussions-around-re-using-schemas-with-additionalproperties
https://github.com/everit-org/json-schema/issues/184#issuecomment-393419878
Should be fixed in next draft 08: https://github.com/json-schema-org/json-schema-org.github.io/issues/77
