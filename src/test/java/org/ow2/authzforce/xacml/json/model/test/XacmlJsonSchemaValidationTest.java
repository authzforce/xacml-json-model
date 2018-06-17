/**
 * Copyright 2012-2018 Thales Services SAS.
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
package org.ow2.authzforce.xacml.json.model.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.ow2.authzforce.xacml.json.model.LimitsCheckingJSONObject;
import org.ow2.authzforce.xacml.json.model.XacmlJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class XacmlJsonSchemaValidationTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(XacmlJsonSchemaValidationTest.class);

	private static final int MAX_JSON_STRING_LENGTH = 65536;

	/*
	 * Max number of child elements - key-value pairs or items - in JSONObject/JSONArray
	 */
	private static final int MAX_JSON_CHILDREN_COUNT = 50000;

	private static final int MAX_JSON_DEPTH = 100;

	private static final String[] XACML_DATA_DIRECTORY_LOCATIONS = { "src/test/resources/xacml.samples/Policies", "src/test/resources/xacml.samples/Requests",
	        "src/test/resources/xacml.samples/Responses" };

	/**
	 * Create test data. Various Requests/Responses in XACML JSON Profile defined format
	 * 
	 * 
	 * @return iterator over test data
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@DataProvider(name = "xacmlJsonDataProvider")
	public Iterator<Object[]> createData() throws URISyntaxException, IOException
	{
		return TestDataProvider.createData(XACML_DATA_DIRECTORY_LOCATIONS);
	}

	@Test(dataProvider = "xacmlJsonDataProvider")
	public void validateXacmlJson(final File xacmlJsonFile, final boolean expectedValid, final ITestContext testCtx) throws FileNotFoundException, IOException
	{
		/*
		 * Read properly as UTF-8 to avoid character decoding issues with org.json API
		 */
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(xacmlJsonFile), StandardCharsets.UTF_8)))
		{
			final JSONObject json = new LimitsCheckingJSONObject(reader, MAX_JSON_STRING_LENGTH, MAX_JSON_CHILDREN_COUNT, MAX_JSON_DEPTH);
			final Schema schema;
			final JSONObject jsonToValidate;
			if (json.has("Request"))
			{
				schema = XacmlJsonUtils.REQUEST_SCHEMA;
				jsonToValidate = json;
			}
			else if (json.has("Response"))
			{
				schema = XacmlJsonUtils.RESPONSE_SCHEMA;
				jsonToValidate = json;
			}
			else if (json.has("policy"))
			{
				schema = XacmlJsonUtils.POLICY_SCHEMA;
				jsonToValidate = json.getJSONObject("policy");
			}
			else
			{
				throw new IllegalArgumentException("Invalid XACML JSON file. Expected root key: \"Request\" or \"Response\" or \"policy\"");
			}

			try
			{
				schema.validate(jsonToValidate); // throws a ValidationException if this object is invalid
				if (!expectedValid)
				{
					Assert.fail("Validation against JSON schema succeeded but expected to fail");
				}
			}
			catch (final ValidationException e)
			{
				LOGGER.debug(e.toJSON().toString(4));

				if (expectedValid)
				{
					Assert.fail("Validation against JSON schema failed but expected to pass");
				}
			}
		}
	}
}
