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
import com.temenos.interaction.core.hypermedia.*;
import com.temenos.interaction.core.resource.RESTResource;
import com.temenos.interaction.core.workflow.TransitionWorkflowCommandBuilder;
import com.temenos.interaction.core.workflow.TransitionWorkflowStrategyCommandBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;

import static com.temenos.interaction.core.command.InteractionCommand.*;


/**
 * Transitions an {@link InteractionContext} into another {@link ResourceState}
 * according to the auto {@link Transition} tree defined for its current {@link ResourceState}.
 * During this process it gathers path parameters, query parameters, context attributes
 * and resource entities of each successful auto {@link Transition} that it processes
 * into {@link InteractionContext}.
 *
 * @author ikarady
 */
public class AutoTransitioner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoTransitioner.class);

    private InteractionContext originalCtx = null;
    private Transformer transformer = null;
    private TransitionWorkflowCommandBuilder transitionWorkflowCommandBuilder = null;
    private ResourceLocatorProvider resourceLocatorProvider = null;
    private LazyResourceStateResolver lazyResourceStateResolver = null;
    private ResourceParameterResolverProvider parameterResolverProvider = null;
    private Outcome outcome = null;

    public AutoTransitioner(InteractionContext ctx, Transformer transformer, CommandController commandController,
            ResourceLocatorProvider resourceLocatorProvider, LazyResourceStateResolver lazyResourceStateResolver) {
        this.originalCtx = copyInteractionContext(ctx);
        this.transformer = transformer;
        this.transitionWorkflowCommandBuilder = new TransitionWorkflowStrategyCommandBuilder(commandController);
        this.resourceLocatorProvider = resourceLocatorProvider;
        this.lazyResourceStateResolver = lazyResourceStateResolver;
    }

    public AutoTransitioner setParameterResolverProvider(ResourceParameterResolverProvider parameterResolverProvider) {
        this.parameterResolverProvider = parameterResolverProvider;
        return this;
    }

    /**
     * Returns the {@link Outcome Outcome} of auto transitioning.
     *
     * @return {@link Outcome Outcome}
     *
     */
    public Outcome getOutcome() {
        return outcome;
    }

    /**
     * Traverses through a tree of auto transitions defined for the current {@link ResourceState}
     * in {@link InteractionContext} to find one successful branch.
     * At each level in the tree it picks the first successful auto {@link Transition} ignoring the rest.
     * Hence a successful branch is made up of auto {@link Transition}s that represent the first successful
     * auto {@link Transition} of their level.
     * Traversal stops when there are no more successful auto transitions at any given level.
     * The result of a successful auto {@link Transition} is a new {@link ResourceState}
     * which is saved into the overall {@link Outcome Outcome} along with any path parameters, query parameters,
     * context attributes and resource entities.
     *
     * @return {@link Outcome Outcome}
     *
     */
    public Outcome transition() {
        if (outcome != null) {
            return outcome;
        }
        outcome = new Outcome();
        outcome.setRestResource(originalCtx.getResource());
        outcome.addPathParameters(copyParameters(originalCtx.getPathParameters()));
        outcome.addQueryParameters(copyParameters(originalCtx.getQueryParameters()));
        outcome.addCtxAttributes(copyProperties(originalCtx.getAttributes()));
        outcome.addEntityProperties(originalCtx.getResource());
        try {
            for (Transition autoTransition : originalCtx.getCurrentState().getAutoTransitions()) {
                if(transition(autoTransition)) {
                    return outcome;
                }
            }
        } catch (ResourceStateRevisitedException e) {
            LOGGER.error("Auto transitioned into same resource state multiple times", e);
        }
        return outcome;
    }

    private boolean transition(Transition transition) throws ResourceStateRevisitedException {
        Outcome currentOutcome = new Outcome(outcome);
        currentOutcome.setState(lazyResourceStateResolver.resolve(transition.getTarget()));
        currentOutcome.addTransitionProperties(transition);
        if (transition.getTarget() instanceof DynamicResourceState) {
            DynamicResourceStateResolver dynamicStateResolver = new DynamicResourceStateResolver((DynamicResourceState) transition.getTarget(), resourceLocatorProvider)
                    .setParameterResolverProvider(parameterResolverProvider)
                    .addProperties(currentOutcome.getTransitionPropertiesBuilder().build())
                    .addProperties(currentOutcome.getCtxAttributes())
                    .addPathParameters(currentOutcome.getPathParameters())
                    .addQueryParameters(currentOutcome.getQueryParameters());
            ResourceStateAndParameters stateAndParameters = dynamicStateResolver.resolve();
            if (stateAndParameters == null) {
                return false;
            }
            currentOutcome.addPathParameters(dynamicStateResolver.getPathParameters());
            currentOutcome.addQueryParameters(dynamicStateResolver.getQueryParameters());
            currentOutcome.setState(lazyResourceStateResolver.resolve(stateAndParameters.getState()));
        }
        if (currentOutcome.getState() == null) {
            return false;
        }
        currentOutcome.addCommand(currentOutcome.buildCommand());
        InteractionContext ctx = currentOutcome.buildInteractionContext(currentOutcome.getPathParameters());
        currentOutcome.evaluate(ctx);
        if (currentOutcome.isSuccessful() || currentOutcome.isInterim()) {
            currentOutcome.setRestResource(ctx.getResource());
            currentOutcome.addOutQueryParameters(ctx.getOutQueryParameters());
            currentOutcome.addCtxAttributes(ctx.getAttributes());
            currentOutcome.addEntityProperties(ctx.getResource());
            outcome.add(currentOutcome);
            for (Transition autoTransition : currentOutcome.getState().getAutoTransitions()) {
                if (transition(autoTransition)) {
                    currentOutcome.setInterimSuccessful(true);
                    return true;
                }
            }
            currentOutcome.setInterimSuccessful(false);
        }
        return currentOutcome.isSuccessful();
    }

    protected InteractionContext copyInteractionContext(InteractionContext ctx) {
        return new InteractionContext(
                ctx,
                ctx.getHeaders(),
                copyParameters(ctx.getPathParameters()),
                copyParameters(ctx.getQueryParameters()),
                ctx.getCurrentState());
    }

    protected MultivaluedMap<String, String> copyParameters(MultivaluedMap<String, String> parameters) {
        MultivaluedMap<String, String> parametersCopy = new MultivaluedMapImpl<>();
        parametersCopy.putAll(parameters);
        return parametersCopy;
    }

    protected Map<String, Object> copyProperties(Map<String, Object> properties) {
        Map<String, Object> propertiesCopy = new HashMap<>();
        propertiesCopy.putAll(properties);
        return propertiesCopy;
    }

    protected MultivaluedMap<String, String> toParameters(Map<String, Object> properties) {
        MultivaluedMap<String, String> parameters = new MultivaluedMapImpl<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (properties.get(entry.getKey()) != null)
                parameters.add(entry.getKey(), entry.getValue().toString());
        }
        return parameters;
    }

    /**
     * Represents the outcome of auto transitioning process which can be either successful or not.
     * If successful a new {@link InteractionContext} can be requested with updated current {@link ResourceState},
     * path parameters, query parameters, context attributes and resource entity.
     *
     * @author ikarady
     */
    public class Outcome {

        private TransitionPropertiesBuilder transitionPropertiesBuilder = new TransitionPropertiesBuilder(transformer);
        private MultivaluedMap<String, String> outQueryParameters = new MultivaluedMapImpl<>();
        private Map<String, Object> ctxAttributes = new HashMap<>();
        private RESTResource restResource = null;
        private Boolean isSuccessful = null;
        private ResourceState state = null;
        private Set<ResourceState> visitedStates = new HashSet<>();
        private TransitionCommand command = null;
        private TransitionCommand delayedCommand = null;
        private Outcome interimOutcome = null;


        private Outcome() {}

        private Outcome (Outcome other) {
            if (other.getInterimOutcome() != null && other.getInterimOutcome().isPending()) {
                this.apply(other.getInterimOutcome());
            } else {
                this.apply(other);
            }
        }

        /**
         * Returns true if the auto transitioning process was a success overall otherwise false.
         * Success means it had at least one successful auto {@link Transition}.
         *
         * @return true or false
         *
         */
        public boolean isSuccessful() {
            return isSuccessful != null && isSuccessful;
        }

        /**
         * Returns a new {@link InteractionContext} with updated current {@link ResourceState},
         * path parameters, query parameters, context attributes and resource entity.
         *
         * @return {@link InteractionContext}
         *
         */
        public InteractionContext getInteractionContext() {
            InteractionContext ctx = buildInteractionContext(toParameters(transitionPropertiesBuilder.build()));
            return ctx;
        }

        protected InteractionContext buildInteractionContext(MultivaluedMap<String, String> pathParameters) {
            InteractionContext ctx = new InteractionContext(
                    originalCtx,
                    originalCtx.getHeaders(),
                    copyParameters(pathParameters),
                    copyParameters(getQueryParameters()),
                    getState());
            ctx.setTargetState(ctx.getCurrentState());
            ctx.setResource(getRestResource());
            ctx.getAttributes().putAll(getCtxAttributes());
            ctx.getOutQueryParameters().putAll(getOutQueryParameters());
            return ctx;
        }

        private boolean isPending() {
            return isSuccessful == null;
        }

        private TransitionPropertiesBuilder getTransitionPropertiesBuilder() {
            return new TransitionPropertiesBuilder(transitionPropertiesBuilder);
        }

        private void setTransitionPropertiesBuilder(TransitionPropertiesBuilder transitionPropertiesBuilder) {
            this.transitionPropertiesBuilder = transitionPropertiesBuilder;
        }

        private MultivaluedMap<String, String> getPathParameters() {
            return transitionPropertiesBuilder.getPathParameters();
        }

        private void addPathParameters(MultivaluedMap<String, String> pathParameters) {
            this.transitionPropertiesBuilder.addPathParameters(pathParameters);
        }

        private MultivaluedMap<String, String> getQueryParameters() {
            return transitionPropertiesBuilder.getQueryParameters();
        }

        private void addQueryParameters(MultivaluedMap<String, String> queryParameters) {
            this.transitionPropertiesBuilder.addQueryParameters(queryParameters);
        }

        private MultivaluedMap<String, String> getOutQueryParameters() {
            return outQueryParameters;
        }

        private void addOutQueryParameters(MultivaluedMap<String, String> outQueryParameters) {
            this.outQueryParameters.putAll(outQueryParameters);
        }

        private Map<String, Object> getCtxAttributes() {
            return ctxAttributes;
        }

        private void addCtxAttributes(Map<String, Object> ctxAttributes) {
            this.ctxAttributes.putAll(ctxAttributes);
        }

        private void addEntityProperties(RESTResource restResource) {
            this.transitionPropertiesBuilder.addRESTResource(restResource);
        }

        private void addTransitionProperties(Transition transition) {
            this.transitionPropertiesBuilder.addTransition(transition);
        }

        private RESTResource getRestResource() {
            return restResource;
        }

        private void setRestResource(RESTResource restResource) {
            this.restResource = restResource;
        }

        private void setSuccessful(Boolean isSuccessful) {
            this.isSuccessful = isSuccessful;
        }

        private void setInterimSuccessful(Boolean isSuccessful) {
            if (isInterim()) {
                setSuccessful(isSuccessful);
            }
        }

        private void addResult(Result result) {
            if (!isInterim()) {
                this.isSuccessful = result == null || result.equals(Result.SUCCESS);
            }
        }

        private boolean isInterim() {
            return delayedCommand != null;
        }

        private ResourceState getState() {
            return state;
        }

        private void setState(ResourceState state) {
            this.state = state;
        }

        private TransitionCommand getDelayedCommand() {
            return delayedCommand;
        }

        private void setCommand(TransitionCommand command) {
            this.command = command;
        }

        private void addCommand(TransitionCommand command) {
            if (command == null) {
                return;
            }
            if (command.isInterim()) {
                this.delayedCommand = command;
            } else {
                this.command = transitionWorkflowCommandBuilder.build(new TransitionCommand[] {this.command, command});
            }
        }

        private TransitionCommand buildCommand() {
            return transitionWorkflowCommandBuilder.build(state.getActions());
        }

        private void evaluate(InteractionContext ctx) {
            try {
                addResult((command == null) ? Result.SUCCESS : command.execute(ctx));
            } catch (InteractionException ie) {
                LOGGER.error("Transition command on state [{}] failed with error [{} - {}]: ",
                        state.getId(), ie.getHttpStatus(), ie.getHttpStatus().getReasonPhrase(), ie);
                addResult(Result.FAILURE);
            }
        }

        private void visitState(ResourceState state) throws ResourceStateRevisitedException {
            if (this.visitedStates.contains(state)) {
                throw new ResourceStateRevisitedException("Resource state "+state+" has been revisited");
            }
            this.visitedStates.add(state);
        }

        private Set<ResourceState> getVisitedStates() {
            return visitedStates;
        }

        private void setVisitedStates(Set<ResourceState> visitedStates) {
            this.visitedStates = visitedStates;
        }

        private Outcome getInterimOutcome() {
            return interimOutcome;
        }

        private void setInterimOutcome(Outcome interimOutcome) {
            this.interimOutcome = interimOutcome;
        }

        private void add(Outcome other) throws ResourceStateRevisitedException {
            if (other == null) {
                return;
            }
            if (other.isInterim()) {
                setInterimOutcome(other);
            } else {
                apply(other);
                setSuccessful(other.isSuccessful());
                visitState(other.getState());
            }
        }

        private void apply(Outcome other) {
            if (other == null) {
                return;
            }
            setTransitionPropertiesBuilder(other.getTransitionPropertiesBuilder());
            addOutQueryParameters(other.getOutQueryParameters());
            addCtxAttributes(other.getCtxAttributes());
            setRestResource(other.getRestResource());
            setState(other.getState());
            setCommand(other.getDelayedCommand());
            setVisitedStates(other.getVisitedStates());
            setInterimOutcome(other.getInterimOutcome());
        }

    }

}
