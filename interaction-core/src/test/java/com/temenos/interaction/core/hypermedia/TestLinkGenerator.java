package com.temenos.interaction.core.hypermedia;

/*
 * #%L
 * interaction-core
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


import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.hypermedia.*;
import com.temenos.interaction.core.web.RequestContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.odata4j.core.*;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmEntitySet;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by ikarady on 06/04/2016.
 */
public class TestLinkGenerator {

    @Before
    public void setup() {
        // initialise the thread local request context with requestUri and baseUri
        RequestContext ctx = new RequestContext("/baseuri", "/requesturi", null);
        RequestContext.setRequestContext(ctx);
    }

    @Test
    public void testCreateLinkHrefSimple() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class));
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test"))
                .build();
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, null);
        Link result = (!links.isEmpty()) ? links.iterator().next() : null;
        assertEquals("/baseuri/test", result.getHref());
    }

    @Test
    public void testCreateLinkHrefReplaceUsingEntity() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test/{noteId}"))
                .build();
        Object entity = new TestNote("123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Link result = (!links.isEmpty()) ? links.iterator().next() : null;
        assertEquals("/baseuri/test/123", result.getHref());
    }

    @Test
    public void testCreateLinkHrefUriParameterTokensReplaceUsingEntity() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String,String> uriParameters = new HashMap<String,String>();
        uriParameters.put("test", "{noteId}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test"))
                .uriParameters(uriParameters)
                .build();

        Object entity = new TestNote("123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Link result = (!links.isEmpty()) ? links.iterator().next() : null;
        assertEquals("/baseuri/test?test=123", result.getHref());
    }

    @Test
    public void testCreateLinkHrefUriParameterTokensReplaceQueryParameters() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String,String> uriParameters = new HashMap<String,String>();
        uriParameters.put("test", "{noteId}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test"))
                .uriParameters(uriParameters)
                .build();
        MultivaluedMap<String,String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("noteId", "123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, queryParameters, null);
        Link result = (!links.isEmpty()) ? links.iterator().next() : null;
        assertEquals("/baseuri/test?test=123", result.getHref());
    }

    @Test
    public void testCreateLinkHrefUriParameterTokensReplaceQueryParametersSpecial() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Map<String,String> uriParameters = new HashMap<String,String>();
        uriParameters.put("filter", "{$filter}");
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test"))
                .uriParameters(uriParameters)
                .build();
        MultivaluedMap<String,String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("$filter", "123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, queryParameters, null);
        Link result = (!links.isEmpty()) ? links.iterator().next() : null;
        assertEquals("/baseuri/test?filter=123", result.getHref());
    }

    @Test
    public void testCreateLinkHrefAllQueryParameters() {
        ResourceStateMachine engine = new ResourceStateMachine(mock(ResourceState.class), new BeanTransformer());
        Transition t = new Transition.Builder().source(mock(ResourceState.class)).target(mockTarget("/test")).
                build();
        MultivaluedMap<String,String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("$filter", "123");
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null).setAllQueryParameters(true);
        Collection<Link> links = linkGenerator.createLink(new MultivaluedMapImpl<String>(), queryParameters, null);
        Link result = (!links.isEmpty()) ? links.iterator().next() : null;
        assertEquals("/baseuri/test?$filter=123", result.getHref());
    }

    @Test
    public void testCreateLinkFromDynamicResource() {
        ResourceStateMachine engineMock = Mockito.mock(ResourceStateMachine.class);
        ResourceStateAndParameters resourceStateAndParameters = new ResourceStateAndParameters();
        resourceStateAndParameters.setState(mockTarget("/testDynamic"));
        resourceStateAndParameters.setParams(new ParameterAndValue[] {new ParameterAndValue("filter2", "564")});
        DynamicResourceState dynamicResourceStateMock = mockDynamicTarget(null);
        Map<String,String> uriParameters = new HashMap<String,String>();
        uriParameters.put("filter", "{nodeId}");
        Transition t = new Transition.Builder()
                .source(mock(ResourceState.class))
                .target(dynamicResourceStateMock)
                .uriParameters(uriParameters)
                .build();
        MultivaluedMap<String,String> queryParameters = new MultivaluedMapImpl<String>();
        queryParameters.add("nodeId", "123");
        Map<String, Object> transitionProperties = new HashMap<String, Object>();
        transitionProperties.put("filter", "{nodeId}");
        transitionProperties.put("nodeId", "123");
        Mockito.when(engineMock.resolveDynamicState(
                dynamicResourceStateMock,
                transitionProperties,
                null))
                .thenReturn(resourceStateAndParameters);
        Mockito.when(engineMock.getTransitionProperties(t, null, new MultivaluedMapImpl<String>(), queryParameters)).thenReturn(transitionProperties);

        LinkGenerator linkGenerator = new LinkGeneratorImpl(engineMock, t, null).setAllQueryParameters(true);
        Collection<Link> links = linkGenerator.createLink(new MultivaluedMapImpl<String>(), queryParameters, null);
        Link result = (!links.isEmpty()) ? links.iterator().next() : null;
        assertEquals("/baseuri/testDynamic?filter=123&filter2=564", result.getHref());
    }

    @Test
    public void testCreateLinkForCollectionEntity() {
        Link result = null;
        Map<String,String> uriParameters = new HashMap<String,String>();
        uriParameters.put("test", "{Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        customerState.addTransition(new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).build());
        OCollection<?> contactColl = OCollections.newBuilder(null)
                .add(createComplexObject("Email","johnEmailAddr","Tel","12345"))
                .add(createComplexObject("Email","smithEmailAddr","Tel","66778")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp =  OProperties.collection("source_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=smithEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
    }

    @Test
    public void testCreateLinkForCollectionEntityTwoLevel() {
        Link result = null;
        Map<String,String> uriParameters = new HashMap<String,String>();
        uriParameters.put("test", "{Contact.Address.PostCode}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        customerState.addTransition(new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).build());

        //Inner collection
        OCollection<?> postCodeColl = OCollections.newBuilder(null).add(createComplexObject("PostCode", "ABCD")).add(createComplexObject("PostCode", "EFGH")).build();

        //Outer Collection
        OProperty<?> addressCollProperty =  OProperties.collection("Address", null, postCodeColl);
        List<OProperty<?>> addressPropList = new ArrayList<OProperty<?>>();
        addressPropList.add(addressCollProperty);
        OComplexObject addressDetails = OComplexObjects.create(EdmComplexType.newBuilder().build(), addressPropList);
        OCollection<?> addressColl = OCollections.newBuilder(null).add(addressDetails).build();

        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(addressColl));

        OProperty<?> contactProp =  OProperties.collection("source_Contact", null, addressColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        Collection<Link> links = linkGenerator.createLink(null, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=ABCD", result.getHref());
        assertEquals("collection", result.getRel());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=EFGH", result.getHref());
        assertEquals("collection", result.getRel());
    }

    @Test
    public void testCreateLinkForCollectionEntityMultiParams() {
        Link result = null;
        Map<String,String> uriParameters = new HashMap<String,String>();
        uriParameters.put("test", "{personName} and {Contact.Email}");
        CollectionResourceState customerState = new CollectionResourceState("customer", "customer", new ArrayList<Action>(), "/customer()", null, null);
        CollectionResourceState contactState = new CollectionResourceState("contact", "contact", new ArrayList<Action>(), "/contact()", null, null);
        customerState.addTransition(new Transition.Builder().method("GET").target(contactState).uriParameters(uriParameters).flags(Transition.FOR_EACH).build());
        OCollection<?> contactColl = OCollections.newBuilder(null)
                .add(createComplexObject("Email","johnEmailAddr","Tel","12345"))
                .add(createComplexObject("Email","smithEmailAddr","Tel","66778")).build();
        ResourceStateMachine engine = new ResourceStateMachine(customerState, getOEntityTransformer(contactColl));

        OProperty<?> contactProp =  OProperties.collection("source_Contact", null, contactColl);
        List<OProperty<?>> contactPropList = new ArrayList<OProperty<?>>();
        contactPropList.add(contactProp);
        OEntity entity = OEntities.createRequest(EdmEntitySet.newBuilder().build(), contactPropList, null);
        Transition t = customerState.getTransitions().get(0);
        LinkGenerator linkGenerator = new LinkGeneratorImpl(engine, t, null);
        MultivaluedMap<String, String> pathParameters = new MultivaluedMapImpl<String>();
        pathParameters.add("personName", "John");
        Collection<Link> links = linkGenerator.createLink(pathParameters, null, entity);
        Iterator<Link> iterator = links.iterator();

        assertEquals(2, links.size());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=John+and+johnEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());

        result = iterator.next();
        assertEquals("/baseuri/contact()?test=John+and+smithEmailAddr", result.getHref());
        assertEquals("collection", result.getRel());
    }

    private OComplexObject createComplexObject(String... values)
    {
        List<OProperty<?>> propertyList = new ArrayList<OProperty<?>>();
        for(int i=0; i<values.length; i+=2)
        {
            OProperty<String> property = OProperties.string(values[i], values[i+1]);
            propertyList.add(property);
        }
        OComplexObject complexObj = OComplexObjects.create(EdmComplexType.newBuilder().build(),propertyList);
        return complexObj;
    }

    private Transformer getOEntityTransformer(OCollection<?> collection)
    {
        Map<String, Object> entityProperties = new HashMap<String, Object>();
        entityProperties.put("source_Contact", collection);
        Transformer transformerMock = mock(Transformer.class);
        when(transformerMock.transform(anyObject())).thenReturn(entityProperties);
        return transformerMock;
    }

    private ResourceState mockTarget(String path) {
        ResourceState target = mock(ResourceState.class);
        when(target.getPath()).thenReturn(path);
        when(target.getRel()).thenReturn("collection");
        return target;
    }

    private DynamicResourceState mockDynamicTarget(String path) {
        DynamicResourceState target = mock(DynamicResourceState.class);
        when(target.getPath()).thenReturn(path);
        return target;
    }

    public static class TestNote {
        String noteId;

        public TestNote(String noteId) {
            this.noteId = noteId;
        }

        public String getNoteId() {
            return noteId;
        }
    }

}
