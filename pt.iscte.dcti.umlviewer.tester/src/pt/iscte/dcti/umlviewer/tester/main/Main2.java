package pt.iscte.dcti.umlviewer.tester.main;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;

public class Main2 {
	
	//private Collection<Integer> col = new LinkedList<Integer>();
	//private String[] array = {"ola", "adeus"};
	private String str = "ola";
	
	public static void main(String[] args) {
		/*Field f = Main2.class.getDeclaredFields()[0];
		Type type = f.getGenericType();
		System.out.println(type.getClass().getName());
		if(ParameterizedType.class.isAssignableFrom(type.getClass())) {
			System.out.println("PARAMETERIZED");
		}*/
		Field f = Main2.class.getDeclaredFields()[0];
		System.out.println(f.getType().getComponentType().getName());
		//Type type = f.getGenericType();
		//System.out.println(Class.class.isAssignableFrom(type.getClass()));
		//System.out.println(((Class<?>)type).isArray());
		//System.out.println(ParameterizedType.class.isAssignableFrom(Class.class));
	}

}