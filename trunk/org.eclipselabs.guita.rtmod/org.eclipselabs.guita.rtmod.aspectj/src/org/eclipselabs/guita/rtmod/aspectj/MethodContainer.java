package org.eclipselabs.guita.rtmod.aspectj;

import java.util.LinkedList;

public class MethodContainer {	
	private String name;
	private LinkedList<ParameterContainer> parameters;
	
	public MethodContainer(String name) {
		this.name = name;
		this.parameters = new LinkedList<ParameterContainer>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkedList<ParameterContainer> getParameters() {
		return parameters;
	}

	public void setParameters(LinkedList<ParameterContainer> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(String name, String type) {
		this.parameters.add(new ParameterContainer(name, type));
	}
	
	public String getMethodExpression() {
		String expression = this.name + "(";		
		
		for(int i = 0, j = parameters.size(); i < j; i++) {			
			expression += parameters.get(i).getType() + " " + parameters.get(i).getName();

			if (i + 1 < j) {
				expression += ", ";
			} else {
				expression += ")";
			}
		}
		
		return expression;
	}
}