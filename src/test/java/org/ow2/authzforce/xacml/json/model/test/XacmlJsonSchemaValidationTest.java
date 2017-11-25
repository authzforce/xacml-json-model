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
import org.ow2.authzforce.xacml.json.model.Xacml3JsonUtils;
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

	private static final String[] XACML_DATA_DIRECTORY_LOCATIONS = { "src/test/resources/xacml.samples/Requests", "src/test/resources/xacml.samples/Responses" };

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
			if (json.has("Request"))
			{
				schema = Xacml3JsonUtils.REQUEST_SCHEMA;
			}
			else if (json.has("Response"))
			{
				schema = Xacml3JsonUtils.RESPONSE_SCHEMA;
			}
			else
			{
				throw new IllegalArgumentException("Invalid XACML JSON file. Expected root key: \"Request\" or \"Response\"");
			}

			try
			{
				schema.validate(json); // throws a ValidationException if this object is invalid
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
