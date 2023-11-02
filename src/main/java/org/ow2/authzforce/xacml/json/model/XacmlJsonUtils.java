/*
 * Copyright 2012-2023 THALES.
 *
 * This file is part of AuthzForce CE.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.authzforce.xacml.json.model;

import java.io.IOException;
import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Instances of JSON schema as defined by JSON Profile of XACML 3.0
 *
 */
public final class XacmlJsonUtils {
	/**
	 * JSON schema for validating Requests according to JSON Profile of XACML 3.0
	 */
	public static final Schema REQUEST_SCHEMA;

	/**
	 * JSON schema for validating Responses according to JSON Profile of XACML 3.0
	 */
	public static final Schema RESPONSE_SCHEMA;

	/**
	 * JSON schema for validating Policies according to AuthzForce/JSON policy format for XACML Policy(Set) (see Policy.schema.json)
	 */
	public static final Schema POLICY_SCHEMA;

	private static final SchemaClient CLASSPATH_AWARE_SCHEMA_CLIENT = SchemaClient.classPathAwareClient();

	static {
		REQUEST_SCHEMA = loadSchema("Request.schema.json");
		RESPONSE_SCHEMA = loadSchema("Response.schema.json");
		POLICY_SCHEMA = loadSchema("Policy.schema.json");
	}

	private XacmlJsonUtils() {
		// hide constructor
	}

	private static Schema loadSchema(String schemaFilenameRelativeToThisClass) {
		try (InputStream inputStream = XacmlJsonUtils.class.getResourceAsStream(schemaFilenameRelativeToThisClass)) {
			if (inputStream == null) {
				throw new RuntimeException("No resource name '" + schemaFilenameRelativeToThisClass + "' found on the classpath");
			}

			final JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
			return SchemaLoader.builder().schemaJson(rawSchema).schemaClient(CLASSPATH_AWARE_SCHEMA_CLIENT).resolutionScope("classpath://org/ow2/authzforce/xacml/json/model/").build().load().build();
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*

	 */
	private static void canonicalizeObligationsOrAdvice(JSONObject xacmlResult, String obligationsOrAdviceKey) {
		final JSONArray obligationsOrAdvice = xacmlResult.optJSONArray(obligationsOrAdviceKey);
		if (obligationsOrAdvice != null) {
			if (obligationsOrAdvice.isEmpty()) {
				xacmlResult.remove(obligationsOrAdviceKey);
			}
			else {
				for (final Object obligation : obligationsOrAdvice) {
					assert obligation instanceof JSONObject;
					final JSONObject obligationJsonObj = (JSONObject) obligation;
					final JSONArray jsonArrayOfAtts = obligationJsonObj.optJSONArray("AttributeAssignment");
					if (jsonArrayOfAtts != null && jsonArrayOfAtts.isEmpty()) {
						obligationJsonObj.remove("AttributeAssignment");
					}
				}
			}
		}
	}

	/**
	 * Canonicalize a XACML/JSON response, typically for comparison with another one. In particular, it removes every Result's status as we choose to ignore the Status. Indeed, a PDP implementation
	 * might return a perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would not expect.
	 *<p>
	 * WARNING: this method modifies the content of {@code xacmlJsonResponse} directly
	 *</p>
	 * @param xacmlJsonResponse
	 *            input XACML Response
	 * @return canonicalized response
	 */
	public static JSONObject canonicalizeResponse(final JSONObject xacmlJsonResponse) {
		/*
		 * We iterate over all results, because for each result, we don't compare everything. In particular, we choose to ignore the StatusMessage, StatusDetail and any nested StatusCode. Indeed, a
		 * PDP implementation might return a perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would not expect.
		 */
		for (final Object resultObj : xacmlJsonResponse.getJSONArray("Response")) {
			final JSONObject resultJsonObj = (JSONObject) resultObj;
			// Status
			final JSONObject statusJsonObj = resultJsonObj.optJSONObject("Status");
			if (statusJsonObj != null) {
				// remove Status if StatusCode OK (optional, default implicit therefore useless)
				final JSONObject statusCodeJsonObj = statusJsonObj.getJSONObject("StatusCode");
				final String statusCodeVal = statusCodeJsonObj.getString("Value");
				if (statusCodeVal.equals("urn:oasis:names:tc:xacml:1.0:status:ok")) {
					// Status OK is useless, simplify
					resultJsonObj.remove("Status");
				}
				else {
					// remove any nested status code, StatusMessage and StatusDetail
					statusCodeJsonObj.remove("StatusCode");
					statusJsonObj.remove("StatusMessage");
					statusJsonObj.remove("StatusDetail");
				}
			}

			// remove empty Category array if any
			final JSONArray jsonArrayOfAttCats = resultJsonObj.optJSONArray("Category");
			if (jsonArrayOfAttCats != null) {
				if (jsonArrayOfAttCats.isEmpty()) {
					resultJsonObj.remove("Category");
				}
				else {
					/*
					 * Remove any IncludeInResult property which is useless and optional in XACML/JSON. (NB.: IncludeInResult is mandatory in XACML/XML schema but optional in JSON Profile).
					 */
					for (final Object attCatJson : jsonArrayOfAttCats) {
						assert attCatJson instanceof JSONObject;
						final JSONObject attCatJsonObj = (JSONObject) attCatJson;
						final JSONArray jsonArrayOfAtts = attCatJsonObj.optJSONArray("Attribute");
						if (jsonArrayOfAtts != null) {
							if (jsonArrayOfAtts.isEmpty()) {
								attCatJsonObj.remove("Attribute");
							}
							else {
								for (final Object attJson : jsonArrayOfAtts) {
									assert attJson instanceof JSONObject;
									final JSONObject attJsonObj = (JSONObject) attJson;
									attJsonObj.remove("IncludeInResult");
								}
							}
						}
					}
				}
			}

			// Handle attribute values in Obligations and AssociatedAdvice if floatWithTrailingZeroToInt
			canonicalizeObligationsOrAdvice(resultJsonObj, "Obligations");
			canonicalizeObligationsOrAdvice(resultJsonObj, "AssociatedAdvice");
		}

		return xacmlJsonResponse;
	}

    /*
    public static void main(String[] args) throws FileNotFoundException
    {
        final JSONObject jsonWithTrailing0 = XacmlJsonUtils.canonicalizeResponse(new JSONObject(new JSONTokener(new FileInputStream(new File("~/git/authzforce-ce-server/webapp/src/test/resources/xacml.samples/pdp/GeoJSON_good/response.json")))), true);
        System.out.println(jsonWithTrailing0.toString());
    }
     */
}
