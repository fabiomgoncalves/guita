package pt.guita.tasks.common;

import java.lang.reflect.Method;

public class AnonymousClassMethodAccess extends MethodAccess {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//	public final String topLevelClass;
	public final int[] anonymousLevels;
	
	public AnonymousClassMethodAccess(Method method, Class<?> clazz, int serialNumber, int line) {
		super(method, getTopLevel(clazz), serialNumber, line);
//		topLevelClass = getTopLevel(clazz).getName();
		anonymousLevels = new int[levels(clazz)];
		fillLevels(clazz, anonymousLevels, 0);
	}

	private static Class<?> getTopLevel(Class<?> clazz) {
		if(!clazz.isAnonymousClass())
			return clazz;
		else
			return getTopLevel(clazz.getEnclosingClass());		
	}
	
	private static int levels(Class<?> clazz) {
		if(!clazz.isAnonymousClass())
			return 0;
		else
			return 1 + levels(clazz.getEnclosingClass());
	}
	
	private static void fillLevels(Class<?> clazz, int[] levels, int index) {
		if(index == levels.length)
			return;
		else {
			
			String tmp = clazz.getName().substring(clazz.getName().lastIndexOf('$')+1);
//			System.out.println("! " + clazz.getName() + " ; " + tmp);
			levels[index] = Integer.parseInt(tmp);
			fillLevels(clazz.getEnclosingClass(), levels, index + 1);
		}
	}
	

	
	
	
}
