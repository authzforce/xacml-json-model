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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.Reader;

/**
 * {@link JSONObject} that complies with limits in terms of value length, depth and number of elements.
 */
public final class LimitsCheckingJSONObject extends JSONObject
{
	/**
	 * Hardened {@link JSONTokener} that checks limits on JSON structures, such as arrays and strings, in order to mitigate content-level attacks. Downside: it is slower at parsing than
	 * {@link JSONTokener}.
	 *
	 */
	private static final class LimitsCheckingJSONTokener extends JSONTokener
	{
		private final int maxJsonStringLength;
		private final int maxNumOfKeysOrArrayItems;
		private final int maxDepth;

		private transient boolean nextStringInProgress = false;

		/*
		 * Position of last read character from a JSON string (being parsed)
		 */
		private transient int lastReadCharPosFromNextString = -1;
		private transient int currentValueDepth = 0;

		/**
		 * Constructs from a {@link Reader}
		 * 
		 * @param reader
		 *            a reader
		 * 
		 * @param maxJsonStringLength
		 *            allowed maximum size of JSON keys and string values
		 * @param maxNumOfImmediateChildren
		 *            allowed maximum number of keys (therefore key-value pairs) in JSON object, or items in JSON array
		 * @param maxDepth
		 *            allowed maximum depth of JSON object
		 */
		private LimitsCheckingJSONTokener(final Reader reader, final int maxJsonStringLength, final int maxNumOfImmediateChildren, final int maxDepth)
		{
			super(reader);
			this.maxJsonStringLength = maxJsonStringLength;
			this.maxNumOfKeysOrArrayItems = maxNumOfImmediateChildren;
			this.maxDepth = maxDepth;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.json.JSONTokener#next()
		 */
		@Override
		public char next() throws JSONException
		{
			final char next = super.next();
			if (nextStringInProgress && next != 0)
			{
				/*
				 * nextStringInProgress = true => $next is before or is indeed the terminating quote character passed as arg to nextString(quote) below
				 */
				/*
				 * lastReadCharPosFromNextString is the position of $next in the string being parsed (starts from zero), or the terminating quote character
				 */
				lastReadCharPosFromNextString += 1;
				/*
				 * The position (starting from zero) of the last character in the string must be <= $maxJsonStringLength - 1 and therefore the position of terminating quote character
				 * ($lastReadCharPosFromNextString) must be <= maxJsonStringLength
				 */
				if (lastReadCharPosFromNextString > maxJsonStringLength)
				{
					throw new IllegalArgumentException("JSON string (key/value) too long: > " + maxJsonStringLength);
				}
			}

			return next;
		}

		@Override
		public String nextString(final char quote) throws JSONException
		{
			nextStringInProgress = true;
			final String nextString = super.nextString(quote);
			nextStringInProgress = false;
			// reset for next call
			lastReadCharPosFromNextString = -1;
			return nextString;
		}

		/**
		 * Get the next value. The value can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
		 * 
		 * @throws JSONException
		 *             If syntax error.
		 *
		 * @return An object.
		 */
		@Override
		public Object nextValue() throws JSONException
		{
			char c = this.nextClean();
			String string;

			switch (c)
			{
				case '"':
				case '\'':
					return this.nextString(c);
				case '{':
					this.back();
					currentValueDepth++;
					if (currentValueDepth > maxDepth)
					{
						throw new IllegalArgumentException("Depth of JSONObject too high: > " + maxDepth);
					}
					final Object jsonObj = new LimitsCheckingJSONObject(this);
					currentValueDepth--;
					return jsonObj;
				case '[':
					this.back();
					currentValueDepth++;
					if (currentValueDepth > maxDepth)
					{
						throw new IllegalArgumentException("Depth of JSONArray too high: > " + maxDepth);
					}
					final Object jsonArray = new LimitsCheckingJSONArray(this);
					currentValueDepth--;
					return jsonArray;

				default:
					/*
					 * Handle unquoted text. This could be the values true, false, or null, or it can be a number. An implementation (such as this one) is allowed to also accept non-standard forms.
					 * 
					 * Accumulate characters until we reach the end of the text or a formatting character.
					 */

					final StringBuilder sb = new StringBuilder();
					while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0)
					{
						sb.append(c);
						c = this.next();
					}
					this.back();

					string = sb.toString().trim();
					if (string.isEmpty())
					{
						throw this.syntaxError("Missing value");
					}
					return JSONObject.stringToValue(string);
			}

		}
	}

	private static final class LimitsCheckingJSONArray extends JSONArray
	{
		private LimitsCheckingJSONArray(final LimitsCheckingJSONTokener x)
		{
			super();
			if (x.nextClean() != '[')
			{
				throw x.syntaxError("A JSONArray text must start with '['");
			}
			if (x.nextClean() != ']')
			{
				x.back();
				int numOfItems = 0;
				for (;;)
				{
					/*
					 * Add new item
					 */
					numOfItems++;
					if (numOfItems > x.maxNumOfKeysOrArrayItems)
					{
						throw new IllegalArgumentException("Number of items in JSONArray is too big: > " + x.maxNumOfKeysOrArrayItems);
					}

					if (x.nextClean() == ',')
					{
						x.back();
						put(JSONObject.NULL);
					}
					else
					{
						x.back();
						put(x.nextValue());
					}
					switch (x.nextClean())
					{
						case ',':
							if (x.nextClean() == ']')
							{
								return;
							}
							x.back();
							break;
						case ']':
							return;
						default:
							throw x.syntaxError("Expected a ',' or ']'");
					}
				}
			}
		}
	}

	private LimitsCheckingJSONObject(final LimitsCheckingJSONTokener x)
	{
		super();

		char c;
		String key;

		if (x.nextClean() != '{')
		{
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}

		int numOfKeys = 0;
		for (;;)
		{
			c = x.nextClean();
			switch (c)
			{
				case 0:
					throw x.syntaxError("A JSONObject text must end with '}'");
				case '}':
					return;
				default:
					x.back();
					key = x.nextValue().toString();
			}

			// The key is followed by ':'.

			c = x.nextClean();
			if (c != ':')
			{
				throw x.syntaxError("Expected a ':' after a key");
			}

			numOfKeys++;
			if (numOfKeys > x.maxNumOfKeysOrArrayItems)
			{
				throw new IllegalArgumentException("Number of keys in JSONObject is too big: > " + x.maxNumOfKeysOrArrayItems);
			}
			this.putOnce(key, x.nextValue());

			// Pairs are separated by ','.

			switch (x.nextClean())
			{
				case ';':
				case ',':
					if (x.nextClean() == '}')
					{
						return;
					}
					x.back();
					break;
				case '}':
					return;
				default:
					throw x.syntaxError("Expected a ',' or '}'");
			}
		}
	}

	/**
	 * Constructs from an {@link InputStream}
	 * 
	 * @param reader
	 *            the source
	 * 
	 * @param maxJsonStringLength
	 *            allowed maximum length of JSON keys and string values
	 * @param maxNumOfImmediateChildren
	 *            allowed maximum number of keys (therefore key-value pairs) in JSON object, or items in JSON array
	 * @param maxDepth
	 *            allowed maximum depth of JSON object
	 */
	public LimitsCheckingJSONObject(final Reader reader, final int maxJsonStringLength, final int maxNumOfImmediateChildren, final int maxDepth)
	{
		this(new LimitsCheckingJSONTokener(reader, maxJsonStringLength, maxNumOfImmediateChildren, maxDepth));
	}
}