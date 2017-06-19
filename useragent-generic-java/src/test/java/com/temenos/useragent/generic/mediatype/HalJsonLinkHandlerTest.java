package com.temenos.useragent.generic.mediatype;

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
