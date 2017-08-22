package com.temenos.interaction.core.hypermedia;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.MultivaluedMapImpl;

public class HypermediaTemplateHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(HypermediaTemplateHelper.class);

	// regex for uri template pattern
	static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(.*?)\\}");

	/**
	 * Provide path parameters for a transition's target state.
	 * @param transition transition
	 * @param transitionProperties transition properties 
	 * @return path parameters
	 */
	public static MultivaluedMap<String, String> getPathParametersForTargetState(Transition transition, Map<String, Object> transitionProperties) {
		//Parse source and target parameters from the transition's 'path' and 'originalPath' attributes respectively
		MultivaluedMap<String, String> pathParameters = new MultivaluedMapImpl<String>();
		String resourcePath = transition.getTarget().getPath();
		String[] sourceParameters = getPathTemplateParameters(resourcePath);
		String[] targetParameters = getPathTemplateParameters(resourcePath);

		//Apply transition properties to parameters
		for(int i=0; i < sourceParameters.length; i++) {
			Object paramValue = transitionProperties.get(sourceParameters[i]);
			if(paramValue != null) {
				pathParameters.putSingle(targetParameters[i], paramValue.toString());
			}
		}
		return pathParameters;
	}

	/*
	 * Returns the list of parameters contained inside
	 * a URI template. 
	 */
	public static String[] getPathTemplateParameters(String pathTemplate) {
		List<String> params = new ArrayList<String>();
		Matcher m = TEMPLATE_PATTERN.matcher(pathTemplate);
		while(m.find()) {
			params.add(m.group(1));
		}
		return params.toArray(new String[0]);
	}

	/**
	 * Similar to UriBuilder, but used for simple template token replacement where
	 * supplied template is not a uri, and not all tokens need to be replaced.
	 * @param template
	 * @param properties
	 * @return
	 */
	public static String templateReplace(String template, Map<String, Object> properties) {
		String result = template;
		Map<String, Object> normalizedProperties = null;
		try {
			if (template != null) {
				Matcher m = TEMPLATE_PATTERN.matcher(template);
				while(m.find()) {
					String param = m.group(1);
					if(null == normalizedProperties) {
						normalizedProperties = HypermediaTemplateHelper.normalizeProperties(properties);
					}
					if (normalizedProperties.containsKey(param)) {
						// replace template tokens
					    String value = Matcher.quoteReplacement(normalizedProperties.get(param).toString());
					    result = resolveWildCardMatches(result, value);
                        /*
                         * Avoid duplicate quotes, if both expression and value
                         * contains single quotes. For example: Expression:
                         * filter=FileVer eq '{Fv}' Value:Fv='SIM' Result should
                         * be filter=FileVer eq 'SIM' instead filter=FileVer eq
                         * ''SIM''
                         */
                        if (value.startsWith("'") && value.endsWith("'")) {
                            result = result.replaceAll(Pattern.quote("'{" + param + "}'"), value);
                        }
					    result = result.replaceAll(Pattern.quote("{"+param+"}"), value);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("An error occurred while replacing tokens in ["+template+"]", e);
		}
		return result;
	}

	/**
     * This is used to replace wild card variable values, while doing template
     * replace for value "id eq {ArrOd}". If variable value contains "...",
     * based on its position equals operator gets converted into particular
     * OData operator.
     *
     * @param result
     * @param value
     */
    private static String resolveWildCardMatches(String result, String value) {
        String[] splitResult = result.split(" ");
        if (splitResult.length == 3 && ("eq".equalsIgnoreCase(splitResult[1]) || "ne".equalsIgnoreCase(splitResult[1]))
                && value.contains("...")) {
            if("eq".equalsIgnoreCase(splitResult[1])) {
                result = "substringof('" + splitResult[2] + "', " + splitResult[0] + ") eq true";
            } else {
                result = "substringof('" + splitResult[2] + "', " + splitResult[0] + ") eq false";
            }
        }
        return result;
    }

	/**
	 * Given a base uri template and a uri, return the base uri portion.
	 * @param baseUriTemplate
	 * @param baseUri
	 * @return
	 */
	public static String getTemplatedBaseUri(String baseUriTemplate, String uri) {
		// (\Qhttp://localhost:8080/responder/rest/\E)((?<companyid>[^\/]+))(\Q/\E)((?<href>[^\/]+))
		// form a regex for the base uri including any context parameters
		StringBuffer templateRegex = new StringBuffer();
		Matcher m = TEMPLATE_PATTERN.matcher(baseUriTemplate);
		if (m.find() && m.groupCount() > 0) {
			templateRegex
					// match anything before the template
					.append(".?")
					// match on the provided template
					.append(Pattern.quote(baseUriTemplate.substring(0, m.start())));
			templateRegex.append(getUriTemplatePattern(m.group(1)));
			while(m.find()) {
				templateRegex.append(getUriTemplatePattern(m.group(1)));
			}
		} else {
			templateRegex
					// match anything before the template
					.append(".?")
					// match on the provided template
					.append(Pattern.quote(baseUriTemplate));
		}
		String groups[] = getPathTemplateParameters(baseUriTemplate);
		Map<String, Object> values = match(groups, templateRegex.toString(), uri);
		String result = null;
		if (values != null) {
			UriBuilder builder = UriBuilder.fromPath(baseUriTemplate);
			result = builder.buildFromMap(values).toASCIIString();
		} else {
			result = baseUriTemplate;
		}
		if (!result.endsWith("/")) {
			result += "/";
		}
		return result;
	}

	private static Map<String, Object> match(String groups[], String template, String uri) {
		Pattern p = Pattern.compile(template);
		Matcher m = p.matcher(uri);
		if (m.find()) {
			Map<String, Object> params = new HashMap<String, Object>(m.groupCount());
			for (int g = 0; g < m.groupCount() && g < groups.length; g++) {
				params.put(groups[g], m.group(1));
			}
			return params;
		}
		return null;
	}

	private static String getUriTemplatePattern(String param) {
		// works with Java 7
		return "([^\\/]*)";
	}

	/**
	 * <p>Returns a map having the original properties plus new entries created from properties in OCollections.   
	 *
	 * <p>Example: For a map having value [OCollection (A) -> OComplexType (B) -> OProperty(C) -> value D]
	 * this method will generate a map entry with key A.B.C and value D 
	 * @param properties
	 * @return
	 */
	public static LinkedHashMap<String, Object> normalizeProperties(Map<String, Object> properties) {
		LinkedHashMap<String, Object> propertiesNormalized = new LinkedHashMap <String, Object>();
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			if (entry.getValue() instanceof OCollection) {
				OCollection<?> collection = (OCollection<?>) entry.getValue();
				String collectionName = extractSimplePropertyName(entry.getKey());
				buildComplexPropertyEntry(propertiesNormalized, collection, collectionName);
			} else {
				addUniqueMapEntry(propertiesNormalized, entry.getKey(), entry.getValue());
			}
		}
		return propertiesNormalized;
	}

	/**
	 * Extracts single name of a property out of a fully qualified property name.
	 * A fully qualified property name is made of an optional prefix followed by a mandatory
	 * single property name. They are separated by "_" if prefix is present.
	 *
	 * @param fullyQualifiedName	the fully qualified name of property
	 *
	 * @return 						the single name of property
	 *
	 */
	public static String extractSimplePropertyName(String fullyQualifiedName) {
		if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
			return null;
		}
		if (!fullyQualifiedName.contains("_")) {
			return fullyQualifiedName;
		}
		return fullyQualifiedName.substring(fullyQualifiedName.indexOf("_") + 1, fullyQualifiedName.length());
	}

	private static void buildComplexPropertyEntry(LinkedHashMap<String, Object> propertiesNormalized, OCollection<?> collection, String collectionName) {
		int elementIdx = 0;
		for (OObject each : collection) {
			if (each instanceof OComplexObject) {
				OComplexObject ooComplex = (OComplexObject) each;
				for (OProperty<?> property : ooComplex.getProperties()) {
					StringBuilder complexMvPropertyName = new StringBuilder();
					complexMvPropertyName.append(collectionName).append("(").append(elementIdx).append(")").append(".");
					complexMvPropertyName.append(extractSimplePropertyName(property.getName()));

					if (property.getValue() instanceof OCollection) {
						buildComplexPropertyEntry(propertiesNormalized, (OCollection<?>) property.getValue(), complexMvPropertyName.toString());
					} else {
						addUniqueMapEntry(propertiesNormalized, complexMvPropertyName.toString(), property.getValue());
					}
				}
			} else { //Primitive or Enum
				StringBuilder complexMvPropertyName = new StringBuilder();
				complexMvPropertyName.append(collectionName).append("(").append(elementIdx).append(")");
				addUniqueMapEntry(propertiesNormalized, complexMvPropertyName.toString(), each);
			}
			elementIdx++;
		}
	}

	private static void addUniqueMapEntry(LinkedHashMap<String, Object> propertiesNormalized, String key, Object object) {
		if (!propertiesNormalized.containsKey(key)) {
			propertiesNormalized.put(key, object);
		}
	}
}
