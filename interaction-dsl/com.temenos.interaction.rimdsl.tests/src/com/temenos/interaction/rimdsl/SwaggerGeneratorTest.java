package com.temenos.interaction.rimdsl;

/*
 * #%L
 * com.temenos.interaction.rimdsl.tests
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.temenos.interaction.rimdsl.rim.DomainModel;
import com.temenos.interaction.rimdsl.rim.ResourceInteractionModel;

@InjectWith(RIMDslSwaggerInjectorProvider.class)
@RunWith(XtextRunner.class)
public class SwaggerGeneratorTest {
	
	@Inject 
	IGenerator underTest;
	@Inject
	ParseHelper<DomainModel> parseHelper;
	
	private final static String LINE_SEP = System.getProperty("line.separator");
	
	private final static String SIMPLE_STATES_RIM = "" +
	"rim Simple {" + LINE_SEP +
	"	event POST {" + LINE_SEP +
	"	    method: POST" + LINE_SEP +
	"	}" + LINE_SEP +

	"	command GetEntity" + LINE_SEP +
	"	command GetException" + LINE_SEP +
	"	command UpdateEntity" + LINE_SEP +
			
	"initial resource A {" + LINE_SEP +
	"	type: collection" + LINE_SEP +
	"	entity: ENTITY" + LINE_SEP +
	"	view: GetEntity" + LINE_SEP +
	"	path: \"/A\"" + LINE_SEP +
	"	POST -> B" + LINE_SEP +
	"   description: \"/A description\"" + LINE_SEP +
	"}" + LINE_SEP +

	"exception resource E {" + LINE_SEP +
	"	type: collection" + LINE_SEP +
	"	entity: EXCEPTION" + LINE_SEP +
	"	view: GetException" + LINE_SEP +
	"}" + LINE_SEP +
	
	"resource B {" +
	"	type: item" + LINE_SEP +
	"	entity: ENTITY" + LINE_SEP +
	"	actions [ UpdateEntity ]" + LINE_SEP +
	"	path: \"/B\"" + LINE_SEP +
	"   description: \"/B description\"" + LINE_SEP +
	"}" + LINE_SEP +
	"}" + LINE_SEP +
	"";

	private final static String SIMPLE_STATES_SWAGGER = "" +		
	"{" + LINE_SEP +
	"  \"apiVersion\": \"0.2\"," + LINE_SEP +
	"  \"swaggerVersion\": \"1.2\"," + LINE_SEP +
	"\"resourcePath\": \"/A\"," + LINE_SEP +
	"\"apis\": [" + LINE_SEP +
	"{" + LINE_SEP +
	"\"path\": \"/A\"," + LINE_SEP +
	"\"operations\": [" + LINE_SEP +
	"{" + LINE_SEP +
	"\"method\": \"GET\"," + LINE_SEP +
	"\"nickname\": \"A\"," + LINE_SEP +
	"\"summary\": \"/A description\" " + LINE_SEP +
	"}" + LINE_SEP +
	"]" + LINE_SEP +
	"}," + LINE_SEP +
	"{" + LINE_SEP +
	"\"path\": \"/B\"," + LINE_SEP +
	"\"operations\": [" + LINE_SEP +
	"{" + LINE_SEP +
	"\"method\": \"POST\"," + LINE_SEP +
	"\"nickname\": \"B\"," + LINE_SEP +
	"\"summary\": \"/B description\" " + LINE_SEP +
	"}," + LINE_SEP +
	"{" + LINE_SEP +
	"\"method\": \"GET\"," + LINE_SEP +
	"\"nickname\": \"B\"," + LINE_SEP +
	"\"summary\": \"/B description\" " + LINE_SEP +
	"}" + LINE_SEP +
	"]" + LINE_SEP +
	"}" + LINE_SEP +
	"]" + LINE_SEP +
	"}" + LINE_SEP;
	
	@Test
	public void testGenerateSimpleStates() throws Exception {
		DomainModel domainModel = parseHelper.parse(SIMPLE_STATES_RIM);
		ResourceInteractionModel model = (ResourceInteractionModel) domainModel.getRims().get(0);
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		underTest.doGenerate(model.eResource(), fsa);
		assertEquals(1, fsa.getFiles().size());
		
		// the behaviour class
		String expectedKey = IFileSystemAccess.DEFAULT_OUTPUT + "api-docs.json";
		assertTrue(fsa.getFiles().containsKey(expectedKey));
		assertEquals(SIMPLE_STATES_SWAGGER, fsa.getFiles().get(expectedKey).toString());
				
	}

	@Test
	public void testGenerateSimple() throws Exception {
		DomainModel domainModel = parseHelper.parse(loadTestRIM());
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		underTest.doGenerate(domainModel.eResource(), fsa);
		assertEquals(1, fsa.getFiles().size());
		
		// the behaviour class
		String expectedKey = IFileSystemAccess.DEFAULT_OUTPUT + "api-docs.json";
		assertTrue(fsa.getFiles().containsKey(expectedKey));
		String output = fsa.getFiles().get(expectedKey).toString();
		assertTrue(output.contains("/notes"));
				
	}
	
	private String loadTestRIM() throws IOException {
		URL url = Resources.getResource("Simple.rim");
		String rim = Resources.toString(url, Charsets.UTF_8);
		return rim;
	}

}
