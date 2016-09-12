/*
 * Our Xtext Java class generator
 */
package com.temenos.interaction.rimdsl.generator

import com.temenos.interaction.rimdsl.rim.State
import com.temenos.interaction.rimdsl.rim.TransitionRef
import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import java.util.HashSet
import java.util.Map
import java.util.Set
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator

class RIMDslGeneratorSwagger implements IGenerator {
	
	override void doGenerate(Resource resource, IFileSystemAccess fsa) {
	    if (resource == null) {
            throw new RuntimeException("Generator called with null resource");	        
	    }
	    var states = resource.allContents.toIterable.filter(typeof(State))
	    var initialState = states.findFirst[ isInitial ]
	    if (initialState != null) {
		    var interactionsByPath = new HashMap<String, Set<String>>()
		    collectInteractionsByPath(interactionsByPath, initialState)
		    var stateByMethodPath = new HashMap<String, State>()
		    collectStateByMethodPath(stateByMethodPath, initialState)
		    var resources = new ArrayList<String>()
		    for (String path : interactionsByPath.keySet) {
		    	if (path.startsWith("/")) {
		    		resources.add(path)
		    	}
		    }
		    fsa.generateFile("api-docs.json", toApiDocHeader() + toResourceListing(initialState, resources, interactionsByPath, stateByMethodPath).toString + toApiDocFooter())
	    }
	}
	
	def toApiDocHeader() '''
		{
		  "apiVersion": "0.2",
		  "swaggerVersion": "1.2",
	'''

	def toApiDocFooter() '''
		}
	'''
	
	def toResourceListing(State initialState, Iterable<String> paths, Map<String, Set<String>> interactionsByPath, Map<String, State> stateByMethodPath) '''
	    "resourcePath": "«initialState.path.name»",
	    "apis": [
        «FOR path : paths SEPARATOR ','»
          «IF path != null»
          « var interactions = interactionsByPath.get(path)»
    		{
    		"path": "«path»"«IF interactions != null»,
    		«toOperations(path, interactionsByPath, stateByMethodPath)»«ENDIF»
    		}
          «ENDIF»
        «ENDFOR»
	    ]
  '''

	def toOperations(String path, Map<String, Set<String>> interactionsByPath, Map<String, State> stateByMethodPath) '''
	    "operations": [
        «FOR method : interactionsByPath.get(path) SEPARATOR ','»
          « var state = stateByMethodPath.get(path + method)»
   		  {
   		  "method": "«method»",
   		  "nickname": "«IF state != null»«state.name»«ENDIF»",
   		  "summary": "«IF state != null»«state.description»«ENDIF»" 
   		  }
        «ENDFOR»
	    ]
  '''
  
	def void collectInteractionsByPath(Map<String, Set<String>> result, State initial) {
		var states = new ArrayList<State>();
		collectInteractionsByPath(result, states, initial);
	}
	
	def void collectInteractionsByPath(Map<String, Set<String>> result, Collection<State> states, State currentState) {
		if (currentState == null || states.contains(currentState)) return;
		states.add(currentState);
		// every state must have a 'GET' interaction
		var sPath = getPath(currentState)
		var interactions = result.get(sPath);
		if (interactions == null)
			interactions = new HashSet<String>();
		interactions.add("GET");
		result.put(sPath, interactions);
		
		// add interactions by iterating through the transitions from this state
		for (TransitionRef t : currentState.transitions) {
			if(t.state != null)
			{
				var path = getPath(t.state)
				interactions = result.get(path);
				if (interactions == null)
					interactions = new HashSet<String>();
				if (t.event.httpMethod != null)
					interactions.add(t.event.httpMethod);
					
				result.put(path, interactions);				
			}
			 
			collectInteractionsByPath(result, states, t.state);
		}		
	}
  
	def String getPath(State state) {
		var path = ""
		if (state.path == null) {
			path = state.name
		} else {
			path = state.path.name
		}
		return path;
	}

	def void collectStateByMethodPath(Map<String, State> result, State initial) {
		var states = new ArrayList<State>();
		collectStateByMethodPath(result, states, initial);
	}
	
	def void collectStateByMethodPath(Map<String, State> result, Collection<State> states, State currentState) {
		if (currentState == null || states.contains(currentState)) return;
		states.add(currentState);
		// every state must have a 'GET' interaction
		var sPath = getPath(currentState)
		result.put(sPath + "GET", currentState);
		// add interactions by iterating through the transitions from this state
		for (TransitionRef t : currentState.transitions) {
			if(t.state != null) {
				var path = getPath(t.state)
				
				if (t.event.httpMethod != null)
					result.put(path + t.event.httpMethod, t.state);
				collectStateByMethodPath(result, states, t.state);				
			}
		}		
	}
	
}

