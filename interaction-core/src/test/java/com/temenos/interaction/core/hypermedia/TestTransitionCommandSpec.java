package com.temenos.interaction.core.hypermedia;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestTransitionCommandSpec {

	@Test
	public void testTransitionCommandSpec() {
		new TransitionCommandSpec("GET", 0);
	}

	@Test
	public void testForEach() {
		TransitionCommandSpec cs = new TransitionCommandSpec("GET", Transition.FOR_EACH);
		assertTrue(cs.isForEach());
	}

    @Test
    public void testForEachEmbedded() {
        TransitionCommandSpec cs = new TransitionCommandSpec("GET", Transition.FOR_EACH_EMBEDDED);
        assertTrue(cs.isEmbeddedForEach());
    }
    
    @Test
    public void testRedirect() {
        TransitionCommandSpec cs = new TransitionCommandSpec("GET", Transition.REDIRECT);
        assertTrue(cs.isRedirectTransition());
    }
    
	
	@Test
	public void testAutoForEach() {
		TransitionCommandSpec cs = new TransitionCommandSpec("GET", Transition.AUTO | Transition.FOR_EACH);
		assertTrue(cs.isAutoTransition());
		assertTrue(cs.isForEach());
	}

	@Test
	public void testAuto() {
		TransitionCommandSpec cs = new TransitionCommandSpec(null, Transition.AUTO);
		assertTrue(cs.isAutoTransition());
	}

	@Test
	public void testEquality() {
		TransitionCommandSpec tcs = new TransitionCommandSpec("GET", 0);
		TransitionCommandSpec tcs2 = new TransitionCommandSpec("GET", 0);
		assertEquals(tcs, tcs2);
		assertEquals(tcs.hashCode(), tcs2.hashCode());
	}

	@Test
	public void testEqualityWithParameters() {
		Map<String, String> params1 = new HashMap<String, String>();
		params1.put("param", "hello");
		TransitionCommandSpec tcs = new TransitionCommandSpec("GET", 0, null, params1, null);
		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("param", "hello");
		TransitionCommandSpec tcs2 = new TransitionCommandSpec("GET", 0, null, params2, null);
		assertEquals(tcs, tcs2);
		assertEquals(tcs.hashCode(), tcs2.hashCode());
	}
	
	@Test
	public void testInequality() {
		TransitionCommandSpec tcs = new TransitionCommandSpec("GET", 0);
		TransitionCommandSpec tcs2 = new TransitionCommandSpec("PUT", 0);
		TransitionCommandSpec tcs3 = new TransitionCommandSpec("GET", Transition.FOR_EACH);
		assertFalse(tcs.equals(tcs2));
		assertFalse(tcs.hashCode() == tcs2.hashCode());
		assertFalse(tcs.equals(tcs3));
		assertFalse(tcs.hashCode() == tcs3.hashCode());
	}

	@Test
	public void testInequalityWithUriLinkageParameters() {
		Map<String, String> params1 = new HashMap<String, String>();
		params1.put("param", "hello");
		TransitionCommandSpec tcs = new TransitionCommandSpec("GET", 0, null, params1, null);
		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("param", "HELLO");
		TransitionCommandSpec tcs2 = new TransitionCommandSpec("GET", 0, null, params2, null);
		assertFalse(tcs.equals(tcs2));
		assertFalse(tcs.hashCode() == tcs2.hashCode());
	}

	@Test
	public void testEqualityNull() {
		TransitionCommandSpec tcs = new TransitionCommandSpec(null, 0);
		TransitionCommandSpec tcs2 = new TransitionCommandSpec(null, 0);
		assertEquals(tcs, tcs2);
		assertEquals(tcs2, tcs);
		assertEquals(tcs.hashCode(), tcs2.hashCode());
	}

	@Test
	public void testInequalityNull() {
		TransitionCommandSpec tcs = new TransitionCommandSpec(null, 0);
		TransitionCommandSpec tcs2 = new TransitionCommandSpec("PUT", 0);
		TransitionCommandSpec tcs3 = new TransitionCommandSpec("GET", 0);
		assertFalse(tcs.equals(tcs2));
		assertFalse(tcs.hashCode() == tcs2.hashCode());
		assertFalse(tcs.equals(tcs3));
		assertFalse(tcs.hashCode() == tcs3.hashCode());
	}

	@Test
	public void testToString() {
		assertEquals("GET", new TransitionCommandSpec("GET", 0).toString());
		assertEquals("*GET", new TransitionCommandSpec("GET", Transition.FOR_EACH).toString());
		assertEquals("AUTO", new TransitionCommandSpec("GET", Transition.AUTO).toString());
	}

	@Test
	public void testLinkIdEquality() {
		Map<String, String> params1 = new HashMap<String, String>();
		params1.put("param", "hello");
		TransitionCommandSpec tcs = new TransitionCommandSpec("GET", 0, null, params1, "123456");
		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("param", "hello");
		TransitionCommandSpec tcs2 = new TransitionCommandSpec("GET", 0, null, params2, "123456");
		assertEquals(tcs, tcs2);
		assertEquals(tcs.hashCode(), tcs2.hashCode());	
	}
	
	@Test
	public void testLinkIdInequality() {
		Map<String, String> params1 = new HashMap<String, String>();
		params1.put("param", "hello");
		TransitionCommandSpec tcs = new TransitionCommandSpec("GET", 0, null, params1, "123456");
		Map<String, String> params2 = new HashMap<String, String>();
		params2.put("param", "hello");
		TransitionCommandSpec tcs2 = new TransitionCommandSpec("GET", 0, null, params2, "654321");
		assertFalse(tcs == tcs2);
		assertFalse(tcs.hashCode() == tcs2.hashCode());		
	}
	
	@Test
	public void testLinkIdToString() {
		assertEquals("null linkId=123456", new TransitionCommandSpec(null, 0,null,null,"123456").toString());
		assertEquals("GET linkId=123456", new TransitionCommandSpec("GET", 0,null,null,"123456").toString());
		assertEquals("*GET linkId=123456", new TransitionCommandSpec("GET", Transition.FOR_EACH, null, null, "123456").toString());
		assertEquals("AUTO linkId=123456", new TransitionCommandSpec("GET", Transition.AUTO, null, null, "123456").toString());
	}
}
