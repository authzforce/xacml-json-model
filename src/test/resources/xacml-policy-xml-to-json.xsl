<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2019 THALES. This file is part of AuthZForce CE. AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published 
	by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
	even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License 
	along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>. -->
<!-- Transformation of XACML 3.0/XML Policy(Set) to JSON; this version outputs all JSON property names in lower camel case -->
<!-- Author: Cyril DANGERVILLE -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xacml="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
	<xsl:output encoding="UTF-8" indent="yes" method="text" omit-xml-declaration="yes" />

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<!-- Parameter used in xacml-common-xml-to-json.xsl -->
	<xsl:param name="useJsonProfile" select="'yes'" />

	<xsl:include href="xacml-common-xml-to-json.xsl" />

	<!-- Integer and Boolean attributes -->
	<xsl:template match="@MaxDelegationDepth|@MustBePresent">
		<xsl:call-template name="simple-key-literal" />
	</xsl:template>

	<!-- String attributes and simple String elements -->
	<xsl:template match="@Category|@EarliestVersion|@LatestVersion|@ContextSelectorId|@Path|@Effect">
		<xsl:call-template name="simple-key-string" />
	</xsl:template>

	<!-- String attributes and simple String elements with name changed -->
	<xsl:template match="@ParameterName">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'ParamName'"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="@PolicyIdRef|@PolicySetIdRef|@RuleIdRef">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'CombinedRef'"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="@PolicySetId|@PolicyId|@RuleId|@VariableId">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'Id'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="@PolicyCombiningAlgId|@RuleCombiningAlgId">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'CombinerId'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="@MatchId">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'MatchFunc'" />
		</xsl:call-template>
	</xsl:template>

	<!-- Apply's @FunctionId -->
	<xsl:template match="@FunctionId">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'FuncId'" />
		</xsl:call-template>
	</xsl:template>

	<!-- Function used as expression in an array of Apply expressions -->
	<xsl:template match="xacml:Function">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'Func'" />
			<xsl:with-param name="valueExpr" select="@FunctionId" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- VariableDefinition used in array of combiner args -->
	<xsl:template match="xacml:VariableDefinition">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'Var'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- VariableReference used as expression in Condition or array of Apply args or AttributeAssignmentExpression -->
	<xsl:template match="xacml:VariableReference">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'VarRef'" />
			<xsl:with-param name="valueExpr" select="@VariableId" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- Description value may be multi-line. -->
	<xsl:template match="xacml:Description">
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'Desc'" />
			<!-- FIXME: remove comment -->
<!-- 			<xsl:with-param name="valueExpr" select="replace(.,'&#xA;','\\n')" /> -->
			<xsl:with-param name="valueExpr" select="." />
		</xsl:call-template>
	</xsl:template>


	<!-- Complex elements -->
	<!-- Special case when AttributeDesignator/AttributeSelector used as Expression in Condition/Apply/AttributeAssignmentExpression, add enclosing braces -->
	<xsl:template match="xacml:Condition/xacml:AttributeDesignator|xacml:Apply/xacml:AttributeDesignator|xacml:AttributeAssignmentExpression/xacml:AttributeDesignator">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'AttrDesignator'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:AttributeDesignator">
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'AttrDesignator'" />
		</xsl:call-template>
	</xsl:template>


	<xsl:template match="xacml:Condition/xacml:AttributeSelector|xacml:Apply/xacml:AttributeSelector|xacml:AttributeAssignmentExpression/xacml:AttributeSelector">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'AttrSelector'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:AttributeSelector">
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'AttrSelector'" />
		</xsl:call-template>
	</xsl:template>

	<!-- Single AttributeValue (not in Attribute) -->
	<xsl:template match="xacml:Match/xacml:AttributeValue">
		<!-- DataType already defined in AttributeDesignator/AttributeSelector -->
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'MatchedValue'" />
		</xsl:call-template>
	</xsl:template>

	<!-- AttributesValue used as one of the possible expressions in Condition/Apply -->
	<xsl:template match="xacml:Condition/xacml:AttributeValue|xacml:Apply/xacml:AttributeValue|xacml:AttributeAssignmentExpression/xacml:AttributeValue">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'Const'" />
			<xsl:with-param name="textNodeKey" select="'Value'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- Other AttributeValues (in AttributeAssignmentExpression, CombinerParameter, etc.) -->
	<xsl:template match="xacml:AttributeValue">
		<xsl:call-template name="elementToJson">
			<xsl:with-param name="skipBraces" select="true()" />
			<xsl:with-param name="textNodeKey" select="'Value'" />
		</xsl:call-template>
	</xsl:template>

	<!-- These end up in JSON array so don't add the json key. -->
	<xsl:template match="xacml:Match|xacml:CombinerParameter">
		<xsl:call-template name="elementToJson" />
	</xsl:template>

	<!-- Same as 'elemenToJsonWithKey' template but with remove one too many bracket level -->
	<xsl:template match="xacml:Condition">
		<xsl:call-template name="elementToJsonWithKey">
			<!-- Braces already added by the template for whatever Expression in the Condition -->
			<xsl:with-param name="skipBraces" select="true()" />
		</xsl:call-template>
	</xsl:template>

	<!-- Complex elements with name changed -->
	<xsl:template match="xacml:PolicySetDefaults|xacml:PolicyDefaults">
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'Default'" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xacml:PolicyIssuer">
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="outputName" select="'Issuer'" />
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:if test="xacml:Content">
			<xsl:apply-templates select="xacml:Content" />
			<xsl:text disable-output-escaping="yes">,</xsl:text>
		</xsl:if>
		<!-- Note that it is "Attribute" instead of "Attributes" in the standard JSON Profile of XACML. We consider the plural form makes more sense. -->
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'Attrs'" />
			<xsl:with-param name="elementsPath" select="xacml:Attribute" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<!-- Target -->
	<xsl:template match="xacml:Target">
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'Target'" />
			<xsl:with-param name="elementsPath" select="xacml:AnyOf" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xacml:AnyOf|xacml:AllOf">
		<xsl:call-template name="elementsToJsonArray">
			<xsl:with-param name="elementsPath" select="xacml:AllOf|xacml:Match" />
		</xsl:call-template>
	</xsl:template>

	<!-- Apply, used in Condition or as expression in array of Apply args -->
	<xsl:template match="xacml:Apply">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="outputName" select="'FuncCall'" />
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:if test="xacml:Description">
			<xsl:apply-templates select="xacml:Description" />
			<xsl:text disable-output-escaping="yes">,</xsl:text>
		</xsl:if>
		<xsl:apply-templates select="@FunctionId" />
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'ArgExprs'" />
			<!-- position() > 1, because child node "Description" already handled -->
			<xsl:with-param name="elementsPath" select="child::*[local-name() != 'Description']" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:AttributeAssignmentExpression">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:for-each select="@*">
			<xsl:if test="position() > 1">
				<xsl:text disable-output-escaping="yes">,</xsl:text>
			</xsl:if>
			<xsl:apply-templates select="." />
		</xsl:for-each>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="outputName" select="'Expr'" />
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:apply-templates select="child::*" />
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:ObligationExpression">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:apply-templates select="@ObligationId" />
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="outputName" select="'Required'" />
			<xsl:with-param name="valueExpr" select="'true'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'AppliesTo'" />
			<xsl:with-param name="valueExpr" select="@FulfillOn" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'AttrAssignmentExprs'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:AdviceExpression">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:apply-templates select="@AdviceId" />
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="outputName" select="'Required'" />
			<xsl:with-param name="valueExpr" select="'false'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="simple-key-string">
			<xsl:with-param name="outputName" select="'AppliesTo'" />
			<xsl:with-param name="valueExpr" select="@AppliesTo" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'AttrAssignmentExprs'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<xsl:template name="pepActionExpsToJson">
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'PepActionExprs'" />
			<xsl:with-param name="elementsPath" select="xacml:ObligationExpressions/xacml:ObligationExpression|xacml:AdviceExpressions/xacml:AdviceExpression" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="xacml:Rule">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:for-each select="@*|xacml:Description|xacml:Target|xacml:Condition">
			<xsl:apply-templates select="." />
			<xsl:text disable-output-escaping="yes">,</xsl:text>
		</xsl:for-each>
		<xsl:call-template name="pepActionExpsToJson" />
		<xsl:text disable-output-escaping="yes">}}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:PolicySetIdReference|xacml:PolicyIdReference">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="elementToJsonWithKey">
			<xsl:with-param name="outputName" select="'PolicyRef'" />
			<xsl:with-param name="textNodeKey" select="'Id'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:CombinerParameters|xacml:PolicySetCombinerParameters|xacml:PolicyCombinerParameters|xacml:RuleCombinerParameters">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="outputName" select="'ConstArgSeq'" />
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:if test="@PolicySetIdRef|@PolicyIdRef|@RuleIdRef">
			<xsl:apply-templates select="@PolicySetIdRef|@PolicyIdRef|@RuleIdRef" />
			<xsl:text disable-output-escaping="yes">,</xsl:text>
		</xsl:if>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'ParamAssignments'" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">}}</xsl:text>
	</xsl:template>

	<xsl:template match="xacml:PolicySet|xacml:Policy">
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:call-template name="simple-key-literal">
			<xsl:with-param name="outputName" select="'Policy'" />
			<xsl:with-param name="valueExpr" select="''" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">{</xsl:text>
		<xsl:for-each select="@*|xacml:Description|xacml:PolicyIssuer|xacml:PolicySetDefaults|xacml:PolicyDefaults|xacml:Target">
			<xsl:apply-templates select="." />
			<xsl:text disable-output-escaping="yes">,</xsl:text>
		</xsl:for-each>
		<!-- Transform child policy elements -->
		<!-- N.B.: VariableDefinitions are defined separately since they are not part of combiner arguments -->
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'Vars'" />
			<xsl:with-param name="elementsPath"
				select="xacml:VariableDefinition" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="elementsToJsonArrayWithKey">
			<xsl:with-param name="outputName" select="'CombinerArgs'" />
			<xsl:with-param name="elementsPath"
				select="xacml:PolicySet|xacml:Policy|xacml:PolicySetIdReference|xacml:PolicyIdReference|xacml:CombinerParameters|xacml:PolicySetCombinerParameters|xacml:PolicyCombinerParameters|xacml:Rule" />
		</xsl:call-template>
		<xsl:text disable-output-escaping="yes">,</xsl:text>
		<xsl:call-template name="pepActionExpsToJson" />
		<xsl:text disable-output-escaping="yes">}}</xsl:text>
	</xsl:template>

</xsl:stylesheet>