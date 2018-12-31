/**
 * Copyright 2012-2018 THALES.
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

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.everit.json.schema.loader.SchemaClient;
import org.springframework.util.ResourceUtils;

/**
 * JSON schema resolver using Spring {@link ResourceUtils} to resolve schema locations (URIs)
 *
 */
public final class SpringBasedJsonSchemaClient implements SchemaClient
{
	private final Map<String, String> schemaCatalog;

	/**
	 * Creates schema resolver with catalog defining mappings from locations in processed JSON schemas to alternate locations (in the same manner as URI mappings in XML catalog) that this instance is
	 * supposed to have access to
	 * 
	 * @param schemaCatalog
	 *            JSON schema catalog (URI mappings)
	 */
	public SpringBasedJsonSchemaClient(final Map<String, String> schemaCatalog)
	{
		this.schemaCatalog = schemaCatalog == null ? Collections.emptyMap() : new HashMap<>(schemaCatalog);
	}

	/**
	 * Creates schema resolver with empty schema catalog
	 */
	public SpringBasedJsonSchemaClient()
	{
		this(null);
	}

	@Override
	public InputStream get(final String uri)
	{
		final String finalLocation = schemaCatalog.getOrDefault(uri, uri);
		try
		{
			return ResourceUtils.getURL(finalLocation).openStream();
		}
		catch (final Exception e)
		{
			throw new IllegalArgumentException("Can't resolve referenced schema location: " + uri, e);
		}
	}
}
