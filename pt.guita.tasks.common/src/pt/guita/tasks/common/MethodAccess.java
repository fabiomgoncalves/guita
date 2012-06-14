package pt.guita.tasks.common;

import java.lang.reflect.Method;

public class MethodAccess extends MemberAccess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final String paramTypes;
	public final int serialNumber;
	
	public MethodAccess(Method method, Class<?> clazz, int serialNumber, int line) {
		super(clazz, method.getName(), line);		
		paramTypes = params(method);
		this.serialNumber = serialNumber;
	}
	
	private static String params(Method method) {
		StringBuilder sig = new StringBuilder();
		
		for(Class<?> c : method.getParameterTypes()) {
			if(sig.length() != 0)
				sig.append(",");
			
			sig.append(c.getName());
		}
		
		return sig.toString();
	}
	
	public boolean isFirst() {
		return serialNumber == 1;
	}
	
	@Override
	public String toString() {		
		return super.toString() + "(" + paramTypes + ") " + "[" + serialNumber + "]";
	}
	
}
