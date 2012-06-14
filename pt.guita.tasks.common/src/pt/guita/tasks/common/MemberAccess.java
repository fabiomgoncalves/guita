package pt.guita.tasks.common;

import java.io.Serializable;

public abstract class MemberAccess implements Serializable {

	private static final long serialVersionUID = 1L;

	public final String className;
	public final String memberName;
	public final int line;
	
	public MemberAccess(Class<?> type, String memberName, int line) {
		this.className = type.getName();
		this.memberName = memberName;
		this.line = line;
	}
	
	public String toString() {
		return className + "." + memberName;
	}
	
	@Override
	public final boolean equals(Object o) {		
		return 
		o instanceof MemberAccess &&
		((MemberAccess) o).className.equals(className) &&
		((MemberAccess) o).line == line;
	}
	
	@Override
	public final int hashCode() {
		return className.hashCode() + line;
	}
	
}
