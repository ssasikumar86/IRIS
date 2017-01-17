package com.temenos.interaction.core.hypermedia.transition;

/*
 * #%L
 * interaction-core
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


import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.*;
import com.temenos.interaction.core.command.InteractionCommand.Result;
import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.EntityMetadata;
import com.temenos.interaction.core.entity.EntityProperties;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.*;
import com.temenos.interaction.core.resource.EntityResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response.Status;
import java.util.*;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author ikarady
 */
public class TestAutoTransitioner {

    private static final String ENTITY_NAME = "Person";

    private @Mock EntityMetadata entityMetadataMock;
    private @Mock Metadata metadataMock;
    private @Mock Transformer transformerMock;

    private ResourceState defaultState;
    private ResourceState initialState;
    private Entity entity0;
    private Entity entity1;
    private Entity entity2;
    private Entity entity3;
    private Entity entity4;

    @Before
    public void setup() throws InteractionException {
        MockitoAnnotations.initMocks(this);

        when(metadataMock.getEntityMetadata(any(String.class))).thenReturn(entityMetadataMock);
        when(transformerMock.transform(Matchers.any())).thenReturn(new HashMap<String, Object>());

        defaultState = new ResourceState(ENTITY_NAME, "defaultState", new ArrayList<Action>(), "/defaultState");
        initialState = new ResourceState(ENTITY_NAME, "initialState", new ArrayList<Action>(), "/initialState");
        entity0 = new Entity("Customer0", new EntityProperties());
        entity1 = new Entity("Customer1", new EntityProperties());
        entity2 = new Entity("Customer2", new EntityProperties());
        entity3 = new Entity("Customer3", new EntityProperties());
        entity4 = new Entity("Customer4", new EntityProperties());
    }

    @Test
    public void testTransition() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", new ArrayList<Action>());
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state2, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertNull(autoTransitioner.getOutcome().getInteractionContext().getResourceEntity());
    }

    @Test
    public void testTransitionWithEntityIn() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().setEntity(entity0).build(),
                transformerMock,
                stubCommandController(new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().build())),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state2, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals(entity0, autoTransitioner.getOutcome().getInteractionContext().getResourceEntity());
    }

    @Test
    public void testTransitionWithEntityInOut() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().setEntity(entity0).build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().setEntity(entity1).build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state2, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals(entity1, autoTransitioner.getOutcome().getInteractionContext().getResourceEntity());
    }

    @Test
    public void testTransitionWithPathParameter() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().addPathParameters("path").build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state2, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals("path", autoTransitioner.getOutcome().getInteractionContext().getPathParameters().getFirst("path"));
        assertFalse(autoTransitioner.getOutcome().getInteractionContext().getQueryParameters().containsKey("path"));
    }

    @Test
    public void testTransitionWithQueryParameter() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().addQueryParameters("query").build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state2, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals("query", autoTransitioner.getOutcome().getInteractionContext().getPathParameters().getFirst("query"));
        assertEquals("query", autoTransitioner.getOutcome().getInteractionContext().getQueryParameters().getFirst("query"));
    }

    @Test
    public void testTransitionFailure() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getFailureCommandBuilder().build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertFalse(autoTransitioner.transition().isSuccessful());
    }

    @Test
    public void testTransitionError() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getErrorCommandBuilder().build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertFalse(autoTransitioner.transition().isSuccessful());
    }

    @Test
    public void testMultipleTransitions() {
        ResourceState state1 = new ResourceState(initialState, "state1", toList(new Action("GET", Action.TYPE.VIEW)));
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("DO", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(initialState, "state3", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state4 = new ResourceState(initialState, "state4", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).flags(Transition.AUTO).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        initialState.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        initialState.addTransition(new Transition.Builder().target(state4).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().setEntity(entity0).build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("GET", getFailureCommandBuilder().setEntity(entity1).build()),
                        new AbstractMap.SimpleEntry<>("DO", getErrorCommandBuilder().setEntity(entity2).build()),
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setEntity(entity3).build()),
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().setEntity(entity4).build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state3, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals(entity3, autoTransitioner.getOutcome().getInteractionContext().getResourceEntity());
    }

    @Test
    public void testDynamicTransition() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new DynamicResourceState(ENTITY_NAME, "state2", null, "path");
        ResourceState state3 = new ResourceState(initialState, "state3", new ArrayList<Action>());
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(),
                mockResourceLocatorProvider(state3),
                mockLazyResourceStateResolver())
                .setParameterResolverProvider(mockResourceParameterResolverProvider());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state3, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals("path", autoTransitioner.getOutcome().getInteractionContext().getPathParameters().getFirst("path"));
        assertEquals("path", autoTransitioner.getOutcome().getInteractionContext().getQueryParameters().getFirst("path"));
    }

    @Test
    public void testLazyTransition() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new LazyResourceState("state2");
        ResourceState state3 = new ResourceState(initialState, "state3", new ArrayList<Action>());
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver(state3));

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state3, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
    }

    @Test
    public void testTransitionThroughInterim() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setInterim(true).build()),
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state3, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
    }

    @Test
    public void testTransitionThroughInterimWithInterimContentOnly() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().setEntity(entity0).build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setInterim(true).setEntity(entity1).build()),
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().build())),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state3, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals(entity1, autoTransitioner.getOutcome().getInteractionContext().getResourceEntity());
    }

    @Test
    public void testTransitionThroughInterimWithContent() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().setEntity(entity0).build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setInterim(true).setEntity(entity1).build()),
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().setEntity(entity2).build())),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state3, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals(entity2, autoTransitioner.getOutcome().getInteractionContext().getResourceEntity());
    }

    @Test
    public void testInterimTransitionOnly() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setInterim(true).build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertFalse(autoTransitioner.transition().isSuccessful());
    }

    @Test
    public void testTransitionWithOrphanInterim() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().setEntity(entity0).build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setEntity(entity1).build()),
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().setInterim(true).setEntity(entity2).build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertTrue(autoTransitioner.transition().isSuccessful());
        assertEquals(state2, autoTransitioner.getOutcome().getInteractionContext().getCurrentState());
        assertEquals(entity1, autoTransitioner.getOutcome().getInteractionContext().getResourceEntity());
    }

    @Test
    public void testTransitionFailureThroughInterim() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setInterim(true).build()),
                        new AbstractMap.SimpleEntry<>("POST", getFailureCommandBuilder().build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertFalse(autoTransitioner.transition().isSuccessful());
    }

    @Test
    public void testInterimTransitionFailure() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getFailureCommandBuilder().setInterim(true).build()),
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().build())),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertFalse(autoTransitioner.transition().isSuccessful());
    }

    @Test
    public void testTransitionErrorThroughInterim() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getSuccessCommandBuilder().setInterim(true).build()),
                        new AbstractMap.SimpleEntry<>("POST", getErrorCommandBuilder().build())
                ),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertFalse(autoTransitioner.transition().isSuccessful());
    }

    @Test
    public void testInterimTransitionError() {
        ResourceState state1 = new ResourceState(initialState, "state1", new ArrayList<Action>());
        ResourceState state2 = new ResourceState(initialState, "state2", toList(new Action("PUT", Action.TYPE.ENTRY)));
        ResourceState state3 = new ResourceState(state2, "state3", toList(new Action("POST", Action.TYPE.ENTRY)));
        initialState.addTransition(new Transition.Builder().target(state1).build());
        initialState.addTransition(new Transition.Builder().target(state2).flags(Transition.AUTO).build());
        state2.addTransition(new Transition.Builder().target(state3).flags(Transition.AUTO).build());
        AutoTransitioner autoTransitioner = new AutoTransitioner(
                new InteractionContextBuilder().build(),
                transformerMock,
                stubCommandController(
                        new AbstractMap.SimpleEntry<>("PUT", getErrorCommandBuilder().setInterim(true).build()),
                        new AbstractMap.SimpleEntry<>("POST", getSuccessCommandBuilder().build())),
                mockResourceLocatorProvider(),
                mockLazyResourceStateResolver());

        assertFalse(autoTransitioner.transition().isSuccessful());
    }

    @SafeVarargs
    private final CommandController stubCommandController(AbstractMap.SimpleEntry<String, TransitionCommand>... entries) {
        MapBasedCommandController commandController = new MapBasedCommandController();
        for (AbstractMap.SimpleEntry<String, TransitionCommand> entry : entries) {
            commandController.getCommandMap().put(entry.getKey(), entry.getValue());
        }
        return commandController;
    }

    private ResourceLocatorProvider mockResourceLocatorProvider() {
        return mockResourceLocatorProvider(defaultState);
    }

    private ResourceLocatorProvider mockResourceLocatorProvider(final ResourceState state) {
        ResourceLocator resourceLocator = new ResourceLocator() {
            @Override
            public ResourceState resolve(Object... alias) {
                return state;
            }};
        ResourceLocatorProvider resourceLocatorProviderMock = mock(ResourceLocatorProvider.class);
        when(resourceLocatorProviderMock.get(anyString())).thenReturn(resourceLocator);
        return resourceLocatorProviderMock;
    }

    private LazyResourceStateResolver mockLazyResourceStateResolver() {
        return mockLazyResourceStateResolver(defaultState);
    }
    
    private LazyResourceStateResolver mockLazyResourceStateResolver(ResourceState state) {
        ResourceStateProvider resourceStateProviderMock = Mockito.mock(ResourceStateProvider.class);
        when(resourceStateProviderMock.getResourceState(anyString())).thenReturn(state);
        return new LazyResourceStateResolver(resourceStateProviderMock);
    }

    private ResourceParameterResolverProvider mockResourceParameterResolverProvider() {
        ResourceParameterResolver parameterResolver = new ResourceParameterResolver() {
            @Override
            public ParameterAndValue[] resolve(Object[] aliases, ResourceParameterResolverContext context) {
                ParameterAndValue[] params = new ParameterAndValue[aliases.length];
                for (int i = 0; i < aliases.length; i++) {
                    String value = aliases[i].toString();
                    params[i] = new ParameterAndValue(value, value);
                }
                return params;
            }
        };
        ResourceParameterResolverProvider parameterResolverMock = mock(ResourceParameterResolverProvider.class);
        when(parameterResolverMock.get(anyString())).thenReturn(parameterResolver);
        return parameterResolverMock;
    }

    private CommandBuilder getSuccessCommandBuilder() {
        return new CommandBuilder().setResult(Result.SUCCESS);
    }

    private CommandBuilder getFailureCommandBuilder() {
        return new CommandBuilder().setResult(Result.FAILURE);
    }

    private CommandBuilder getErrorCommandBuilder() {
        return new CommandBuilder().setException(new InteractionException(Status.INTERNAL_SERVER_ERROR));
    }

    @SafeVarargs
    private final <T> List<T> toList(T... array) {
        List<T> list = new ArrayList<T>();
        Collections.addAll(list, array);
        return list;
    }

    private class CommandBuilder {
        private Result result;
        private Entity entity;
        private boolean isInterim;
        private InteractionException exception;

        CommandBuilder setResult(Result result) {
            this.result = result;
            return this;
        }

        CommandBuilder setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }

        CommandBuilder setInterim(boolean interim) {
            isInterim = interim;
            return this;
        }

        CommandBuilder setException(InteractionException exception) {
            this.exception = exception;
            return this;
        }

        TransitionCommand build() {
            return new TransitionCommand() {
                @Override
                public Result execute(InteractionContext ctx) throws InteractionException {
                    if (exception != null) {
                        throw exception;
                    } else if (entity != null) {
                        ctx.setResource(new EntityResource<>(entity.getName(), entity));
                    }
                    return result;
                }
                @Override
                public boolean isInterim() {
                    return isInterim;
                }
            };
        }

    }

    private class InteractionContextBuilder {

        private Entity entity = null;
        private MultivaluedMapImpl<String> pathParameters = new MultivaluedMapImpl<>();
        private MultivaluedMapImpl<String> queryParameters = new MultivaluedMapImpl<>();

        InteractionContextBuilder setEntity(Entity entity) {
            this.entity = entity;
            return this;
        }

        InteractionContextBuilder addPathParameters(String... pathParameters) {
            if (pathParameters != null) {
                for (String pathParameter : pathParameters) {
                    this.pathParameters.putSingle(pathParameter, pathParameter);
                }
            }
            return this;
        }

        InteractionContextBuilder addQueryParameters(String... queryParameters) {
            if (queryParameters != null) {
                for (String queryParameter : queryParameters) {
                    this.queryParameters.putSingle(queryParameter, queryParameter);
                }
            }
            return this;
        }

        InteractionContext build() {
            InteractionContext ctx = new InteractionContext(null, null, pathParameters, queryParameters, initialState, metadataMock);
            if (entity != null) {
                ctx.setResource(new EntityResource<>(entity.getName(), entity));
            }
            return ctx;
        }
    }

}
