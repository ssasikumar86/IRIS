package com.temenos.useragent.generic.mediatype;

/*
 * #%L
 * useragent-generic-java
 * %%
 * Copyright (C) 2012 - 2016 Temenos Holdings N.V.
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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.abdera.model.Content;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class AtomUtil {

	public static final String MEDIA_TYPE = "application/atom+xml";
	public final static String NS_ATOM = "http://www.w3.org/2005/Atom";
	public final static String NS_ODATA = "http://schemas.microsoft.com/ado/2007/08/dataservices";
	public final static String NS_ODATA_METADATA = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";

	public static String extractRel(String relAttributeValue) {
		int spaceIndex = relAttributeValue.trim().indexOf(" ");
		if (spaceIndex > 0) {
			return relAttributeValue.substring(spaceIndex).trim();
		} else {
			return relAttributeValue.trim();
		}
	}

	public static String extractDescription(String relAttributeValue) {
		int spaceIndex = relAttributeValue.trim().indexOf(" ");
		if (spaceIndex > 0) {
			return relAttributeValue.substring(0, spaceIndex + 1).trim();
		} else {
			return "";
		}
	}

	public static String getBaseUrl(Element element) {
		try {
			return element.getBaseUri().toURL().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Entry updateEntryContent(Document document, Entry entry) {
		if (document == null) {
			// no content to update the entry
			return entry;
		}
		DOMSource domSource = new DOMSource(document);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to update Atom Entry content from XML", e);
		}
		entry.setContent(writer.toString(), Content.Type.XML);
		return entry;
	}

	public static Document buildXmlDocument(String xmlContent) {
		if (xmlContent == null || xmlContent.isEmpty()) {
			return null;
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlContent));
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			return docBuilder.parse(is);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to build Atom Entry content into XML. Entry content '"
							+ xmlContent + "'", e);
		}
	}
}
