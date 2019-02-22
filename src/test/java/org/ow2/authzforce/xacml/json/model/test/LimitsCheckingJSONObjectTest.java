/**
 * Copyright 2012-2019 THALES.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.ow2.authzforce.xacml.json.model.LimitsCheckingJSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LimitsCheckingJSONObjectTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LimitsCheckingJSONObjectTest.class);

	private static final int MAX_JSON_STRING_LENGTH = 10;

	/*
	 * Max number of child elements - key-value pairs or items - in JSONObject/JSONArray
	 */
	private static final int MAX_JSON_CHILDREN_COUNT = 2;

	private static final int MAX_JSON_DEPTH = 2;

	private static final String[] TEST_DATA_DIRECTORY_LOCATIONS = { "src/test/resources/json.samples" };

	/**
	 * Create test data. Various JSON files.
	 * 
	 * 
	 * @return iterator over test data
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@DataProvider(name = "jsonDataProvider")
	public Iterator<Object[]> createData() throws URISyntaxException, IOException
	{
		return TestDataProvider.createData(TEST_DATA_DIRECTORY_LOCATIONS);
	}

	@Test(dataProvider = "jsonDataProvider")
	public void test(final File jsonFile, final boolean expectedValid, final ITestContext testCtx) throws FileNotFoundException, IOException, JSONException
	{
		/*
		 * Read properly as UTF-8 to avoid character decoding issues with org.json API
		 */
		try (final InputStream in = new FileInputStream(jsonFile))
		{
			try
			{
				final JSONObject json = new LimitsCheckingJSONObject(in, MAX_JSON_STRING_LENGTH, MAX_JSON_CHILDREN_COUNT, MAX_JSON_DEPTH);
				if (!expectedValid)
				{
					Assert.fail("Validation against JSON schema succeeded but expected to fail.  JSON = " + json);
				}
			}
			catch (final IllegalArgumentException e)
			{
				if (expectedValid)
				{
					Assert.fail("Validation failed but expected to pass.", e);
				}
				else
				{
					LOGGER.debug("Limits violation detected: ", e);
				}
			}
		}
	}
}
