
package com.temenos.interaction.core.entity;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
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


import java.io.InputStream;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.temenos.interaction.core.entity.vocabulary.TermFactory;
import com.temenos.interaction.core.entity.vocabulary.Vocabulary;
import com.temenos.interaction.core.entity.vocabulary.terms.TermComplexGroup;
import com.temenos.interaction.core.entity.vocabulary.terms.TermComplexType;

/**
 * Parser to read metadata from an XML file.
 */
public class MetadataParser extends DefaultHandler {
	private static final Logger logger = LoggerFactory.getLogger(MetadataParser.class);
	TermFactory termFactory;

	Metadata metadata = null;
	EntityMetadata entityMetadata = null;									//Meta data for current entity
	Stack<String> propertyName = new Stack<String>();						//Stack of property names
	Stack<Vocabulary> propertyVocabulary = new Stack<Vocabulary>();			//Stack of property vocabularies
	boolean isComplexProperty = false;										//Indicates if this is a complex property
	String termName = null;													//Name of vocabulary term
	String termValue = null;												//Value of vocabulary term

	public MetadataParser() {
		termFactory = new TermFactory();
	}

	public MetadataParser(TermFactory termFactory) {
		this.termFactory = termFactory;
	}
	
	/**
	 * Parse an XML document.
	 * @param reader I/O Reader providing the xml document
	 * @return Metadata containing the metadata or null if error
	 */
	public Metadata parse(InputStream is) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(is, this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		logger.debug("parsed, element count = " + entityMetadata.getPropertyVocabularyKeySet().size() );
		return metadata;
	}
	
	@Override
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("Metadata")) {
			metadata = new Metadata(attributes.getValue("ModelName"));
		}
		else if (qName.equalsIgnoreCase("Entity")) {
			entityMetadata = new EntityMetadata(attributes.getValue("Name"));
		}
		else if (qName.equalsIgnoreCase("Property")) {
			String name = attributes.getValue("Name");
			Vocabulary voc = new Vocabulary();
			if(propertyName.size() > 0) {
				//This property belongs to a complex property => set complex group term
				try {
					voc.setTerm(termFactory.createTerm(TermComplexGroup.TERM_NAME, propertyName.peek()));
				}
				catch(Exception e) {
					throw new SAXException(e);
				}
			}
			propertyName.push(name);			
			propertyVocabulary.push(voc);
			isComplexProperty = false;
		}
		else if (qName.equalsIgnoreCase("Term")) {
			termName = attributes.getValue("Name");
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("Entity")) {
			metadata.setEntityMetadata(entityMetadata);
		}
		else if (qName.equalsIgnoreCase("Property")) {
			String name = propertyName.pop();
			Vocabulary voc = propertyVocabulary.pop();
			if(isComplexProperty) {
				//This is a complex property
				try {
					voc.setTerm(termFactory.createTerm(TermComplexType.TERM_NAME, "true"));
				}
				catch(Exception e) {
					throw new SAXException(e);
				}
			}
			if ( name == null ) {
				throw new SAXException("Parse error: Property without name in Entity " + entityMetadata.getEntityName());
			}
			entityMetadata.setPropertyVocabulary(name, voc, propertyName.elements());
			isComplexProperty = (propertyName.size() > 0);
		}
		else if (qName.equalsIgnoreCase("Term")) {
			try {
				if(propertyName.size() > 0) {
					propertyVocabulary.peek().setTerm(termFactory.createTerm(termName, termValue));
				}
				else {
					Vocabulary voc = new Vocabulary();
					voc.setTerm(termFactory.createTerm(termName, termValue));
					entityMetadata.setVocabulary(voc);
				}
				termValue = null;
			}
			catch(Exception e) {
				throw new SAXException(e);
			}
			finally {
				termName = null;	//characters() uses termName to avoid reading elements other than Terms			
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (termName != null) {
			if(termValue != null) {		//this method may be invoked multiple times if data is read in chunks
				termValue += new String(ch, start, length);
			}
			else {
				termValue = new String(ch, start, length);
			}
		}

	}
}
