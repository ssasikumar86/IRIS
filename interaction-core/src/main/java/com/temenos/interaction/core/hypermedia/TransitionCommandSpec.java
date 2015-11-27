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


import java.util.HashMap;
import java.util.Map;

import com.temenos.interaction.core.hypermedia.expression.Expression;

/**
 * Define how a transition from one state to another should occur.
 * @author aphethean
 */
public class TransitionCommandSpec {

	private final String method;
	private final int flags;
	// conditional link evaluation expression 
	private final Expression evaluation;
	private final Map<String, String> uriParameters;
	private final String linkId;
	
	protected TransitionCommandSpec(String method) {
		this(method, 0);
	}

	protected TransitionCommandSpec(String method, int flags) {
		this(method, flags, null);
	}
	
	protected TransitionCommandSpec(String method, int flags, Expression evaluation) {
		this(method, flags, evaluation, null, null);
	}

	protected TransitionCommandSpec(String method, int flags, Expression evaluation, Map<String, String> uriParameters, String linkId) {
		this.method = method;
		this.flags = flags;
		this.evaluation = evaluation;
		this.uriParameters = uriParameters != null ? new HashMap<String, String>(uriParameters) : null;
		this.linkId = linkId;
	}
	
	public int getFlags() {
		return flags;
	}

	public String getMethod() {
		return method;
	}

	public Expression getEvaluation() {
		return evaluation;
	}

	public Map<String, String> getUriParameters() {
		return uriParameters;
	}
	
	public String getLinkId() {
		return linkId;
	}

	/**
	 * Is this transition command to be applied to each item in a collection?
	 * @return
	 */
	public boolean isForEach() {
		return ((flags & Transition.FOR_EACH) == Transition.FOR_EACH);
	}
	
    /**
     * Is this embedded transition command to be applied to each item in a collection?
     * @return
     */
    public boolean isEmbeddedForEach() {
        return ((flags & Transition.FOR_EACH_EMBEDDED) == Transition.FOR_EACH_EMBEDDED);
    }	
	
	/**
	 * Is this transition an auto transition?
	 * @return
	 */
	public boolean isAutoTransition() {
		return ((flags & Transition.AUTO) == Transition.AUTO);
	}

	/**
	 * Is this transition a redirect transition?
	 * @return
	 */
	public boolean isRedirectTransition() {
		return ((flags & Transition.REDIRECT) == Transition.REDIRECT);
	}

	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof TransitionCommandSpec)) return false;
		TransitionCommandSpec otherObj = (TransitionCommandSpec) other;
		return this.getFlags() == otherObj.getFlags() &&
				((this.getMethod() == null && otherObj.getMethod() == null) || (this.getMethod() != null && this.getMethod().equals(otherObj.getMethod())) &&
				((this.getUriParameters() == null && otherObj.getUriParameters() == null) || (this.getUriParameters() != null && this.getUriParameters().equals(otherObj.getUriParameters())))) &&
				((this.linkId == null && otherObj.linkId == null) || (this.linkId != null && this.getLinkId().equals(otherObj.getLinkId())));
	}
	
	public int hashCode() {
		return this.flags 
				+ (this.method != null ? this.method.hashCode() : 0)
				+ (this.uriParameters != null ? this.uriParameters.hashCode() : 0)
				+ (this.linkId != null ? this.linkId.hashCode() : 0);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (isForEach())
			sb.append("*");
		if (isAutoTransition()) {
			sb.append("AUTO");
		} else {
			sb.append(method);
		}
		if (evaluation != null) {
			sb.append(" (");
			sb.append(evaluation.toString());
			sb.append(")");
		}
		if (uriParameters != null && uriParameters.size() > 0) {
			sb.append(" ");
			for(String key : uriParameters.keySet()) {
				String value = uriParameters.get(key);
				sb.append(key + "=" + value);
			}
		}
		if(linkId != null && linkId.length() > 0 ) {
			sb.append(" ");
			sb.append("linkId=" + linkId);
		}
		return sb.toString();
	}
}
