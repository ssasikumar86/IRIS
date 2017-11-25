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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.temenos.useragent.generic.Link;

public class AtomEntryHandlerTest {

	private AtomEntryHandler handler = new AtomEntryHandler();

	@Before
	public void setUp() {
		handler.setContent(AtomEntryHandler.class
				.getResourceAsStream("/atom_entry_with_xml_content.txt"));
	}

	@Test
	public void testGetLinks() {
		List<Link> links = handler.getLinks();
		assertEquals(4, links.size());

		// first 'self' link
		Link firstLink = links.get(0);
		assertEquals("self", firstLink.rel());
		assertEquals("Customers('100974')", firstLink.href());
		assertEquals("Customer", firstLink.title());
		assertEquals("", firstLink.description());
		assertFalse(firstLink.hasEmbeddedPayload());

		// second 'see' link
		Link secondLink = links.get(1);
		assertEquals("http://mybank/rels/see", secondLink.rel());
		assertEquals("Customers('100974')/see", secondLink.href());
		assertEquals("see record", secondLink.title());
		assertEquals(
				"http://schemas.microsoft.com/ado/2007/08/dataservices/related/Customer",
				secondLink.description());
		assertFalse(secondLink.hasEmbeddedPayload());

		// third 'see' link
		Link thirdLink = links.get(2);
		assertEquals("http://mybank/rels/input", thirdLink.rel());
		assertEquals("Customers('100974')/input", thirdLink.href());
		assertEquals("input deal", thirdLink.title());
		assertEquals("", thirdLink.description());
		assertFalse(thirdLink.hasEmbeddedPayload());

		// fourth 'see' link
		Link fourthLink = links.get(3);
		assertEquals("http://mybank/rels/review", fourthLink.rel());
		assertEquals("Customers('100974')/review", fourthLink.href());
		assertEquals("audit deal", fourthLink.title());
		assertEquals("", fourthLink.description());
		assertFalse(fourthLink.hasEmbeddedPayload());
	}

	@Test
	public void testGetId() {
		assertEquals("100974", handler.getId());
	}

	@Test
	public void testGetCountForValidProperties() {
		assertEquals(1, handler.getCount("GivenNames"));
		assertEquals(2, handler.getCount("Customer_LegalIdGroup"));
		assertEquals(1,
				handler.getCount("Customer_LegalIdGroup(0)/LegalDocName"));
		assertEquals(1,
				handler.getCount("Customer_LegalIdGroup(1)/LegalDocName"));
		assertEquals(2, handler.getCount("Customer_AddressGroup"));
		assertEquals(
				2,
				handler.getCount("Customer_AddressGroup(0)/Customer_AddressSubGroup"));
		assertEquals(
				1,
				handler.getCount("Customer_AddressGroup(0)/Customer_AddressSubGroup(0)/Address"));
		assertEquals(
				1,
				handler.getCount("Customer_AddressGroup(0)/Customer_AddressSubGroup(1)/Address"));
		assertEquals(
				2,
				handler.getCount("Customer_AddressGroup(1)/Customer_AddressSubGroup"));
		assertEquals(
				1,
				handler.getCount("Customer_AddressGroup(1)/Customer_AddressSubGroup(0)/Address"));
		assertEquals(
				1,
				handler.getCount("Customer_AddressGroup(1)/Customer_AddressSubGroup(1)/Address"));
	}

	@Test
	public void testGetCountForInvalidProperties() {
		assertEquals(0, handler.getCount("foo"));
		assertEquals(0,
				handler.getCount("Customer_LegalIdGroup(2)/LegalDocName"));
		assertEquals(
				0,
				handler.getCount("Customer_AddressGroup(0)/Customer_AddressSubGroup(10)/Address"));
		checkForCountWithException(null);
		checkForCountWithException("");
		checkForCountWithException("Customer_LegalIdGroup(-1)/LegalId");
		checkForCountWithException("Customer_LegalIdGroup[1]/LegalDocName");
		checkForCountWithException("Customer_AddressGroup(foo)");
		checkForCountWithException("Customer_AddressGroup(-1)");
		checkForCountWithException("Customer_AddressGroup/Customer_AddressSubGroup(-1)");
		checkForCountWithException("Customer_AddressGroup(0)/Customer_AddressSubGroup(-1)/Address");
		checkForCountWithException("Customer_AddressGroup(1)/Customer_AddressSubGroup()");
		checkForCountWithException("Customer_AddressGroup(1)/Customer_AddressSubGroup{1}/Address");
		checkForCountWithException("Customer_AddressGroup(1)/Customer_AddressSubGroup{0}/Address");
	}

	private void checkForCountWithException(String fqPropertyName) {
		try {
			handler.getCount(fqPropertyName);
			fail("IllegalArgumentException should have been thrown for property name '"
					+ fqPropertyName + "'");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testGetValueForValidProperties() {
		assertEquals("Peter", handler.getValue("GivenNames"));
		assertEquals("PASSPORT",
				handler.getValue("Customer_LegalIdGroup(0)/LegalDocName"));
		assertEquals("DRIVING LICENSE",
				handler.getValue("Customer_LegalIdGroup(1)/LegalDocName"));
		assertEquals(
				"18",
				handler.getValue("Customer_AddressGroup(0)/Customer_AddressSubGroup(0)/Address"));
		assertEquals(
				"Redds",
				handler.getValue("Customer_AddressGroup(0)/Customer_AddressSubGroup(1)/Address"));
		assertEquals(
				"18",
				handler.getValue("Customer_AddressGroup(1)/Customer_AddressSubGroup(0)/Address"));
		assertEquals(
				"Redes",
				handler.getValue("Customer_AddressGroup(1)/Customer_AddressSubGroup(1)/Address"));
	}

	@Test
	public void testGetValueForInvalidProperties() {
		assertEquals(null, handler.getValue("foo"));
		assertEquals(null,
				handler.getValue("Customer_LegalIdGroup(2)/LegalDocName"));
		assertEquals(
				null,
				handler.getValue("Customer_AddressGroup(0)/Customer_AddressSubGroup(10)/Address"));
		checkForValueWithException(null);
		checkForValueWithException("");
		checkForValueWithException("Customer_LegalIdGroup(-1)/LegalId");
		checkForValueWithException("Customer_LegalIdGroup[1]/LegalDocName");
		checkForValueWithException("Customer_AddressGroup(foo)");
		checkForValueWithException("Customer_AddressGroup(-1)");
		checkForValueWithException("Customer_AddressGroup/Customer_AddressSubGroup(-1)");
		checkForValueWithException("Customer_AddressGroup(0)/Customer_AddressSubGroup(-1)/Address");
		checkForValueWithException("Customer_AddressGroup(1)/Customer_AddressSubGroup()");
		checkForValueWithException("Customer_AddressGroup(1)/Customer_AddressSubGroup{1}/Address");
		checkForValueWithException("Customer_AddressGroup(1)/Customer_AddressSubGroup{0}/Address");
	}
	
    @Test
    public void testSetPrimitiveValueForProperty() throws IOException {
        handler.setValue("AccountOfficer", 2003L);
        assertEquals("2003", handler.getValue("AccountOfficer"));
        handler.setValue("CoCode", 'D');
        assertEquals("D", handler.getValue("CoCode"));
        handler.setValue("AllowBulkProcess", true);
        assertEquals("true", handler.getValue("AllowBulkProcess"));
        handler.setValue("Foo", "Bar");
        assertEquals("Bar", handler.getValue("Foo"));
        handler.setValue("Balance", 12345789.87654321);
        assertEquals("1.234578987654321E7", handler.getValue("Balance"));
        handler.setValue("Credit", -10203.50f);
        assertEquals("-10203.5", handler.getValue("Credit"));
        handler.setValue("Baz", null);
        assertEquals("", handler.getValue("Baz"));
        
        String content = IOUtils.toString(handler.getContent());
        assertThat(content, containsString("<d:AccountOfficer>2003</d:AccountOfficer>"));
        assertThat(content, containsString("<d:CoCode>D</d:CoCode>"));
        assertThat(content, containsString("<d:AllowBulkProcess>true</d:AllowBulkProcess>"));
        assertThat(content, containsString("<d:Balance>1.234578987654321E7</d:Balance>"));
        assertThat(content, containsString("<d:Credit>-10203.5</d:Credit>"));
        assertThat(content, containsString("<d:Baz />"));
    }
	
	

	private void checkForValueWithException(String fqPropertyName) {
		try {
			handler.getValue(fqPropertyName);
			fail("IllegalArgumentException should have been thrown for property name '"
					+ fqPropertyName + "'");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}
}
