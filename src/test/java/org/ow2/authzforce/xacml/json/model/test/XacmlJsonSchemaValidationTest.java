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

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.ow2.authzforce.xacml.Xacml3JaxbHelper;
import org.ow2.authzforce.xacml.json.model.LimitsCheckingJSONObject;
import org.ow2.authzforce.xacml.json.model.XacmlJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class XacmlJsonSchemaValidationTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(XacmlJsonSchemaValidationTest.class);

	private static final int MAX_JSON_STRING_LENGTH = 65536;

	/*
	 * Max number of child elements - key-value pairs or items - in JSONObject/JSONArray
	 */
	private static final int MAX_JSON_CHILDREN_COUNT = 50000;

	private static final int MAX_JSON_DEPTH = 100;

	/*
	 * Source XACML/JSON files (not generated from XACML/XML files)
	 */
	private static final String[] SRC_XACML_JSON_DATA_DIRECTORY_LOCATIONS = { "src/test/resources/xacml+json.samples/Policies", "src/test/resources/xacml+json.samples/Requests",
	        "src/test/resources/xacml+json.samples/Responses" };

	private static final String[] SRC_XACML_XML_CONFORMANCE_TEST_DATA_PARENT_DIRECTORY_LOCATIONS = { "src/test/resources/xacml+xml.samples/xacml-3.0-ct/mandatory",
	        "src/test/resources/xacml+xml.samples/xacml-3.0-ct/optional" };

	private static final String[] GEN_XACML_XML_CONFORMANCE_TEST_DATA_PARENT_DIRECTORY_LOCATIONS = { "target/generated-test-resources/xacml-xslt-outputs/xacml-3.0-ct/mandatory",
	        "target/generated-test-resources/xacml-xslt-outputs/xacml-3.0-ct/optional" };

	/**
	 * Create test data. Various Requests/Responses in XACML JSON Profile defined format
	 * 
	 * 
	 * @return iterator over test data
	 */
	@DataProvider(name = "xacmlJsonDataProvider")
	public Iterator<Object[]> createData()
	{
		final List<Entry<File, File>> testDataDirLocations = Arrays.stream(SRC_XACML_JSON_DATA_DIRECTORY_LOCATIONS).map(loc -> new AbstractMap.SimpleImmutableEntry<>(new File(loc), (File) null))
		        .collect(Collectors.toList());
		for (int i = 0; i < GEN_XACML_XML_CONFORMANCE_TEST_DATA_PARENT_DIRECTORY_LOCATIONS.length; i++)
		{
			final String loc = GEN_XACML_XML_CONFORMANCE_TEST_DATA_PARENT_DIRECTORY_LOCATIONS[i];
			try
			{
				final int testDataParentDirIndex = i;
				Files.newDirectoryStream(Paths.get(loc)).forEach(testDataDirPath -> testDataDirLocations.add(new AbstractMap.SimpleImmutableEntry<>(testDataDirPath.toFile(),
				        new File(SRC_XACML_XML_CONFORMANCE_TEST_DATA_PARENT_DIRECTORY_LOCATIONS[testDataParentDirIndex], testDataDirPath.getFileName().toString()))));
				i++;
			}
			catch (final IOException e)
			{
				throw new RuntimeException("I/O error opening location '" + loc + "' as directory to iterate over its entries", e);
			}
		}

		return TestDataProvider.createData(testDataDirLocations);
	}

	/**
	 * 
	 * @param xacmlJsonFile XACML/JSON file
	 * @param expectedXacmlXmlFile expected XACML/XML file from conversion of {@code xacmlJsonFile}
	 * @param expectedValid
	 *            true iff validation against JSON schema should succeed
	 * @param actualXacmlXmlFile
	 *            XACML/XML file generated from {@code expectedXacmlXmlFile} by XSLT (during maven generate-test-resources phase) for XACML/XML->XACML/JSON conversion, then XSLT for XACML/JSON ->
	 *            XACML/XML conversion back; may be null if no XACML/XML file exists (expected to have been converted from {@code xacmlJsonFile}, therefore should be equivalent)
	 * @param testCtx testng Test Context
	 * @throws JAXBException error parsing XACML/XML
	 * @throws IOException error reading XACML file
	 */
	@Test(dataProvider = "xacmlJsonDataProvider")
	public void validateXacmlJson(final File xacmlJsonFile, final boolean expectedValid, final File expectedXacmlXmlFile, final File actualXacmlXmlFile, final ITestContext testCtx)
	        throws IOException, JAXBException
	{
		/*
		 * Read properly as UTF-8 to avoid character decoding issues with org.json API
		 */
		try (final BufferedReader reader = Files.newBufferedReader(xacmlJsonFile.toPath(), StandardCharsets.UTF_8))
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
				LOGGER.error("Error validating JSON file: '{}'\n{}", xacmlJsonFile, e.toJSON().toString(2));

				if (expectedValid)
				{
					Assert.fail("Validation against JSON schema failed but expected to pass");
				}
			}

			/*
			 * Validation of XACML/XML output from JSON-to-XML XSLT if any
			 */
			if (actualXacmlXmlFile != null)
			{
				final Object expectedXacmlJaxbObj = Xacml3JaxbHelper.createXacml3Unmarshaller().unmarshal(expectedXacmlXmlFile);
				final Object actualXacmlJaxbObj = Xacml3JaxbHelper.createXacml3Unmarshaller().unmarshal(actualXacmlXmlFile);
				Assert.assertEquals(actualXacmlJaxbObj, expectedXacmlJaxbObj, "Source XACML/XML file and generated XACML/XML file after XML->JSON->XML (XSLT) conversion do not match");
			}
		}
	}
}
