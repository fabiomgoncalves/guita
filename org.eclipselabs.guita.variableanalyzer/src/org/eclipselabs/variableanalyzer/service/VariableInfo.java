package org.eclipselabs.variableanalyzer.service;

public class VariableInfo {
	private final String type;
	private final int order;
	
	public VariableInfo(String type, int order) {
		this.type = type;
		this.order = order;
	}

	public String getType() {
		return type;
	}
	
	public int getLineExecutionOrder() {
		return order;
	}
	
	
}
