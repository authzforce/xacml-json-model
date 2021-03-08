/*
 * Copyright 2012-2021 THALES.
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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

public final class TestDataProvider
{
	private static final FileFilter JSON_FILE_FILTER = pathname -> pathname.isFile() && pathname.getName().endsWith(".json");

	private static final String VALID_TEST_DATA_DIRECTORY_NAME = "valid";
	private static final String INVALID_TEST_DATA_DIRECTORY_NAME = "invalid";

	private static final int JSON_FILE_EXT_LEN = ".json".length();

	/**
	 * Provides test data for JSON input validation tests
	 * 
	 * @param pathsToXacmlJsonAndSrcXmlFileDirsRelativeToMavenProject
	 *            (jsonDir, xmlDir) pairs where jsonDir is a relative path (relative to Maven Project root) of a directory containing two subdirectories: one name 'valid' that contains XACML/JSON
	 *            sample payloads that must fail the validation tests, one file per payload; and another one named 'invalid' that contains XACML/JSON sample payloads that must succeed; and where
	 *            xmlDir, if not null, is the source directory (with 'valid' and 'invalid' sub-directories) of XACML/XML files from which the corresponding XACML/JSON files in jsonDir were generated
	 *            (by XSLT in maven generated-test-resources phase) if these JSON files were actually generated from XML, else xmlDir is null
	 * 
	 * 
	 * @return iterator over test data
	 */
	public static Iterator<Object[]> createData(final Iterable<Entry<File, File>> pathsToXacmlJsonAndSrcXmlFileDirsRelativeToMavenProject)
	{
		final Collection<Object[]> testParams = new ArrayList<>();
		for (final Entry<File, File> xacmlJsonAndSrcXmlDirsEntry : pathsToXacmlJsonAndSrcXmlFileDirsRelativeToMavenProject)
		{
			final File xacmlJsonFilesDirRelToMvnProj = xacmlJsonAndSrcXmlDirsEntry.getKey();
			if (!xacmlJsonFilesDirRelToMvnProj.exists())
			{
				throw new RuntimeException("TEST XACML/JSON FILES DIRECTORY NOT FOUND: " + xacmlJsonFilesDirRelToMvnProj);
			}

			final File validXacmlJsonFilesDir;
			final File validXacmlJsonFilesSubDir = new File(xacmlJsonFilesDirRelToMvnProj, VALID_TEST_DATA_DIRECTORY_NAME);
			if (validXacmlJsonFilesSubDir.exists())
			{
				validXacmlJsonFilesDir = validXacmlJsonFilesSubDir;
			}
			else
			{
				/*
				 * No valid/invalid sub-directory. The directory 'xacmlJsonFilesDirRelToMvnProj' itself is considered all valid.
				 */
				validXacmlJsonFilesDir = xacmlJsonFilesDirRelToMvnProj;
			}

			/*
			 * Source XACML/XML files directory, optional, i.e. may be null if the JSON files are source files already (not generated from XACML/XML files)
			 */
			final File srcXmlFilesDirRelToMvnProj = xacmlJsonAndSrcXmlDirsEntry.getValue();
			if (xacmlJsonFilesDirRelToMvnProj.equals(srcXmlFilesDirRelToMvnProj))
			{
				throw new IllegalArgumentException("Relative paths to XACML/JSON and XACML/XML directories cannot be the same: " + xacmlJsonFilesDirRelToMvnProj);
			}

			final File validSrcXmlFilesDir;
			if (srcXmlFilesDirRelToMvnProj == null)
			{
				validSrcXmlFilesDir = null;
			}
			else
			{
				final File validSrcXmlFilesSubDir = new File(srcXmlFilesDirRelToMvnProj, VALID_TEST_DATA_DIRECTORY_NAME);
				if (validSrcXmlFilesSubDir.exists())
				{
					validSrcXmlFilesDir = validSrcXmlFilesSubDir;
				}
				else
				{
					/*
					 * No valid/invalid sub-directory. The directory 'srcXmlFilesDirRelToMvnProj' itself is considered all valid.
					 */
					validSrcXmlFilesDir = srcXmlFilesDirRelToMvnProj;
				}
			}

			for (final File jsonFile : Objects.requireNonNull(validXacmlJsonFilesDir.listFiles(JSON_FILE_FILTER)))
			{
				/*
				 * Specific test's resources directory location, used as parameter to PdpTest(String). Check for a XML version of the file.
				 */
				final String xmlFilename = jsonFile.getName().substring(0, jsonFile.getName().length() - JSON_FILE_EXT_LEN) + ".xml";
				final File srcXmlFile = validSrcXmlFilesDir == null ? null : new File(validSrcXmlFilesDir, xmlFilename);
				/*
				 * Output XML file from XML-to-JSON-to-XML back XSL transformation if any, next to the JSON file
				 */
				final File outXmlFromJsonXslt = srcXmlFile == null ? null : new File(validXacmlJsonFilesDir, xmlFilename);
				testParams.add(new Object[] { jsonFile, true, srcXmlFile != null && srcXmlFile.exists() ? srcXmlFile : null,
				        outXmlFromJsonXslt != null && outXmlFromJsonXslt.exists() ? outXmlFromJsonXslt : null });
			}

			final File invalidXacmlJsonFilesDataDir = new File(xacmlJsonFilesDirRelToMvnProj, INVALID_TEST_DATA_DIRECTORY_NAME);
			if (!invalidXacmlJsonFilesDataDir.exists())
			{
				System.out.println("WARNING: skipping 'invalid' TEST DATA DIRECTORY (NOT FOUND): " + invalidXacmlJsonFilesDataDir);
				continue;
			}

			/*
			 * If the "invalid" sub-directory exists...
			 */
			final File invalidSrcXmlFilesDir = srcXmlFilesDirRelToMvnProj == null ? null : new File(srcXmlFilesDirRelToMvnProj, INVALID_TEST_DATA_DIRECTORY_NAME);

			for (final File jsonFile : Objects.requireNonNull(invalidXacmlJsonFilesDataDir.listFiles(JSON_FILE_FILTER)))
			{
				/*
				 * Specific test's resources directory location, used as parameter to PdpTest(String). Check for a XML version of the file.
				 */
				final String xmlFilename = jsonFile.getName().substring(0, jsonFile.getName().length() - 5) + ".xml";
				final File srcXmlFile = invalidSrcXmlFilesDir == null ? null : new File(invalidSrcXmlFilesDir, xmlFilename);
				/*
				 * Output XML file from XML-to-JSON-to-XML back XSL transformation if any, next to the JSON file
				 */
				final File outXmlFromJsonXslt = srcXmlFile == null ? null : new File(validXacmlJsonFilesDir, xmlFilename);
				testParams.add(new Object[] { jsonFile, false, srcXmlFile != null && srcXmlFile.exists() ? srcXmlFile : null,
				        outXmlFromJsonXslt != null && outXmlFromJsonXslt.exists() ? outXmlFromJsonXslt : null });
			}
		}

		return testParams.iterator();
	}
}
