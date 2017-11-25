package org.ow2.authzforce.xacml.json.model;

import java.io.IOException;
import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.common.collect.ImmutableMap;

/**
 * Instances of JSON schema as defined by JSON Profile of XACML 3.0
 *
 */
public final class Xacml3JsonUtils
{
	private Xacml3JsonUtils()
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

	static
	{
		final SchemaClient schemaClient = new SpringBasedJsonSchemaClient(ImmutableMap.of("http://authzforce.github.io/xacml-json-profile-model/schemas/1/common.schema.json",
				"classpath:org/ow2/authzforce/xacml/json/model/common.schema.json"));
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
			final JSONArray jsonArray = resultJsonObj.optJSONArray("Category");
			if (jsonArray != null && jsonArray.length() == 0)
			{
				resultJsonObj.remove("Category");
			}
		}

		return xacmlJsonResponse;
	}
}
