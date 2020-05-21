/**
 * Copyright 2012-2020 THALES.
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
import java.util.HashMap;
import java.util.Map;

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
public final class XacmlJsonUtils
{
	private XacmlJsonUtils()
	{
		// hide constructor
	}

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

	static
	{
		final Map<String, String> mutableCatalogMap = new HashMap<>();
		mutableCatalogMap.put("http://authzforce.github.io/xacml-json-profile-model/schemas/1/common-std.schema.json", "classpath:org/ow2/authzforce/xacml/json/model/common-std.schema.json");
		mutableCatalogMap.put("http://authzforce.github.io/xacml-json-profile-model/schemas/1/common-ng.schema.json", "classpath:org/ow2/authzforce/xacml/json/model/common-ng.schema.json");
		final SchemaClient schemaClient = new SpringBasedJsonSchemaClient(mutableCatalogMap);
		try (InputStream inputStream = SpringBasedJsonSchemaClient.class.getResourceAsStream("Request.schema.json"))
		{
			final JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
			// final SchemaLoader schemaLoader = schemaLoaderBuilder.schemaJson(rawSchema).build();
			REQUEST_SCHEMA = SchemaLoader.load(rawSchema, schemaClient); // schemaLoader.load().build();
		}
		catch (final IOException e)
		{
			throw new RuntimeException(e);
		}

		try (InputStream inputStream = SpringBasedJsonSchemaClient.class.getResourceAsStream("Response.schema.json"))
		{
			final JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
			RESPONSE_SCHEMA = SchemaLoader.load(rawSchema, schemaClient);
		}
		catch (final IOException e)
		{
			throw new RuntimeException(e);
		}

		try (InputStream inputStream = SpringBasedJsonSchemaClient.class.getResourceAsStream("Policy.schema.json"))
		{
			final JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
			POLICY_SCHEMA = SchemaLoader.load(rawSchema, schemaClient);
		}
		catch (final IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Canonicalize a XACML/JSON response, typically for comparison with another one. In particular, it removes every Result's status as we choose to ignore the Status. Indeed, a PDP implementation
	 * might return a perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would not expect.
	 * 
	 * WARNING: this method modifies the content of {@code xacmlJsonResponse} directly
	 * 
	 * @param xacmlJsonResponse
	 *            input XACML Response
	 * @return canonicalized response
	 */
	public static JSONObject canonicalizeResponse(final JSONObject xacmlJsonResponse)
	{
		/*
		 * We iterate over all results, because for each results, we don't compare everything. In particular, we choose to ignore the StatusMessage, StatusDetail and any nested StatusCode. Indeed, a
		 * PDP implementation might return a perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would not expect.
		 */
		for (final Object resultObj : xacmlJsonResponse.getJSONArray("Response"))
		{
			final JSONObject resultJsonObj = (JSONObject) resultObj;
			// Status
			final JSONObject statusJsonObj = resultJsonObj.optJSONObject("Status");
			if (statusJsonObj != null)
			{
				// remove Status if StatusCode OK (optional, default implicit therefore useless)
				final JSONObject statusCodeJsonObj = statusJsonObj.getJSONObject("StatusCode");
				final String statusCodeVal = statusCodeJsonObj.getString("Value");
				if (statusCodeVal.equals("urn:oasis:names:tc:xacml:1.0:status:ok"))
				{
					// Status OK is useless, simplify
					resultJsonObj.remove("Status");
				}
				else
				{
					// remove any nested status code, StatusMessage and StatusDetail
					statusCodeJsonObj.remove("StatusCode");
					statusJsonObj.remove("StatusMessage");
					statusJsonObj.remove("StatusDetail");
				}
			}

			// remove empty Category array if any
			final JSONArray jsonArrayOfAttCats = resultJsonObj.optJSONArray("Category");
			if (jsonArrayOfAttCats != null)
			{
				if (jsonArrayOfAttCats.length() == 0)
				{
					resultJsonObj.remove("Category");
				}
				else
				{
					/*
					 * Remove any IncludeInResult property which is useless and optional in XACML/JSON. (NB.: IncludeInResult is mandatory in XACML/XML schema but optional in JSON Profile).
					 */
					for (final Object attCatJson : jsonArrayOfAttCats)
					{
						assert attCatJson instanceof JSONObject;
						final JSONObject attCatJsonObj = (JSONObject) attCatJson;
						final JSONArray jsonArrayOfAtts = attCatJsonObj.optJSONArray("Attribute");
						if (jsonArrayOfAtts != null)
						{
							jsonArrayOfAtts.forEach(attJson -> {
								assert attJson instanceof JSONObject;
								final JSONObject attJsonObj = (JSONObject) attJson;
								attJsonObj.remove("IncludeInResult");
							});
						}
					}
				}
			}
		}

		return xacmlJsonResponse;
	}
}
