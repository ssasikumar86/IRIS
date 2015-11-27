/*
 * Our Xtext Java class generator
 */
package com.temenos.interaction.rimdsl.generator

import com.temenos.interaction.rimdsl.rim.Expression
import com.temenos.interaction.rimdsl.rim.Function
import com.temenos.interaction.rimdsl.rim.ImplRef
import com.temenos.interaction.rimdsl.rim.NotFoundFunction
import com.temenos.interaction.rimdsl.rim.OKFunction
import com.temenos.interaction.rimdsl.rim.Relation
import com.temenos.interaction.rimdsl.rim.RelationConstant
import com.temenos.interaction.rimdsl.rim.ResourceCommand
import com.temenos.interaction.rimdsl.rim.ResourceInteractionModel
import com.temenos.interaction.rimdsl.rim.State
import com.temenos.interaction.rimdsl.rim.Transition
import com.temenos.interaction.rimdsl.rim.TransitionAuto
import com.temenos.interaction.rimdsl.rim.TransitionEmbedded
import com.temenos.interaction.rimdsl.rim.TransitionForEach
import com.temenos.interaction.rimdsl.rim.TransitionRedirect
import com.temenos.interaction.rimdsl.rim.TransitionRef
import com.temenos.interaction.rimdsl.rim.UriLink
import java.util.Iterator
import javax.inject.Inject
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.apache.commons.lang.StringEscapeUtils

class RIMDslGenerator implements IGenerator {
	
	@Inject extension IQualifiedNameProvider
	
	override void doGenerate(Resource resource, IFileSystemAccess fsa) {
	    if (resource == null) {
            throw new RuntimeException("Generator called with null resource");	        
	    }
        for (rim : resource.allContents.toIterable.filter(typeof(ResourceInteractionModel))) {
            generate(resource, rim, fsa);
        }
	}
		
	def void generate(Resource resource, ResourceInteractionModel rim, IFileSystemAccess fsa) {
        // generate Behaviour class
        var rimPath = rim.fullyQualifiedName.toString("/")
        fsa.generateFile(rimPath+"Behaviour.java", toJavaCode(rim))
        // generate resource classes
        for (resourceState : rim.states) {
            val statePath = resourceState.fullyQualifiedName.toString("/")
            fsa.generateFile(statePath+"ResourceState.java", toJavaCode(rim, resourceState))
        }
	}
	
	def className(Resource res) {
		var name = res.URI.lastSegment
		return name.substring(0, name.indexOf('.'))
	}
		
	def toJavaCode(ResourceInteractionModel rim, State state) '''
        package «rim.fullyQualifiedName»;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.Properties;
        import com.temenos.interaction.core.hypermedia.UriSpecification;
        import com.temenos.interaction.core.hypermedia.Action;
        import com.temenos.interaction.core.hypermedia.CollectionResourceState;
        import com.temenos.interaction.core.hypermedia.DynamicResourceState;        
        import com.temenos.interaction.core.hypermedia.LazyResourceLoader;        
        import com.temenos.interaction.core.hypermedia.ResourceFactory;
        import com.temenos.interaction.core.hypermedia.ResourceState;
        import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
        import com.temenos.interaction.core.hypermedia.Transition;
        import com.temenos.interaction.core.hypermedia.validation.HypermediaValidator;
        import com.temenos.interaction.core.hypermedia.expression.Expression;
        import com.temenos.interaction.core.hypermedia.expression.ResourceGETExpression;
        import com.temenos.interaction.core.hypermedia.expression.SimpleLogicalExpressionEvaluator;
        import com.temenos.interaction.core.hypermedia.ResourceLocator;        
        import com.temenos.interaction.core.hypermedia.ResourceLocatorProvider;
        
        public class «state.name»ResourceState extends «IF state.type.isCollection»Collection«ENDIF»ResourceState implements LazyResourceLoader {
            
            private ResourceFactory factory = null;

            public «state.name»ResourceState() {
                this(new ResourceFactory());
            }

            public «state.name»ResourceState(ResourceFactory factory) {
                «IF state.type.isCollection»
                super("«state.entity.name»", "«state.name»", createActions(), "«producePath(rim, state)»", createLinkRelations(), null, «if (state.errorState != null) { "factory.getResourceState(\"" + state.errorState.fullyQualifiedName + "\")" } else { "null" }»);
                «ELSEIF state.type.isItem»
                super("«state.entity.name»", "«state.name»", createActions(), "«producePath(rim, state)»", createLinkRelations(), «if (state.path != null) { "new UriSpecification(\"" + state.name + "\", \"" + producePath(rim, state) + "\")" } else { "null" }», «if (state.errorState != null) { "factory.getResourceState(\"" + state.errorState.fullyQualifiedName + "\")" } else { "null" }»);
                «ENDIF»
                «IF state.isInitial»
                setInitial(true);
                «ENDIF»
                «IF state.isException»
                setException(true);
                «ENDIF»
                this.factory = factory;
            }
            
            public boolean initialise() {
                Map<String, String> uriLinkageProperties = new HashMap<String, String>();
                List<Expression> conditionalLinkExpressions = null;
                «IF state.type.isCollection»Collection«ENDIF»ResourceState «stateVariableName(state)» = this;
                «
                val resources = newArrayList()
                »
                «IF !resources.contains(state.name) && resources.add(state.name)»«ENDIF»
                // create transitions
                «FOR t : state.transitions»
                    «IF ((t.state != null && t.state.name != null) || t.name != null) && !resources.contains(transitionTargetStateVariableName(t)) && resources.add(transitionTargetStateVariableName(t))»
                    ResourceState «transitionTargetStateVariableName(t)» = factory.getResourceState("«if (t.state != null && t.state.name != null) {t.state.fullyQualifiedName} else {t.name}»");
                    «ENDIF»
                    «IF (t.state != null && t.state.name != null) || t.name != null || t.locator != null»
                        «IF(transitionProcessingRequired(t))»                				               	
                            «IF t instanceof Transition»
                            // create regular transition
                            «produceTransitions(state, t as Transition)»
                            «ENDIF»

                            «IF t instanceof TransitionForEach»
                            // create foreach transition
                            «produceTransitionsForEach(state, t as TransitionForEach)»
                            «ENDIF»

                            «IF t instanceof TransitionAuto»
                            // create AUTO transition
                            «produceTransitionsAuto(state, t as TransitionAuto)»
                            «ENDIF»

                            «IF t instanceof TransitionRedirect»
                            // create REDIRECT transition
                            «produceTransitionsRedirect(state, t as TransitionRedirect)»
                            «ENDIF»

                            «IF t instanceof TransitionEmbedded»
                            // create EMBEDDED transition
                            «produceTransitionsEmbedded(state, t as TransitionEmbedded)»
                            «ENDIF»
                        «ENDIF»
                    «ENDIF»
                «ENDFOR»
                return true;
            }
            
            private static List<Action> createActions() {
                Properties actionViewProperties = null;
                «produceActionSet(state, state.impl)»
                return «state.name»Actions;
            }

            private static String[] createLinkRelations() {
                «produceRelations(state)»
                return «state.name»Relations;
            }
            
        }
	'''
		
	def toJavaCode(ResourceInteractionModel rim) '''
		«IF rim.eContainer.fullyQualifiedName != null»
		package «rim.eContainer.fullyQualifiedName»;
		«ENDIF»

		import java.util.ArrayList;
		import java.util.HashMap;
		import java.util.List;
		import java.util.Map;
		import java.util.Properties;

		import com.temenos.interaction.core.hypermedia.UriSpecification;
		import com.temenos.interaction.core.hypermedia.Action;
		import com.temenos.interaction.core.hypermedia.CollectionResourceState;
		import com.temenos.interaction.core.hypermedia.ResourceFactory;
		import com.temenos.interaction.core.hypermedia.ResourceLocatorProvider;
		import com.temenos.interaction.core.hypermedia.ResourceState;
		import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
		import com.temenos.interaction.core.hypermedia.validation.HypermediaValidator;
		import com.temenos.interaction.core.hypermedia.expression.Expression;
		import com.temenos.interaction.core.hypermedia.expression.ResourceGETExpression;
		import com.temenos.interaction.core.resource.ResourceMetadataManager;
		
		public class «rim.name»Behaviour {
			private ResourceLocatorProvider locatorProvider;
		
		    public static void main(String[] args) {
		        «rim.name»Behaviour behaviour = new «rim.name»Behaviour();
		        ResourceStateMachine hypermediaEngine = new ResourceStateMachine(behaviour.getRIM(), behaviour.getExceptionResource());
		        HypermediaValidator validator = HypermediaValidator.createValidator(hypermediaEngine, new ResourceMetadataManager(hypermediaEngine).getMetadata());
		        System.out.println(validator.graph());
		    }
		
		    public ResourceState getRIM() {
		        Map<String, String> uriLinkageProperties = new HashMap<String, String>();
		        List<Expression> conditionalLinkExpressions = null;
		        Properties actionViewProperties;

		        ResourceFactory factory = new ResourceFactory();
		        factory.setResourceLocatorProvider(locatorProvider);
		        ResourceState initial = null;
		        // create states
		        «FOR c : rim.states»
		        	«IF c.isInitial»
		        	// identify the initial state
		        	initial = factory.getResourceState("«rim.fullyQualifiedName».«c.name»");
					«ENDIF»
				«ENDFOR»
		        return initial;
		    }

		    public ResourceState getExceptionResource() {
		        ResourceFactory factory = new ResourceFactory();
		        ResourceState exceptionState = null;
		        «FOR c : rim.states»
		        	«IF c.isException»
		        	exceptionState = factory.getResourceState("«rim.fullyQualifiedName».«c.name»");
					«ENDIF»
				«ENDFOR»
		        return exceptionState;
		    }
		    
		    public void setResourceLocatorProvider(ResourceLocatorProvider locatorProvider) {
		    	this.locatorProvider = locatorProvider;
		    }
		    
		    public ResourceLocatorProvider getResourceLocatorProvider() {
		    	return locatorProvider;
		    }		    
		}
	'''
	
	def produceResourceStates(ResourceInteractionModel rim, State state) '''
            «produceActionSet(state, state.impl)»
            «produceRelations(state)»
            «IF state.type.isCollection»
            CollectionResourceState «stateVariableName(state)» = new CollectionResourceState("«state.entity.name»", "«state.name»", «state.name»Actions, "«producePath(rim, state)»", «state.name»Relations, null);
            «ELSEIF state.type.isItem»
            ResourceState «stateVariableName(state)» = new ResourceState("«state.entity.name»", "«state.name»", «state.name»Actions, "«producePath(rim, state)»", «stateVariableName(state)»Relations«if (state.path != null) { ", new UriSpecification(\"" + state.name + "\", \"" + producePath(rim, state) + "\")" }»);
            «ENDIF»
            «IF state.isException»
            «stateVariableName(state)».setException(true);
            «ENDIF»
	'''

    def producePath(ResourceInteractionModel rim, State state) '''«
    	// prepend the basepath
	    if (rim.basepath != null) {
		    if (state.path != null) { rim.basepath.name + state.path.name } else { rim.basepath.name + "/" + state.name }
		} else {
		    if (state.path != null) { state.path.name } else { "/" + state.name }
		}
    »'''

    def produceRelations(State state) '''
        «IF state.relations != null && state.relations.size > 0»
        String «state.name»RelationsStr = "";
        «FOR relation : state.relations»
        «IF relation instanceof RelationConstant»
        «state.name»RelationsStr += "«(relation as RelationConstant).name» ";
        «ELSE»
        «state.name»RelationsStr += "«(relation.relation as Relation).fqn» ";
        «ENDIF»
        «ENDFOR»
        String[] «state.name»Relations = «state.name»RelationsStr.trim().split(" ");
        «ELSE»
        String[] «state.name»Relations = null;
        «ENDIF»
    '''

    def produceActionSet(State state, ImplRef impl) {
    	if (impl != null) {
   			produceActionSet(state, impl.view, impl.actions);
    	}
    }

    def produceActionSet(State state, ResourceCommand view, EList<ResourceCommand> actions) '''
        List<Action> «state.name»Actions = new ArrayList<Action>();
        «IF view != null && ((view.command.spec != null && view.command.spec.properties.size > 0) || view.properties.size > 0)»
            actionViewProperties = new Properties();
            «IF view.command.spec != null && view.command.spec.properties.size > 0»
            «FOR commandProperty :view.command.spec.properties»
                actionViewProperties.put("«commandProperty.name»", "«commandProperty.value»");
	        	«ENDFOR»
	        «ENDIF»
            «FOR commandProperty :view.properties»
            actionViewProperties.put("«commandProperty.name»", "«commandProperty.value»");
            «ENDFOR»
        «ENDIF»
        «IF view != null»
        «state.name»Actions.add(new Action("«view.command.name»", Action.TYPE.VIEW, «if (view != null && ((view.command.spec != null && view.command.spec.properties.size > 0) || view.properties.size > 0)) { "actionViewProperties" } else { "new Properties()" }»));
        «ENDIF»
        «IF actions != null»
            «FOR action : actions»
            actionViewProperties = new Properties();
            «IF action != null && ((action.command.spec != null && action.command.spec.properties.size > 0) || action.properties.size > 0)»
                «IF action.command.spec != null && action.command.spec.properties.size > 0»
                    «FOR commandProperty :action.command.spec.properties»
                    actionViewProperties.put("«commandProperty.name»", "«commandProperty.value»");
                    «ENDFOR»
		        «ENDIF»
                «FOR commandProperty :action.properties»
                actionViewProperties.put("«commandProperty.name»", "«commandProperty.value»");
                «ENDFOR»
            «ENDIF»
            «state.name»Actions.add(new Action("«action.command.name»", Action.TYPE.ENTRY, actionViewProperties));
            «ENDFOR»
        «ENDIF»'''
    
    def produceExpressions(Expression conditionExpression) '''
        «IF conditionExpression != null»
        conditionalLinkExpressions = new ArrayList<Expression>();
        «FOR function : conditionExpression.expressions»
            conditionalLinkExpressions.add(«produceExpression(function)»);
        «ENDFOR»
        «ENDIF»
    '''

    def produceExpression(Function expression) '''
        «IF expression instanceof OKFunction»
            new ResourceGETExpression(factory.getResourceState("«(expression as OKFunction).state.fullyQualifiedName»"), ResourceGETExpression.Function.OK)«
        ELSE»
            new ResourceGETExpression(factory.getResourceState("«(expression as NotFoundFunction).state.fullyQualifiedName»"), ResourceGETExpression.Function.NOT_FOUND)«
        ENDIF»'''
	
	def produceTransitions(State fromState, Transition transition) '''
            conditionalLinkExpressions = null;
            uriLinkageProperties.clear();
        «IF transition.spec != null»
            «produceUriLinkage(transition.spec.uriLinks)»
            «produceExpressions(transition.spec.eval)»
        «ENDIF»
            «stateVariableName(fromState)».addTransition(new Transition.Builder()
                    .method("«transition.event.httpMethod»")
                    .target(«findTargetState(fromState, transition)»)
                    .uriParameters(uriLinkageProperties)
                    .evaluation(conditionalLinkExpressions != null ? new SimpleLogicalExpressionEvaluator(conditionalLinkExpressions) : null)
                    .label("«getTransitionLabel(transition)»")
                    .build());
	'''
	
    def produceTransitionsForEach(State fromState, TransitionForEach transition) '''    		
            conditionalLinkExpressions = null;
            uriLinkageProperties.clear();
        «IF transition.spec != null»
            «produceUriLinkage(transition.spec.uriLinks)»
            «produceExpressions(transition.spec.eval)»
        «ENDIF»
            «stateVariableName(fromState)».addTransition(new Transition.Builder()
                    .flags(Transition.FOR_EACH)
                    .method("«transition.event.httpMethod»")
                    .target(«findTargetState(fromState, transition)»)
                    .uriParameters(uriLinkageProperties)
                    .evaluation(conditionalLinkExpressions != null ? new SimpleLogicalExpressionEvaluator(conditionalLinkExpressions) : null)
                    .label("«getTransitionLabel(transition)»")
                    .linkId("«getTransitionLinkId(transition)»")
                    .build());
    '''
    		
    def produceTransitionsAuto(State fromState, TransitionAuto transition) '''
            conditionalLinkExpressions = null;
            uriLinkageProperties.clear();
        «IF transition.spec != null»
            «produceUriLinkage(transition.spec.uriLinks)»
            «produceExpressions(transition.spec.eval)»
        «ENDIF»
            «stateVariableName(fromState)».addTransition(new Transition.Builder()
                    .flags(Transition.AUTO)
                    .target(«findTargetState(fromState, transition)»)
                    .uriParameters(uriLinkageProperties)
                    .evaluation(conditionalLinkExpressions != null ? new SimpleLogicalExpressionEvaluator(conditionalLinkExpressions) : null)
                    .build());
    '''

    def produceTransitionsRedirect(State fromState, TransitionRedirect transition) '''
            conditionalLinkExpressions = null;
            uriLinkageProperties.clear();
        «IF transition.spec != null»
            «produceUriLinkage(transition.spec.uriLinks)»
            «produceExpressions(transition.spec.eval)»
        «ENDIF»
            «stateVariableName(fromState)».addTransition(new Transition.Builder()
                    .flags(Transition.REDIRECT)
                    .target(«findTargetState(fromState, transition)»)
                    .uriParameters(uriLinkageProperties)
                    .evaluation(conditionalLinkExpressions != null ? new SimpleLogicalExpressionEvaluator(conditionalLinkExpressions) : null)
                    .build());
    '''

    def produceTransitionsEmbedded(State fromState, TransitionEmbedded transition) '''
            conditionalLinkExpressions = null;
            uriLinkageProperties.clear();
        «IF transition.spec != null»
            «produceUriLinkage(transition.spec.uriLinks)»
            «produceExpressions(transition.spec.eval)»
        «ENDIF»
            «stateVariableName(fromState)».addTransition(new Transition.Builder()
                    .flags(Transition.EMBEDDED)
                    .method("«transition.event.httpMethod»")
                    .target(«findTargetState(fromState, transition)»)
                    .uriParameters(uriLinkageProperties)
                    .evaluation(conditionalLinkExpressions != null ? new SimpleLogicalExpressionEvaluator(conditionalLinkExpressions) : null)
                    .label(«if (transition.spec != null && transition.spec.title != null) { "\"" + transition.spec.title.name + "\"" } else { "\"" + transition.state.name + "\"" }»)
                    .build());
    '''

    def produceUriLinkage(EList<UriLink> uriLinks) '''
        «IF uriLinks != null»
            «FOR prop : uriLinks»
            uriLinkageProperties.put("«prop.templateProperty»", "«prop.entityProperty.name»");
            «ENDFOR»«
        ENDIF»
    '''

	def String transitionTargetStateVariableName(TransitionRef t) {
		if (t.state != null) {
			return stateVariableName(t.state);
		} else {
       		return "s" + (t.name).replaceAll("\\.", "_");
		}
	}

	def String stateVariableName(State state) {
		if (state != null && state.name != null) {
			return "s" + (state.name).replaceAll("\\.", "_");
		}
		return null;
	}
	
	def transitionProcessingRequired(TransitionRef transition) {
		if(transition.locator == null) {
			return transitionTargetStateVariableName(transition) != null	
		} else {
			return true;			
		}
	}
	
	def private static escapeXml(String input) {
	    return  StringEscapeUtils.escapeXml(input)
	}
	
    def static getTransitionLabel(TransitionRef transition) {
        val unescapedLabel = if (transition.spec != null && transition.spec.title != null) { transition.spec.title.name } else { if (transition.state != null) { transition.state.name } else { transition.name } }
        
    	return escapeXml(unescapedLabel)
    }
    
    def static getTransitionLinkId(TransitionRef transition) {
    	return if (transition.spec != null && transition.spec.id != null && transition.spec.id.name.length() > 0) { transition.spec.id.name } else { null }
    }
    
    def findTargetState(State fromState, TransitionRef transition) {
    	if(transition.locator == null) {
    		return transitionTargetStateVariableName(transition)
    	} else {
    		return "new DynamicResourceState(\"" + fromState.entity.name + "\", \"dynamic\", \"" + transition.locator.name + "\", " + toStringArray(transition.locator.args) + ")"
    	}
    }
    
    def toStringArray(EList<String> array) {
    	var result = "new String[] {";
    
    	val Iterator<String> iter = array.iterator
    	
    	while(iter.hasNext) {
    		result = result + "\"" + iter.next + "\""
    		
    		if(iter.hasNext) {
    			result = result + ", "
    		}
    	}
    	
    	result = result + "}";
    }        
}

