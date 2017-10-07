package com.temenos.useragent.generic.mediatype;

/*
 * #%L
 * useragent-generic-java
 * %%
 * Copyright (C) 2012 - 2017 Temenos Holdings N.V.
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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import com.temenos.useragent.generic.Link;

public class HalJsonLinkHandlerTest {
	
	private HalJsonEntityHandler entityHandler;
	
	@Test
	public void testGetLinks() {
		initEntityHandler("/haljson_item_with_all_properties.json");
		List<Link> links = entityHandler.getLinks();
		assertEquals(4, links.size());

		Link selfLink = links.get(0);
		assertEquals("self", selfLink.rel());
		assertEquals(
				"http://mybank/restservice/BankService/Customers('66052')",
				selfLink.href());
		assertFalse(selfLink.hasEmbeddedPayload());
		assertEquals("", selfLink.baseUrl());
		assertEquals("", selfLink.title());
		assertNull(selfLink.embedded());

		Link inputLink = links.get(1);
		assertEquals("http://temenostech.temenos.com/rels/input",
				inputLink.rel());
		assertEquals(
				"http://mybank/restservice/BankService/Customers('66052')",
				inputLink.href());
		assertFalse(inputLink.hasEmbeddedPayload());
		assertEquals("", inputLink.baseUrl());
		assertEquals("input deal", inputLink.title());
		assertNull(inputLink.embedded());
	}
	
	@Test
	public void testGetLinksWithEmbeddedEntity() {
		initEntityHandler("/haljson_item_with_inline_item.json");
		List<Link> links = entityHandler.getLinks();
		assertEquals(3, links.size());
	}
	
	private void initEntityHandler(String jsonFileName) {
		entityHandler = new HalJsonEntityHandler();
		entityHandler.setContent(HalJsonEntityHandlerTest.class
				.getResourceAsStream(jsonFileName));
	}
}
