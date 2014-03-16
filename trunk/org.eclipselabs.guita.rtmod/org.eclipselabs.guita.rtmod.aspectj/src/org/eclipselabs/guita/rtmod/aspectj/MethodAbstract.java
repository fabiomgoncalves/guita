package teste;

import java.util.LinkedList;

public class MethodAbstract {
	
	private String methodName;
	private LinkedList<String> parameterNames;
	private LinkedList<String> parameterTypes;
	
	public MethodAbstract(String methodName, LinkedList<String> parameterNames,	LinkedList<String> parameterTypes) {
		this.setMethodName(methodName);
		this.setParameterNames(parameterNames);
		this.setParameterTypes(parameterTypes);
	}
	
	public LinkedList<String> getParameterNames() {
		return parameterNames;
	}
	
	public void setParameterNames(LinkedList<String> parameterNames) {
		this.parameterNames = parameterNames;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public LinkedList<String> getParameterTypes() {
		return parameterTypes;
	}
	
	public void setParameterTypes(LinkedList<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	
	public String getFormatedParameters() {
		String returnString = "";
		for (int i = 0, j = parameterTypes.size(); i < j; i++) {
			returnString += parameterTypes.get(i) + " " + parameterNames.get(i);
					
			if (i + 1 != j) {
				returnString += ", ";
			}
		}
		
		return returnString;
	}
}