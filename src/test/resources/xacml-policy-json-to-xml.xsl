<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2019 Thales. This file is part of AuthZForce CE. AuthZForce CE is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published 
	by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. AuthZForce CE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
	even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License 
	along with AuthZForce CE. If not, see <http://www.gnu.org/licenses/>. -->
<!-- Transformation of AuthzForce's XACML/JSON Policy format (as produced by 'xacml-policy-xml-to-json.xsl' stylesheet) back to standard XACML 3.0/XML format. XSLT v3.0 or later is required for handling 
	JSON input (esp. standard JSON to XML conversion): https://www.saxonica.com/html/documentation/functions/fn/json-to-xml.html -->
<!-- Author: Cyril DANGERVILLE -->
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xpath-default-namespace="http://www.w3.org/2005/xpath-functions" expand-text="yes"
	xmlns:xacml="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17">
	<!-- Reference example for JSON to XML transformation: https://www.saxonica.com/papers/xmlprague-2016mhk.pdf -->
	<!-- expand-text option allows to use text value templates in XSLT 3.0 -->
	<xsl:output encoding="UTF-8" indent="yes" method="xml" />

	<!-- This element removes indentation with Xalan 2.7.1 (indentation preserved with Saxon 9.6.0.4). -->
	<!-- <xsl:strip-space elements="*" /> -->

	<!-- Default output Policy Version when undefined in input JSON -->
	<xsl:param name="defPolicyVersion" select="'1.0'" />

	<!-- Default CombinerParameter/AttributeValue/DataType when undefined in input JSON -->
	<xsl:param name="defCombinerParamDataType" select="'http://www.w3.org/2001/XMLSchema#string'" />

	<xsl:param name="inJsonFile" />

	<!-- Global variables -->
	<xsl:variable name="match-funcs-with-string-param0"
		select="('urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with', 'urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with', 'urn:oasis:names:tc:xacml:3.0:function:anyURI-contains', 'urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match', 'urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match','urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match', 'urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match','urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match', 'urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match')" />

	<xsl:template name="xsl:initial-template">
		<!-- <xsl:copy-of select="json-to-xml(.)" /> -->
		<xsl:apply-templates select="json-to-xml(unparsed-text($inJsonFile))" />
	</xsl:template>

	<!-- TODO: these simple templates could be merged together -->
	<xsl:template match="string[@key='issuer' or @key='Issuer']">
		<xsl:attribute name="Issuer" select="." />
	</xsl:template>

	<xsl:template match="string[@key='category' or @key='Category']">
		<xsl:attribute name="Category" select="." />
	</xsl:template>

	<xsl:template match="string[@key='dataType' or @key='DataType']">
		<xsl:attribute name="DataType" select="." />
	</xsl:template>

	<xsl:template match="string[@key='attrId' or @key='AttributeId']">
		<xsl:attribute name="AttributeId" select="." />
	</xsl:template>

	<xsl:template match="boolean[@key='mustBePresent' or @key='MustBePresent']">
		<xsl:attribute name="MustBePresent" select="." />
	</xsl:template>

	<xsl:template match="string[@key='desc' or @key='Desc']">
		<xacml:Description><xsl:value-of select="." /></xacml:Description>
	</xsl:template>

	<xsl:template match="map[@key='var' or @key='Var']">
		<xacml:VariableDefinition VariableId="{string[@key='id' or @key='Id']}" />
	</xsl:template>

	<!-- Expressions -->
	<xsl:template match="map[@key='attrDesignator' or @key='AttributeDesignator']">
		<xacml:AttributeDesignator>
			<xsl:apply-templates select="string[@key='attrId' or @key='AttributeId']" />
			<xsl:apply-templates select="string[@key='issuer' or @key='Issuer']" />
			<xsl:apply-templates select="string[@key='category' or @key='Category']" />
			<xsl:apply-templates select="string[@key='dataType' or @key='DataType']" />
			<xsl:apply-templates select="boolean[@key='mustBePresent' or @key='MustBePresent']" />
		</xacml:AttributeDesignator>
	</xsl:template>

	<xsl:template match="map[@key='attrSelector' or @key='AttributeSelector']">
		<xacml:AttributeSelector Path="{string[@key='path' or @key='Path']}">
			<xsl:if test="string[@key='contextSelectorId' or @key='ContextSelectorId']">
				<xsl:attribute name="ContextSelectorId" select="string[@key='contextSelectorId' or @key='ContextSelectorId']" />
			</xsl:if>
			<xsl:apply-templates select="string[@key='category' or @key='Category']" />
			<xsl:apply-templates select="string[@key='dataType' or @key='DataType']" />
			<xsl:apply-templates select="boolean[@key='mustBePresent' or @key='MustBePresent']" />
		</xacml:AttributeSelector>
	</xsl:template>

	<xsl:template match="map[@key='const' or @key='Const']">
		<xacml:AttributeValue>
			<xsl:apply-templates select="string[@key='dataType' or @key='DataType']" />
			<xsl:value-of select="string[@key='value' or @key='Value']" />
		</xacml:AttributeValue>
	</xsl:template>

	<xsl:template match="string[@key='varRef' or @key='VarRef']">
		<xacml:VariableReference VariableId="{string[@key='dataType' or @key='DataType']}" />
	</xsl:template>

	<xsl:template match="string[@key='func' or @key='Func']">
		<xacml:Function FunctionId="{string[@key='func' or @key='Func']}" />
	</xsl:template>

	<xsl:template match="map[@key='funcCall' or @key='FuncCall']">
		<xacml:Apply FunctionId="{string[@key='funcId' or @key='FuncId']}">
			<xsl:apply-templates select="string[@key='desc' or @key='Desc']" />
			<!-- argExprs -->
			<xsl:apply-templates select="array/map" />
		</xacml:Apply>
	</xsl:template>

	<!-- Target -->
	<xsl:template match="array[@key='target' or @key='Target']">
		<xsl:for-each select="array">
			<xacml:AnyOf>
				<xsl:for-each select="array">
					<xacml:AllOf>
						<xsl:for-each select="map">
							<xacml:Match MatchId="{string[@key='matchFunc' or @key='MatchFunc']}">
								<xacml:AttributeValue>
									<xsl:attribute name="DataType">
									<xsl:choose>
									<!-- AttributeValue's datatype is the same as the AttributeDesignator/Selector's except for certain functions -->
									<xsl:when test="contains-token($match-funcs-with-string-param0, string[@key='matchFunc' or @key='MatchFunc'])">http://www.w3.org/2001/XMLSchema#string</xsl:when>
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:3.0:function:anyURI-contains'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
<!-- 									<xsl:when test="string[@key='matchFunc'] = 'urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match'">http://www.w3.org/2001/XMLSchema#string</xsl:when> -->
									<xsl:otherwise>{map/string[@key='dataType' or @key='DataType']}</xsl:otherwise>
									</xsl:choose>
									</xsl:attribute>
									<xsl:value-of select="string[@key='matchedValue' or @key='MatchedValue']" />
								</xacml:AttributeValue>
								<xsl:apply-templates select="map" />
							</xacml:Match>
						</xsl:for-each>
					</xacml:AllOf>
				</xsl:for-each>
			</xacml:AnyOf>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="array[@key='attrAssignmentExprs' or @key='AttributeAssignmentExprs']/map">
		<xacml:AttributeAssignmentExpression AttributeId="{string[@key='attrId' or @key='AttributeId']}">
			<xsl:if test="string[@key='category' or @key='Category']">
				<xsl:attribute name="Category" select="string[@key='category' or @key='Category']" />
			</xsl:if>
			<xsl:apply-templates select="string[@key='issuer' or @key='Issuer']" />
			<xsl:apply-templates select="map[@key='expr' or @key='Expr']" />
		</xacml:AttributeAssignmentExpression>
	</xsl:template>

	<xsl:template match="array[@key='pepActionExprs' or @key='PepActionExprs']">
		<!-- Obligations first (only boolean attribute is 'required' and true) -->
		<xsl:if test="map[boolean = 'true']">
			<xacml:ObligationExpressions>
				<xsl:for-each select="map[boolean = 'true']">
					<xacml:ObligationExpression ObligationId="{string[@key='id' or @key='Id']}" FulfillOn="{string[@key='appliesTo' or @key='AppliesTo']}">
						<!-- AttributeAssignmentExpressions -->
						<xsl:apply-templates select="array/map" />
					</xacml:ObligationExpression>
				</xsl:for-each>
			</xacml:ObligationExpressions>
		</xsl:if>
		<xsl:if test="map[boolean = 'false']">
			<xacml:AdviceExpressions>
				<xsl:for-each select="map[boolean = 'false']">
					<xacml:AdviceExpression AdviceId="{string[@key='id' or @key='Id']}" AppliesTo="{string[@key='appliesTo' or @key='AppliesTo']}">
						<!-- AttributeAssignmentExpressions -->
						<xsl:apply-templates select="array/map" />
					</xacml:AdviceExpression>
				</xsl:for-each>
			</xacml:AdviceExpressions>
		</xsl:if>
	</xsl:template>

	<xsl:template match="map[@key='rule' or @key='Rule']">
		<xacml:Rule Effect="{string[@key='effect' or @key='Effect']}" RuleId="{string[@key='id' or @key='Id']}">
			<xsl:apply-templates select="string[@key='desc' or @key='Desc']" />
			<!-- Rule's Target is optional (no Target -> inherits enclosing Policy's) -->
			<xsl:if test="array[@key='target' or @key='Target']">
				<xacml:Target>
					<xsl:apply-templates select="array[@key='target' or @key='Target']" />
				</xacml:Target>
			</xsl:if>
			<xsl:if test="map[@key='condition' or @key='Condition']">
				<xacml:Condition>
					<xsl:apply-templates select="map[@key='condition' or @key='Condition']/child::*" />
				</xacml:Condition>
			</xsl:if>
			<xsl:apply-templates select="array[@key='pepActionExprs' or @key='PepActionExprs']" />
		</xacml:Rule>
	</xsl:template>

	<xsl:template match="map[@key='issuer' or @key='Issuer']">
		<xacml:PolicyIssuer>
			<xsl:if test="string[@key='content' or @key='Content']">
				<xacml:Content>
					<xsl:value-of select="string[@key='content' or @key='Content']" />
				</xacml:Content>
			</xsl:if>
			<!-- For each Attribute -->
			<!-- JSON Profile uses 'Attribute' as key to array of Attributes -->
			<xsl:for-each select="array[@key='attrs' or @key='Attribute']/map">
				<xacml:Attribute AttributeId="{string[@key='attrId' or @key='AttributeId']}">
					<xsl:apply-templates select="string[@key='issuer' or @key='Issuer']" />
					<xsl:variable name="dataType" select="string[@key='dataType' or @key='DataType']" />
					<!-- JSON Profile uses 'Value' as key to array of AttributeValues -->
					<xsl:for-each select="array[@key='values' or @key='Value']/string">
						<xacml:AttributeValue DataType="$dataType">
							<xsl:value-of select="." />
						</xacml:AttributeValue>
					</xsl:for-each>
				</xacml:Attribute>
			</xsl:for-each>
		</xacml:PolicyIssuer>
	</xsl:template>

	<xsl:template match="map[@key='defaults' or @key='Defaults']">
		<xacml:XPathVersion>
			<xsl:value-of select="string[@key='xPathVersion' or @key='XPathVersion']" />
		</xacml:XPathVersion>
	</xsl:template>

	<xsl:template match="array[@key='vars' or @key='Vars']/map">
		<xacml:VariableDefinition VariableId="{string[@key='id' or @key='Id']}">
			<!-- Expression -->
			<xsl:apply-templates select="map|string[@key='varRef' or @key='VarRef' or @key='func' or @key='Func']" />
		</xacml:VariableDefinition>
	</xsl:template>

	<xsl:template match="array[@key='paramAssignments' or @key='ParamAssignments']/map">
		<xacml:CombinerParameter ParameterName="{string[@key='paramName' or @key='ParamName']}">
			<xacml:AttributeValue DataType="{string[@key='dataType' or @key='DataType']}">
				<xsl:value-of select="string[@key='value' or @key='Value']" />
			</xacml:AttributeValue>
		</xacml:CombinerParameter>
	</xsl:template>

	<xsl:template match="map[@key='policy' or @key='Policy']">
		<xsl:choose>
			<xsl:when test="array[@key='combinerArgs' or @key='CombinerArgs']/map/map[@key='policy' or @key='Policy']">
				<xacml:PolicySet PolicyCombiningAlgId="{string[@key='combinerId' or @key='CombinerId']}" PolicySetId="{string[@key='id' or @key='Id']}">
					<xsl:attribute name="Version">
					<xsl:choose>
						<xsl:when test="string[@key='version' or @key='Version']">{string[@key='version' or @key='Version']}</xsl:when>
						<xsl:otherwise><xsl:value-of select="$defPolicyVersion" /></xsl:otherwise>
					</xsl:choose>
					</xsl:attribute>
					<xsl:if test="number[@key='maxDelegationDepth' or @key='MaxDelegationDepth']">
						<xsl:attribute name="MaxDelegationDepth" select="number[@key='maxDelegationDepth' or @key='MaxDelegationDepth']" />
					</xsl:if>
					<xsl:apply-templates select="string[@key='desc' or @key='Desc']" />
					<xsl:apply-templates select="map[@key='issuer' or @key='Issuer']" />
					<xsl:if test="map[@key='defaults' or @key='Defaults']">
						<xacml:PolicySetDefaults>
							<xsl:apply-templates select="map[@key='defaults' or @key='Defaults']" />
						</xacml:PolicySetDefaults>
					</xsl:if>
					<xacml:Target>
						<xsl:apply-templates select="array[@key='target' or @key='Target']" />
					</xacml:Target>
					<xsl:for-each select="array[@key='combinerArgs' or @key='CombinerArgs']/map">
						<xsl:choose>
							<xsl:when test="@key='constArgSeq' or @key='ConstArgSeq'">
								<xsl:choose>
									<xsl:when test="string[@key='combinedRef' or @key='CombinedRef']">
										<!-- FIXME: support PolicySetCombinerParameters -->
										<xacml:PolicyCombinerParameters PolicyIdRef="{string[@key='combinedRef' or @key='CombinedRef']}">
											<xsl:apply-templates select="array[@key='paramAssignments' or @key='ParamAssignments']/map" />
										</xacml:PolicyCombinerParameters>
									</xsl:when>
									<xsl:otherwise>
										<xacml:CombinerParameters>
											<xsl:apply-templates select="array[@key='paramAssignments' or @key='ParamAssignments']/map" />
										</xacml:CombinerParameters>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="." />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
					<xsl:apply-templates select="array[@key='pepActionExprs' or @key='PepActionExprs']" />
				</xacml:PolicySet>
			</xsl:when>
			<xsl:otherwise>
				<xacml:Policy RuleCombiningAlgId="{string[@key='combinerId' or @key='CombinerId']}" PolicyId="{string[@key='id' or @key='Id']}">
					<xsl:attribute name="Version">
					<xsl:choose>
						<xsl:when test="string[@key='version' or @key='Version']">{string[@key='version' or @key='Version']}</xsl:when>
						<xsl:otherwise><xsl:value-of select="$defPolicyVersion" /></xsl:otherwise>
					</xsl:choose>
					</xsl:attribute>
					<xsl:if test="number[@key='maxDelegationDepth' or @key='MaxDelegationDepth']">
						<xsl:attribute name="MaxDelegationDepth" select="number[@key='maxDelegationDepth' or @key='MaxDelegationDepth']" />
					</xsl:if>
					<xsl:apply-templates select="string[@key='desc' or @key='Desc']" />
					<xsl:apply-templates select="map[@key='issuer' or @key='Issuer']" />
					<xsl:if test="map[@key='defaults' or @key='Defaults']">
						<xacml:PolicyDefaults>
							<xsl:apply-templates select="map[@key='defaults' or @key='Defaults']" />
						</xacml:PolicyDefaults>
					</xsl:if>
					<xacml:Target>
						<xsl:apply-templates select="array[@key='target' or @key='Target']" />
					</xacml:Target>
					<xsl:apply-templates select="array[@key='vars' or @key='Vars']/map" />
					<xsl:for-each select="array[@key='combinerArgs' or @key='CombinerArgs']/map">
						<xsl:choose>
							<xsl:when test="@key='constArgSeq' or @key='ConstArgSeq'">
								<xsl:choose>
									<xsl:when test="string[@key='combinedRef' or @key='CombinedRef']">
										<xacml:RuleCombinerParameters RuleIdRef="{string[@key='combinedRef' or @key='CombinedRef']}">
											<xsl:apply-templates select="array[@key='paramAssignments' or @key='ParamAssignments']/map" />
										</xacml:RuleCombinerParameters>
									</xsl:when>
									<xsl:otherwise>
										<xacml:CombinerParameters>
											<xsl:apply-templates select="array[@key='paramAssignments' or @key='ParamAssignments']/map" />
										</xacml:CombinerParameters>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="." />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
					<xsl:apply-templates select="array[@key='pepActionExprs' or @key='PepActionExprs']" />
				</xacml:Policy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="map[@key='policyRef' or @key='PolicyRef']">
		<!-- FIXME: support PolicySetIdReference -->
		<xacml:PolicyIdReference>
			<xsl:if test="string[@key='version' or @key='Version']">
				<xsl:attribute name="Version" select="string[@key='version' or @key='Version']" />
			</xsl:if>
			<xsl:if test="string[@key='earliestVersion' or @key='EarliestVersion']">
				<xsl:attribute name="EarliestVersion" select="string[@key='earliestVersion' or @key='EarliestVersion']" />
			</xsl:if>
			<xsl:if test="string[@key='latestVersion' or @key='LatestVersion']">
				<xsl:attribute name="LatestVersion" select="string[@key='latestVersion' or @key='LatestVersion']" />
			</xsl:if>
			<xsl:value-of select="string[@key='id' or @key='Id']" />
		</xacml:PolicyIdReference>
	</xsl:template>

</xsl:stylesheet>