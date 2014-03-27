package org.eclipselabs.guita.ipreviews.view;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;
import org.eclipselabs.guita.ipreviews.handler.ConstantStatementVisitor;
import org.eclipselabs.guita.ipreviews.handler.MethodInvocationVisitor;
import org.eclipselabs.guita.ipreviews.handler.NewStatementVisitor;
import org.eclipselabs.guita.ipreviews.handler.VariableDeclarationStatementVisitor;
import org.eclipselabs.guita.ipreviews.regex.Regex;

public class PreviewView extends ViewPart {

	private final Map<Class<?>, Class<?>> equivalent_classes = new HashMap<Class<?>, Class<?>>();
	private final Map<Class<?>, Class<?>> primitive_classes = new HashMap<Class<?>, Class<?>>();
	private final Map<Class<?>, Class<?>> primitive_classes_inverse = new HashMap<Class<?>, Class<?>>();
	
	private HashMap<String, Composite> composite_classes = new HashMap<String, Composite>();
	private HashMap<String, Widget> widgets = new HashMap<String, Widget>();
	private	HashMap<String, Class<?>> widgets_classes = new HashMap<String, Class<?>>();
	
	private List<String> shells = new ArrayList<String>();

	public static final String ID = "guidesign.previewview";

	Composite composite;

	public PreviewView(){
		equivalent_classes.put(FillLayout.class, Layout.class);
		equivalent_classes.put(GridLayout.class, Layout.class);
		equivalent_classes.put(RowLayout.class, Layout.class);
		primitive_classes.put(byte.class, Byte.class);
		primitive_classes.put(short.class, Short.class);
		primitive_classes.put(int.class, Integer.class);
		primitive_classes.put(long.class, Long.class);
		primitive_classes.put(float.class, Float.class);
		primitive_classes.put(double.class, Double.class);
		primitive_classes.put(boolean.class, Boolean.class);
		primitive_classes.put(char.class, Character.class);
		primitive_classes_inverse.put(Byte.class, byte.class);
		primitive_classes_inverse.put(Short.class, short.class);
		primitive_classes_inverse.put(Integer.class, int.class);
		primitive_classes_inverse.put(Long.class, long.class);
		primitive_classes_inverse.put(Float.class, float.class);
		primitive_classes_inverse.put(Double.class, double.class);
		primitive_classes_inverse.put(Boolean.class, boolean.class);
		primitive_classes_inverse.put(Character.class, char.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void constructPreview(List<VariableDeclarationStatement> declarations, List<MethodInvocation> methods) {

		resolveDeclarations(declarations);

		for(MethodInvocation mi : methods){
			String objectName = mi.toString().substring(0, mi.toString().indexOf("."));
			String fullMethod = mi.toString().substring(mi.toString().indexOf(".") + 1);
			String methodName = fullMethod.substring(0, fullMethod.indexOf("("));
			if(widgets_classes.containsKey(objectName)){
				resolveMethod(mi, objectName, methodName, true);
			}
			else if(composite_classes.containsKey(objectName)){
				resolveMethod(mi, objectName, methodName, false);
			}
		}


		System.out.println("COMPOSITES: " + composite_classes);
		System.out.println("WIDGETS: " + widgets);
		System.out.println("WIDGETS_CLASSES: " + widgets_classes);

	}

	private void resolveDeclarations(
			List<VariableDeclarationStatement> declarations) {
		for(VariableDeclarationStatement vd : declarations){
			VariableDeclarationStatementVisitor variable_visitor = new VariableDeclarationStatementVisitor();
			vd.accept(variable_visitor);
			try {
				Class <?> variable_class = resolveClass(vd.getType().resolveBinding().getQualifiedName());
				ClassInstanceCreation node = variable_visitor.getNode();
				if(node.arguments().size() == 1 && variable_class.equals(Shell.class)){
					composite_classes.put(getNameDeclaration(vd), composite);
					shells.add(getNameDeclaration(vd));
				}
				else {
					Object int_aux = null;
					if(((Expression)node.arguments().get(1)).toString().matches(Regex.constantDeclarations)){
						ConstantStatementVisitor c_visitor = new ConstantStatementVisitor();
						((Expression)node.arguments().get(1)).accept(c_visitor);
						int_aux = c_visitor.getObjects_map().get(((Expression)node.arguments().get(1)).toString());
					}
					else int_aux = getConstructor(Integer.class, String.class).newInstance(((Expression)node.arguments().get(1)).toString());
					if(variable_class.isAssignableFrom(Composite.class)){
						Composite aux_composite;
						Constructor<?> constr = getConstructor(variable_class, Composite.class, int.class);
						if(composite_classes.containsKey(node.arguments().get(0).toString())){
							Composite aux = composite_classes.get(node.arguments().get(0).toString());
							aux_composite = (Composite) constr.newInstance(aux, int_aux);
						}
						else aux_composite = (Composite) constr.newInstance(composite, int_aux);
						composite_classes.put(getNameDeclaration(vd), aux_composite);
					}
					else {
						Constructor<?> constr = getConstructor(variable_class, Composite.class, int.class);
						Widget widget;
						if(composite_classes.containsKey(node.arguments().get(0).toString())){
							Composite aux = composite_classes.get(node.arguments().get(0).toString());
							widget = (Widget) constr.newInstance(aux, int_aux);
						}
						else widget = (Widget) constr.newInstance(composite, int_aux);
						widgets.put(getNameDeclaration(vd), widget);
						widgets_classes.put(getNameDeclaration(vd), variable_class);
					}
				}
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private void resolveMethod(MethodInvocation mi, String objectName, String methodName, boolean isMethod) {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		for(int i = 0; i != mi.arguments().size(); i++){
			Object o = mi.arguments().get(i);
			class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
		}
		Class<?> methodClass;
		if(isMethod)
			methodClass = widgets_classes.get(objectName);
		else methodClass = Composite.class;
		try {
			Class<?> [] class_args2 = class_args.clone();
			for(int i = 0; i != class_args2.length; i++) {
				if(equivalent_classes.containsKey(class_args2[i]))
					class_args2[i] = equivalent_classes.get(class_args2[i]);
			}
			Method m = methodClass.getMethod(methodName, class_args2);
			Object[] objects = new Object[mi.arguments().size()];
			for(int i = 0; i != class_args.length; i++){
				Expression exp = (Expression)mi.arguments().get(i);
				objects[i] = resolveType(class_args, exp, i); 
			}
			if(isMethod) {
				m.invoke(widgets.get(objectName), objects);
			}
			else {
				m.invoke(composite_classes.get(objectName), objects);
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException f){
			if(!shells.contains(objectName))
				f.printStackTrace();
		}
	}
	
	private Object getObjectMethod(MethodInvocation mi, String objectName, String methodName, boolean isMethod) {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		for(int i = 0; i != mi.arguments().size(); i++){
			Object o = mi.arguments().get(i);
			class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
		}
		Class<?> methodClass;
		if(isMethod)
			methodClass = widgets_classes.get(objectName);
		else methodClass = Composite.class;
		try {
			Class<?> [] class_args2 = class_args.clone();
			for(int i = 0; i != class_args2.length; i++) {
				if(equivalent_classes.containsKey(class_args2[i]))
					class_args2[i] = equivalent_classes.get(class_args2[i]);
			}
			Method m = methodClass.getMethod(methodName, class_args2);
			Object[] objects = new Object[mi.arguments().size()];
			for(int i = 0; i != class_args.length; i++){
				Expression exp = (Expression)mi.arguments().get(i);
				objects[i] = resolveType(class_args, exp, i); 
			}
			if(isMethod) {
				return m.invoke(widgets.get(objectName), objects);
			}
			else {
				return m.invoke(composite_classes.get(objectName), objects);
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException f){
			if(!shells.contains(objectName))
				f.printStackTrace();
			return null;
		}
	}

	private Object resolveType(Class<?>[] class_object_args, Expression argument_expression, int j)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if(argument_expression.toString().matches(Regex.newArguments)){
			return constructObject(argument_expression, class_object_args[j]);
		}
		else {
			if(argument_expression.toString().matches(Regex.constantDeclarations)){
				ConstantStatementVisitor constant_visitor = new ConstantStatementVisitor();
				argument_expression.accept(constant_visitor);
				if(constant_visitor.getObjects_map().containsKey(argument_expression.toString())){
					return constant_visitor.getObjects_map().get(argument_expression.toString());
				}
				//em principio não deve entrar aqui
				else return null;
			}
			else {
				if(argument_expression.toString().matches(Regex.methodDeclarations)){
					MethodInvocationVisitor method_visitor = new MethodInvocationVisitor();
					argument_expression.accept(method_visitor);
					MethodInvocation node = method_visitor.getNode();
					String objectName = node.toString().substring(0, node.toString().indexOf("."));
					String fullMethod = node.toString().substring(node.toString().indexOf(".") + 1);
					String methodName = fullMethod.substring(0, fullMethod.indexOf("("));
					if(widgets_classes.containsKey(objectName)){
						return getObjectMethod(node, objectName, methodName, true);
					}
					else if(composite_classes.containsKey(objectName)){
						return getObjectMethod(node, objectName, methodName, false);
					}
					else
						//variáveis que não sejam nem widgets e composite vão aqui fazer return a null
						return null;
				}
				else return getConstructor(class_object_args[j], String.class).newInstance(argument_expression.toString().replaceAll("\"", ""));
			}
		}
	}

	

	//	private void resolveNewDeclarations(Class<?>[] class_args,
	//			Object[] objects, int i, Expression exp)
	//					throws ClassNotFoundException, InstantiationException,
	//					IllegalAccessException, InvocationTargetException,
	//					NoSuchMethodException {
	//		NewStatementVisitor visitor_aux = new NewStatementVisitor();
	//		exp.accept(visitor_aux);
	//
	//		ClassInstanceCreation new_class = visitor_aux.getNew_argument_classes().get(0);
	//		Class<?>[] class_object_args = new Class<?>[new_class.arguments().size()];
	//		for(int j = 0; j != new_class.arguments().size(); j++){
	//			Object o = new_class.arguments().get(j);
	//			class_object_args[j] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
	//		}
	//		Object[] objects1 = new Object[new_class.arguments().size()];
	//		for(int j = 0; j != class_object_args.length; j++){
	//			Expression argument_expression = (Expression)new_class.arguments().get(j);
	//			objects1[j] = resolveType(class_object_args, argument_expression, j); 
	//		}
	//		objects[i] = getConstructor(class_args[i], class_object_args).newInstance(objects1);
	//	}

	private Object constructObject(Expression exp, Class<?> c) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		NewStatementVisitor visitor_aux = new NewStatementVisitor();
		exp.accept(visitor_aux);
		ClassInstanceCreation new_class = visitor_aux.getNew_argument_classes().get(0);
		Class<?>[] class_object_args = new Class<?>[new_class.arguments().size()];
		for(int j = 0; j != new_class.arguments().size(); j++){
			Object o = new_class.arguments().get(j);
			class_object_args[j] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
		}
		Object[] objects1 = new Object[new_class.arguments().size()];
		for(int j = 0; j != class_object_args.length; j++){
			Expression argument_expression = (Expression)new_class.arguments().get(j);
			objects1[j] = resolveType(class_object_args, argument_expression, j); 
		}
		return getConstructor(c, class_object_args).newInstance(objects1);
	}

	private Constructor<?> getConstructor(Class <?> c, Class<?>... params){
		if(c.isPrimitive()){
			c = primitive_classes.get(c);
		}
		Class<?>[] params_aux = params.clone();
		for(int i = 0; i != params_aux.length; i++){
			if(primitive_classes_inverse.containsKey(params_aux[i])){
				params_aux[i] = primitive_classes_inverse.get(params_aux[i]);
			}
		}
		try {
			return c.getConstructor(params_aux);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getNameDeclaration(VariableDeclarationStatement vd) {
		return ((VariableDeclarationFragment)vd.fragments().get(0)).getName().toString();
	}

	private Class<?> resolveClass(String classpath){
		for(Class<?> c : primitive_classes.keySet()){
			if(c.getName().equals(classpath))
				return c;
		}
		try {
			return Class.forName(classpath);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
