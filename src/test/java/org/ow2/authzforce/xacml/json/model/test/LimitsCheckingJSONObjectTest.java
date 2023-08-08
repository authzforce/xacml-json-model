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
package org.ow2.authzforce.xacml.json.model.test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

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
	 * @return iterator over test data
	 */
	@DataProvider(name = "jsonDataProvider")
	public Iterator<Object[]> createData()
	{
		return TestDataProvider.createData(Arrays.stream(TEST_DATA_DIRECTORY_LOCATIONS).map(loc -> new AbstractMap.SimpleImmutableEntry<>(new File(loc), (File) null)).collect(Collectors.toList()));
	}

	/**
	 * 
	 * @param xacmlJsonFile XACML/JSON file (Request or Response)
	 * @param expectedValid
	 *            true iff validation against JSON schema should succeed
	 * @param srcXacmlXmlFile original source XACML/XML file
	 * @param genXacmlXmlFileFromXslt
	 *            Parameter not used by this test method but value returned anyway by the dataProvider (shared with other tests).
	 * @param testCtx testng Test Context
	 * @throws JSONException error parsing JSON
	 * @throws IOException error reading input XACML files
	 */
	@Test(dataProvider = "jsonDataProvider")
	public void test(final File xacmlJsonFile, final boolean expectedValid, final File srcXacmlXmlFile, final File genXacmlXmlFileFromXslt, final ITestContext testCtx)
	        throws IOException, JSONException
	{
		/*
		 * Read properly as UTF-8 to avoid character decoding issues with org.json API
		 */
		try (final BufferedReader fileReader = Files.newBufferedReader(xacmlJsonFile.toPath(), StandardCharsets.UTF_8))
		{
			try
			{
				final JSONObject json = new LimitsCheckingJSONObject(fileReader, MAX_JSON_STRING_LENGTH, MAX_JSON_CHILDREN_COUNT, MAX_JSON_DEPTH);
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
