package pt.guita.tasks.common;

import java.lang.reflect.Field;

public class FieldAccess extends MemberAccess {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Type {
		READ, WRITE;
	}
	
	public final Type type;
	
	public FieldAccess(Field field, Type type, int line) {
		super(field.getDeclaringClass(), field.getName(), line);
		this.type = type;
	}

}
