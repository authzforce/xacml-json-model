package org.ow2.authzforce.xacml.json.model.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class TestDataProvider
{
	private static final FileFilter JSON_FILE_FILTER = new FileFilter()
	{

		@Override
		public boolean accept(final File pathname)
		{
			return pathname.isFile() && pathname.getName().endsWith(".json");
		}

	};

	private static final String VALID_TEST_DATA_DIRECTORY_NAME = "valid";
	private static final String INVALID_TEST_DATA_DIRECTORY_NAME = "invalid";

	/**
	 * Provides test data for JSON input validation tests
	 * 
	 * @param relativePathsToMavenProject
	 *            relative paths to directories containing two subdirectories: one name 'valid' that contains JSON sample payloads that must fail the validation tests, one file per payload; and
	 *            another one named 'invalid' that contains JSON sample payloads that must succeed
	 * 
	 * 
	 * @return iterator over test data
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static Iterator<Object[]> createData(final String[] relativePathsToMavenProject) throws URISyntaxException, IOException
	{
		final Collection<Object[]> testParams = new ArrayList<>();
		for (final String dataDirLocation : relativePathsToMavenProject)
		{
			final File dataDir = new File(dataDirLocation);
			if (!dataDir.exists())
			{
				throw new RuntimeException("TEST DATA DIRECTORY NOT FOUND: " + dataDir);
			}

			final File validDataDir = new File(dataDir, VALID_TEST_DATA_DIRECTORY_NAME);
			if (!validDataDir.exists())
			{
				throw new RuntimeException("VALID TEST DATA DIRECTORY NOT FOUND: " + validDataDir);
			}
			for (final File jsonFile : validDataDir.listFiles(JSON_FILE_FILTER))
			{
				// specific test's resources directory location, used as parameter
				// to PdpTest(String)
				testParams.add(new Object[] { jsonFile, true });
			}

			final File invalidDataDir = new File(dataDir, INVALID_TEST_DATA_DIRECTORY_NAME);
			if (!invalidDataDir.exists())
			{
				throw new RuntimeException("INVALID TEST DATA DIRECTORY NOT FOUND: " + invalidDataDir);
			}

			for (final File jsonFile : invalidDataDir.listFiles(JSON_FILE_FILTER))
			{
				// specific test's resources directory location, used as parameter
				// to PdpTest(String)
				testParams.add(new Object[] { jsonFile, false });
			}
		}

		return testParams.iterator();
	}
}
