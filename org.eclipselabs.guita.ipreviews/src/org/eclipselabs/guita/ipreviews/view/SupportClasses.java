package org.eclipselabs.guita.ipreviews.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Layout;

public class SupportClasses {
	
	public static Map<Class<?>, Class<?>> primitive_classes = new HashMap<Class<?>, Class<?>>();
	public static Map<String, Class<?>> primitive_classes_string = new HashMap<String, Class<?>>();
	public static Map<Class<?>, Class<?>> primitive_classes_inverse = new HashMap<Class<?>, Class<?>>();
	public static Map<Class<?>, Class<?>> primitive_array_to_object = new HashMap<Class<?>, Class<?>>();
	
	static {
		
		primitive_classes.put(byte.class, Byte.class);
		primitive_classes.put(short.class, Short.class);
		primitive_classes.put(int.class, Integer.class);
		primitive_classes.put(long.class, Long.class);
		primitive_classes.put(float.class, Float.class);
		primitive_classes.put(double.class, Double.class);
		primitive_classes.put(boolean.class, Boolean.class);
		primitive_classes.put(char.class, Character.class);
		primitive_classes.put(byte[].class, Byte[].class);
		primitive_classes.put(short[].class, Short[].class);
		primitive_classes.put(int[].class, Integer[].class);
		primitive_classes.put(long[].class, Long[].class);
		primitive_classes.put(float[].class, Float[].class);
		primitive_classes.put(double[].class, Double[].class);
		primitive_classes.put(boolean[].class, Boolean[].class);
		primitive_classes.put(char[].class, Character[].class);
		
		primitive_classes_inverse.put(Byte.class, byte.class);
		primitive_classes_inverse.put(Short.class, short.class);
		primitive_classes_inverse.put(Integer.class, int.class);
		primitive_classes_inverse.put(Long.class, long.class);
		primitive_classes_inverse.put(Float.class, float.class);
		primitive_classes_inverse.put(Double.class, double.class);
		primitive_classes_inverse.put(Boolean.class, boolean.class);
		primitive_classes_inverse.put(Character.class, char.class);
		primitive_classes_inverse.put(Byte[].class, byte[].class);
		primitive_classes_inverse.put(Short[].class, short[].class);
		primitive_classes_inverse.put(Integer[].class, int[].class);
		primitive_classes_inverse.put(Long[].class, long[].class);
		primitive_classes_inverse.put(Float[].class, float[].class);
		primitive_classes_inverse.put(Double[].class, double[].class);
		primitive_classes_inverse.put(Boolean[].class, boolean[].class);
		primitive_classes_inverse.put(Character.class, char.class);
		
		primitive_classes_string.put("byte", byte.class);
		primitive_classes_string.put("short", short.class);
		primitive_classes_string.put("int", int.class);
		primitive_classes_string.put("long", long.class);
		primitive_classes_string.put("float", float.class);
		primitive_classes_string.put("double", double.class);
		primitive_classes_string.put("boolean", boolean.class);
		primitive_classes_string.put("char", char.class);
		primitive_classes_string.put("byte[]", byte[].class);
		primitive_classes_string.put("short[]", short[].class);
		primitive_classes_string.put("int[]", int[].class);
		primitive_classes_string.put("long[]", long[].class);
		primitive_classes_string.put("float[]", float[].class);
		primitive_classes_string.put("double[]", double[].class);
		primitive_classes_string.put("boolean[]", boolean[].class);
		primitive_classes_string.put("char[]", char[].class);
		
		primitive_array_to_object.put(byte[].class, byte.class);
		primitive_array_to_object.put(short[].class, short.class);
		primitive_array_to_object.put(int[].class, int.class);
		primitive_array_to_object.put(long[].class, long.class);
		primitive_array_to_object.put(float[].class, float.class);
		primitive_array_to_object.put(double[].class, double.class);
		primitive_array_to_object.put(boolean[].class, boolean.class);
		primitive_array_to_object.put(char[].class, char.class);
	}
}
