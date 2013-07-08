package org.eclipselabs.guita.codetrace.common;

import java.io.Serializable;

public class VariableInfo implements Serializable {
	private final String type;
	private final int order;
	
	private final int totalVars;
	private final int totalVarsOfType;
	private final int totalSameName;
	
	public VariableInfo(String type, int order, int totalVars, int totalVarsOfType, int totalSameName) {
		this.type = type;
		this.order = order;
		this.totalVars = totalVars;
		this.totalVarsOfType = totalVarsOfType;
		this.totalSameName = totalSameName;
	}

	public String getType() {
		return type;
	}
	
	public int getLineExecutionOrder() {
		return order;
	}
	
	public int getTotalVars() {
		return totalVars;
	}
	
	public int getNumberOfVarsWithSameType() {
		return totalVarsOfType;
	}

	public int getNumberOfVarsWithSameName() {
		return totalSameName;
	}
	
	
	public String toString() {
		return 
				"type: " + getType() + "\n" + 
				"order: " + getLineExecutionOrder() + "\n" + 
				"totalVars: " + getTotalVars() + "\n" +
				"varsSameType: " + getNumberOfVarsWithSameType() + "\n" +
				"varsSameName: " + getNumberOfVarsWithSameName() + "\n";
	}
	
	
}
