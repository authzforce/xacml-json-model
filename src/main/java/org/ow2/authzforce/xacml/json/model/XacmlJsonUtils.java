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

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.*;

/**
 * Instances of JSON schema as defined by JSON Profile of XACML 3.0
 *
 */
public final class XacmlJsonUtils
{
    private static final SchemaClient CLASSPATH_AWARE_SCHEMA_CLIENT = SchemaClient.classPathAwareClient();

    private static Schema loadSchema(String schemaFilenameRelativeToThisClass)
    {
        try (InputStream inputStream = XacmlJsonUtils.class.getResourceAsStream(schemaFilenameRelativeToThisClass))
        {
            final JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            return SchemaLoader.builder().schemaJson(rawSchema).schemaClient(CLASSPATH_AWARE_SCHEMA_CLIENT).resolutionScope("classpath://org/ow2/authzforce/xacml/json/model/").build().load().build();
        } catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
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
        REQUEST_SCHEMA = loadSchema("Request.schema.json");
        RESPONSE_SCHEMA = loadSchema("Response.schema.json");
        POLICY_SCHEMA = loadSchema("Policy.schema.json");
    }

    /*

     */
    private static void canonicalizeObligationsOrAdvice(JSONObject xacmlResult, String obligationsOrAdviceKey, boolean floatWithTrailingZeroToInt)
    {
        final JSONArray obligationsOrAdvice = xacmlResult.optJSONArray(obligationsOrAdviceKey);
        if(obligationsOrAdvice != null) {

            if (obligationsOrAdvice.length() == 0)
            {
                xacmlResult.remove(obligationsOrAdviceKey);
            } else
            {
                for (final Object obligation : obligationsOrAdvice)
                {
                    assert obligation instanceof JSONObject;
                    final JSONObject obligationJsonObj = (JSONObject) obligation;
                    final JSONArray jsonArrayOfAtts = obligationJsonObj.optJSONArray("AttributeAssignment");
                    if (jsonArrayOfAtts != null)
                    {
                        if (jsonArrayOfAtts.length() == 0)
                        {
                            obligationJsonObj.remove("AttributeAssignment");
                        } else
                        {
                            for (final Object attJson : jsonArrayOfAtts)
                            {
                                assert attJson instanceof JSONObject;
                                final JSONObject attJsonObj = (JSONObject) attJson;
                                if (floatWithTrailingZeroToInt)
                                {
                                    floatWithTrailingZeroToInt(attJsonObj);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Same as {@link #canonicalizeResponse(JSONObject, boolean)} but with second parameter set to false.
     *
     * @param xacmlJsonResponse
     *            input XACML Response
     * @return canonicalized response
     */
    public static JSONObject canonicalizeResponse(final JSONObject xacmlJsonResponse)
    {
        return canonicalizeResponse(xacmlJsonResponse, false);
    }

    /**
     * Canonicalize a XACML/JSON response, typically for comparison with another one. In particular, it removes every Result's status as we choose to ignore the Status. Indeed, a PDP implementation
     * might return a perfectly XACML-compliant response but with extra StatusCode/Message/Detail that we would not expect.
     *
     * WARNING: this method modifies the content of {@code xacmlJsonResponse} directly
     * FIXME: waiting for 'org.everity.json.schema' to upgrade dependency 'org.json:json' to v20210307 or later in order to fix https://github.com/stleary/JSON-java/issues/589
     *
     * @param xacmlJsonResponse
     *            input XACML Response
     * @param floatWithTrailingZeroToInt true iff floats with trailing zero (after decimal point) in AttributeValues are converted to Integer, this is originally a workaround for <a href="https://github.com/stleary/JSON-java/issues/589">issue #589 on org.json:json library</a>, used by our dependency 'org.everity.json.schema', which has been fixed in v20210307 of org.json:json; but still waiting for 'org.everity.json.schema' to upgrade.
     * @return canonicalized response
     */
    public static JSONObject canonicalizeResponse(final JSONObject xacmlJsonResponse, boolean floatWithTrailingZeroToInt)
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
                } else
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
                } else
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
                            if (jsonArrayOfAtts.length() == 0)
                            {
                                attCatJsonObj.remove("Attribute");
                            } else
                            {
                                for (final Object attJson : jsonArrayOfAtts)
                                {
                                    assert attJson instanceof JSONObject;
                                    final JSONObject attJsonObj = (JSONObject) attJson;
                                    attJsonObj.remove("IncludeInResult");
                                    if (floatWithTrailingZeroToInt)
                                    {
                                        floatWithTrailingZeroToInt(attJsonObj);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Handle attribute values in Obligations and AssociatedAdvice if floatWithTrailingZeroToInt
            canonicalizeObligationsOrAdvice(resultJsonObj, "Obligations", floatWithTrailingZeroToInt);
            canonicalizeObligationsOrAdvice(resultJsonObj, "AssociatedAdvice", floatWithTrailingZeroToInt);

        }

        return xacmlJsonResponse;
    }

    /*
    Returns a number if input was a Double with zero fraction, therefore converted to an integer type (BigInteger, Integer, Long, Short); else null

    FIXME: workaround for this issue: https://github.com/stleary/JSON-java/issues/589
     */
    private static Number floatWithTrailingZeroToInt(Object input)
    {
        if (input instanceof JSONObject)
        {
            final JSONObject json = (JSONObject) input;
            final Map<String, Number> modifiedProperties = new HashMap<>();
            json.keySet().forEach(key ->
            {
                final Number convertedIfNonNull = floatWithTrailingZeroToInt(json.get(key));
                if (convertedIfNonNull != null)
                {
                    // Double value to be changed to an integer
                    modifiedProperties.put(key, convertedIfNonNull);
                }
            });
            // apply modifications if any
            modifiedProperties.forEach(json::put);
            return null;
        }

        if (input instanceof JSONArray)
        {
            final JSONArray json = (JSONArray) input;
            final Deque<Map.Entry<Integer, Number>> modifiedItems = new ArrayDeque<>(json.length());
            int index = 0;
            for (final Object item : json)
            {
                final Number convertedIfNonNull = floatWithTrailingZeroToInt(item);
                if (convertedIfNonNull != null)
                {
                    // Double value to be changed to an integer
                    modifiedItems.addLast(new AbstractMap.SimpleImmutableEntry<>(index, convertedIfNonNull));
                }
                index++;
            }
            // apply modifications if any
            modifiedItems.forEach(e -> json.put(e.getKey(), e.getValue()));
            return null;
        }

        if (input instanceof Double)
        {
            // FIXME: workaround for this issue: https://github.com/stleary/JSON-java/issues/589
            // if there is some trailing zero, this Double is considered equivalent to an int
            // The corresponding int is obtained after serializing/deserializing
            final String serialized = JSONObject.valueToString(input);
            final Object deserialized = JSONObject.stringToValue(serialized);
            if (!(deserialized instanceof Double))
            {
                // value was converted to an int
                assert deserialized instanceof Number;
                return (Number) deserialized;
            }
        }

        // nothing to change
        return null;
    }

    private XacmlJsonUtils()
    {
        // hide constructor
    }

    /*
    public static void main(String[] args) throws FileNotFoundException
    {
        final JSONObject jsonWithTrailing0 = XacmlJsonUtils.canonicalizeResponse(new JSONObject(new JSONTokener(new FileInputStream(new File("~/git/authzforce-ce-server/webapp/src/test/resources/xacml.samples/pdp/GeoJSON_good/response.json")))), true);
        System.out.println(jsonWithTrailing0.toString());
    }
     */
}
