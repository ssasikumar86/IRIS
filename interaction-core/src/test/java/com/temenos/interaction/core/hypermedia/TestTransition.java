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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.temenos.interaction.core.hypermedia.expression.Expression;

public class TestTransition {

	@Test
	public void testEquality() {
		ResourceState begin = new ResourceState("entity", "", new ArrayList<Action>(), "/");
		ResourceState begin2 = new ResourceState("entity", "", new ArrayList<Action>(), "/");

		Transition.Builder tb = new Transition.Builder();
		tb.source(begin)
			.target(begin2)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t = tb.build();
		
		t.equals(null);
		
		Transition.Builder tb2 = new Transition.Builder();
		tb2.source(begin)
			.target(begin2)
			.method("PUT")
			.flags(Transition.FOR_EACH);		
		Transition t2 = tb2.build();
		assertEquals(t, t2);
		assertEquals(t.hashCode(), t2.hashCode());		
	}
	
	@Test
	public void testEqualityNullTarget() {
		ResourceState begin = new ResourceState("entity", "", new ArrayList<Action>(), "/");
		Transition.Builder tb = new Transition.Builder();
		tb.source(begin)
			.target(null)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t = tb.build();
		Transition.Builder tb2 = new Transition.Builder();
		tb2.source(begin)
			.target(null)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t2 = tb2.build();
		assertEquals(t, t2);
		assertEquals(t.hashCode(), t2.hashCode());
		
		t2.setTarget(mock(ResourceState.class));
		assertFalse(t.equals(t2));

		t2.setTarget(null);		
		t.setSource(mock(ResourceState.class));
		
		assertFalse(t.equals(t2));		
	}
	
	@Test
	public void testEqualityNullSource() {
		ResourceState begin2 = new ResourceState("entity", "", new ArrayList<Action>(), "/");
		Transition.Builder tb = new Transition.Builder();
		tb.source(null)
			.target(begin2)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t = tb.build();
		Transition.Builder tb2 = new Transition.Builder();
		tb2.source(null)
			.target(begin2)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t2 = tb2.build();
		assertEquals(t, t2);
		assertEquals(t.hashCode(), t2.hashCode());
		
		t2.setSource(mock(ResourceState.class));
		assertFalse(t.equals(t2));

		t2.setSource(null);		
		t.setSource(mock(ResourceState.class));
		
		assertFalse(t.equals(t2));		
	}
	
	
	@Test
	public void testEqualityNullSourceName() {
		ResourceState begin2 = new ResourceState("entity", "", new ArrayList<Action>(), "/");
		Transition.Builder tb = new Transition.Builder();

		ResourceState state = mock(ResourceState.class);
		when(state.getName()).thenReturn("target");		
		
		tb.source(state)
			.target(begin2)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t = tb.build();
		Transition.Builder tb2 = new Transition.Builder();
		
		ResourceState state2 = mock(ResourceState.class);
		
		tb2.source(state2)
			.target(begin2)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t2 = tb2.build();
		
		assertFalse(t.equals(t2));
		
		t2.setSource(state);
		t.setSource(state2);
		
		assertFalse(t.equals(t2));		
	}
	
	@Test
	public void testEqualityNullTargetName() {
		ResourceState begin = new ResourceState("entity", "", new ArrayList<Action>(), "/");
		Transition.Builder tb = new Transition.Builder();

		ResourceState state = mock(ResourceState.class);
		when(state.getName()).thenReturn("target");		
		
		tb.target(state)
			.source(begin)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t = tb.build();
		Transition.Builder tb2 = new Transition.Builder();
		
		ResourceState state2 = mock(ResourceState.class);
		
		tb2.target(state2)
			.source(begin)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t2 = tb2.build();
		
		assertFalse(t.equals(t2));
		
		t2.setTarget(state);
		t.setTarget(state2);
		
		assertFalse(t.equals(t2));		
	}	
	

	@Test 
	public void testInequality() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "/");
		ResourceState exists = new ResourceState("entity", "exists", new ArrayList<Action>(), "{id}");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "/");

		Transition.Builder tb = new Transition.Builder();
		tb.source(begin)
			.target(end)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t = tb.build();
		Transition.Builder tb2 = new Transition.Builder();
		tb2.source(begin)
			.target(exists)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t2 = tb2.build();
		assertFalse(t.equals(t2));
		assertFalse(t.hashCode() == t2.hashCode());
		
		Transition.Builder tb3 = new Transition.Builder();
		tb3.source(begin)
			.target(end)
			.method("PUT")
			.flags(Transition.FOR_EACH);
		Transition t3 = tb3.build();
		Transition.Builder tb4 = new Transition.Builder();
		tb4.source(begin)
			.target(end)
			.flags(Transition.AUTO);
		Transition t4 = tb2.build();
		assertFalse(t3.equals(t4));
		assertFalse(t3.hashCode() == t4.hashCode());

	}

	@Test 
	public void testInequalityUriParameters() {
		ResourceState begin = new ResourceState("entity", "collection", new ArrayList<Action>(), "/");
		ResourceState exists = new ResourceState("entity", "onetype", new ArrayList<Action>(), "{id}");

		Map<String, String> uriParameters = new HashMap<String, String>();
		uriParameters.put("id", "abc");
		Transition t = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.flags(Transition.FOR_EACH)
				.uriParameters(uriParameters)
				.target(exists)
				.build();
		
		uriParameters.clear();
		uriParameters.put("id", "xyz");
		Transition t2 = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.flags(Transition.FOR_EACH)
				.uriParameters(uriParameters)
				.target(exists)
				.build();
		assertFalse(t.equals(t2));
		assertFalse(t.hashCode() == t2.hashCode());
	}
	
	@Test
	public void testGetId() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");

		Transition t = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.target(end)
				.build();
		assertEquals("entity.begin>PUT>entity.end", t.getId());
	}

	@Test
	public void testToString() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "/begin");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "/end");

		Transition.Builder tb = new Transition.Builder();
		tb.source(begin)
			.target(end)
			.method("PUT");
		Transition t = tb.build();
		assertEquals("entity.begin>PUT>entity.end", t.toString());
	}

	@Test
	public void testGetLabel() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");

		Transition.Builder tba = new Transition.Builder();
		tba.source(begin)
			.target(end)
			.method("GET")
			.label("A");
		Transition ta = tba.build();
		assertEquals("A", ta.getLabel());
		Transition.Builder tbb = new Transition.Builder();
		tbb.source(begin)
			.target(end)
			.method("GET")
			.label("B");
		Transition tb = tbb.build();
		assertEquals("B", tb.getLabel());
	}

	@Test
	public void testIdMultiTransitions() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");

		Transition ta = new Transition.Builder()
				.source(begin)
				.method("GET")
				.target(end)
				.label("A")
				.build();
		Transition taPut = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.target(end)
				.label("A")
				.build();
		assertEquals("entity.begin>GET(A)>entity.end", ta.getId());
		assertEquals("entity.begin>PUT(A)>entity.end", taPut.getId());
		Transition tb = new Transition.Builder()
				.source(begin)
				.method("GET")
				.target(end)
				.label("B")
				.build();
		assertEquals("entity.begin>GET(B)>entity.end", tb.getId());
	}

	@Test
	public void testCheckTransitionFromCollectionToEntityResource() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");
		Transition t = new Transition.Builder()
				.source(begin).method("GET").target(end).build();
		assertFalse(t.isGetFromCollectionToEntityResource());

		begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		end = new CollectionResourceState("entity", "end", new ArrayList<Action>(), "{id}");
		t = new Transition.Builder()
				.source(begin).method("GET").target(end).build();
		assertFalse(t.isGetFromCollectionToEntityResource());

		begin = new CollectionResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");
		t = new Transition.Builder()
				.source(begin).method("GET").target(end).build();
		assertTrue(t.isGetFromCollectionToEntityResource());

		begin = new CollectionResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		end = new ResourceState("otherEntity", "end", new ArrayList<Action>(), "{id}");
		t = new Transition.Builder()
				.source(begin).method("GET").target(end).build();
		assertFalse(t.isGetFromCollectionToEntityResource());

		begin = new CollectionResourceState("otherEntity", "begin", new ArrayList<Action>(), "{id}");
		end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");
		t = new Transition.Builder()
				.source(begin).method("GET").target(end).build();
		assertFalse(t.isGetFromCollectionToEntityResource());
	}

	@Test
	public void testIdMultiTransitionsWithParametersNoLabel() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");

		Map<String, String> params = new HashMap<String, String>();
		params.put("paramA", "hello A");
		Transition ta = new Transition.Builder()
				.source(begin)
				.method("GET")
				.uriParameters(params)
				.target(end)
				.build();
		Transition taPut = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.target(end)
				.build();
		assertEquals("entity.begin>GET>entity.end", ta.getId());
		assertEquals("entity.begin>PUT>entity.end", taPut.getId());
		params = new HashMap<String, String>();
		params.put("paramB", "hello B");
		Transition tb = new Transition.Builder()
				.source(begin)
				.method("GET")
				.uriParameters(params)
				.target(end)
				.build();
		assertEquals("entity.begin>GET>entity.end", tb.getId());
	}
	
	@Test
	public void testGetLinkId() {
		ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
		ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");

		Transition.Builder tba = new Transition.Builder();
		tba.source(begin)
			.target(end)
			.method("GET")
			.label("A")
			.linkId("123456");
		Transition ta = tba.build();
		assertEquals("123456", ta.getLinkId());
		Transition.Builder tbb = new Transition.Builder();
		tbb.source(begin)
			.target(end)
			.method("GET")
			.label("B")
			.linkId("123456");
		Transition tb = tbb.build();
		assertEquals("123456", tb.getLinkId());
	}
	
	@Test
	public void testResourceLocator() {
        ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
        ResourceLocator locator = mock(ResourceLocator.class);

        Transition.Builder tba = new Transition.Builder();
        tba.source(begin)
            .locator(locator)
            .method("GET")
            .label("A")
            .linkId("123456");
        Transition t = tba.build();
        assertEquals(locator, t.getLocator());
	}
	
    @Test
    public void testEvaluation() {
        ResourceState begin = new ResourceState("entity", "begin", new ArrayList<Action>(), "{id}");
        ResourceState end = new ResourceState("entity", "end", new ArrayList<Action>(), "{id}");
        Expression expression = mock(Expression.class);
        Transition.Builder tba = new Transition.Builder();
        tba.source(begin)
            .target(end)
            .method("GET")
            .evaluation(expression)
            .label("A")
            .linkId("123456");
        Transition t = tba.build();
        assertEquals(expression, t.getCommand().getEvaluation());
    }	
	
	@Test 
	public void testEqualityLinkId() {
		ResourceState begin = new ResourceState("entity", "collection", new ArrayList<Action>(), "/");
		ResourceState exists = new ResourceState("entity", "onetype", new ArrayList<Action>(), "{id}");

		Transition t = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.flags(Transition.FOR_EACH)
				.target(exists)
				.label("label1")
				.linkId("123456")
				.build();
		Transition t2 = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.flags(Transition.FOR_EACH)
				.target(exists)
				.label("label1")
				.linkId("123456")
				.build();
		assertTrue(t.equals(t2));
		assertTrue(t.hashCode() == t2.hashCode());
	}
	
	@Test 
	public void testInequalityLinkId() {
		ResourceState begin = new ResourceState("entity", "collection", new ArrayList<Action>(), "/");
		ResourceState exists = new ResourceState("entity", "onetype", new ArrayList<Action>(), "{id}");

		Transition t = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.flags(Transition.FOR_EACH)
				.target(exists)
				.label("label1")
				.linkId("12345")
				.build();
		Transition t2 = new Transition.Builder()
				.source(begin)
				.method("PUT")
				.flags(Transition.FOR_EACH)
				.target(exists)
				.label("label1")
				.linkId("67890")
				.build();
		assertFalse(t.equals(t2));
		assertFalse(t.hashCode() == t2.hashCode());
		
		t.setLinkId(null);		
		assertFalse(t.equals(t2));
		
		t.setLinkId("12345");
		t2.setLinkId(null);		
		assertFalse(t.equals(t2));
		
		t.setLinkId(null);
		assertFalse(t.equals(t2));
	}
	
	@Test	
	public void testEqualityCommand() {
		ResourceState begin = new ResourceState("entity", "collection", new ArrayList<Action>(), "/");
		ResourceState exists = new ResourceState("entity", "onetype", new ArrayList<Action>(), "{id}");
		String label = "label1";
		
		Transition t = new Transition.Builder()
			.source(begin)
			.method("PUT")
			.flags(Transition.FOR_EACH)
			.target(exists)
			.label(label)
			.linkId("12345")
			.build();
		
		Transition t2 = new Transition.Builder()
			.source(begin)
			.method("PUT")
			.flags(Transition.FOR_EACH)
			.target(exists)
			.label(label)
			.linkId("12345")
			.build();
		
		assertTrue(t.equals(t2));		
	}
	
	@Test	
	public void testInequalityLabel() {
		ResourceState begin = new ResourceState("entity", "collection", new ArrayList<Action>(), "/");
		ResourceState exists = new ResourceState("entity", "onetype", new ArrayList<Action>(), "{id}");
		String label = "label1";
		
		Transition t = new Transition.Builder()
			.source(begin)
			.method("PUT")
			.flags(Transition.FOR_EACH)
			.target(exists)
			.label(null)
			.linkId("12345")
			.build();
		
		Transition t2 = new Transition.Builder()
			.source(begin)
			.method("PUT")
			.flags(Transition.FOR_EACH)
			.target(exists)
			.label(label)
			.linkId("12345")
			.build();
		
		assertFalse(t.equals(t2));
		assertFalse(t.hashCode() == t2.hashCode());		
		
		t = new Transition.Builder()
			.source(begin)
			.method("PUT")
			.flags(Transition.FOR_EACH)
			.target(exists)
			.label(label)
			.linkId("12345")
			.build();

		t2 = new Transition.Builder()
		.source(begin)
		.method("PUT")
		.flags(Transition.FOR_EACH)
		.target(exists)
		.label(null)
		.linkId("12345")
		.build();
	
		assertFalse(t.equals(t2));
		
		t = new Transition.Builder()
		.source(begin)
		.method("PUT")
		.flags(Transition.FOR_EACH)
		.target(exists)
		.label(null)
		.linkId("12345")
		.build();		
		
		assertTrue(t.equals(t2));		
	}	
}
