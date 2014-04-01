package org.eclipselabs.guita.ipreviews.view;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
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
import org.eclipse.swt.widgets.Display;
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

	private HashMap<String, Composite> composites = new HashMap<String, Composite>();
	private HashMap<String, Widget> widgets = new HashMap<String, Widget>();
	private	HashMap<String, Class<?>> widgets_classes = new HashMap<String, Class<?>>();

	private	HashMap<String, Object> variables = new HashMap<String, Object>();
	private	HashMap<String, Class<?>> variables_classes = new HashMap<String, Class<?>>();

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

	public void constructPreview(List<ASTNode> nodes, List<Class<?>> nodes_classes) {

		for(int i = 0; i != nodes.size(); i++){
			Class<?> node_class = nodes_classes.get(i);
			if(node_class.equals(VariableDeclarationFragment.class)){
				VariableDeclarationFragment node = (VariableDeclarationFragment) nodes.get(i);
				try {
					Class<?> variable_class = Class.forName(node.resolveBinding().getType().getQualifiedName());
					if(Widget.class.isAssignableFrom(variable_class) || Composite.class.isAssignableFrom(variable_class) || variable_class.equals(Shell.class)){
						resolveDeclarationSWT(node, variable_class);
					}
					else {
						resolveDeclaration(node, variable_class);
					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
					System.out.println("Falhei aqui - " + node);
					e.printStackTrace();
				}
			}
			else if(node_class.equals(MethodInvocation.class)){
				MethodInvocation node = (MethodInvocation) nodes.get(i);
				String objectName = node.toString().substring(0, node.toString().indexOf("."));
				try {
					resolveMethod(node, objectName, node.getName().toString());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | ClassNotFoundException | InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (NoSuchMethodException e){
					if(!shells.contains(objectName)){
						System.out.println("Falhei aqui - " + node);
						e.printStackTrace();
					}
				}
			}
			else if(node_class.equals(Assignment.class)){
				Assignment node = (Assignment) nodes.get(i);
				try {
					Class<?> variable_class = Class.forName(node.resolveTypeBinding().getQualifiedName());
					resolveAssignment(node, variable_class);
				} catch (InstantiationException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					System.out.println("Falhei aqui - " + node);
					e.printStackTrace();
				}
			}
		}

		//		resolveDeclarations(declarations);
		//
		//		for(MethodInvocation mi : methods){
		//			String objectName = mi.toString().substring(0, mi.toString().indexOf("."));
		//			String fullMethod = mi.toString().substring(mi.toString().indexOf(".") + 1);
		//			String methodName = fullMethod.substring(0, fullMethod.indexOf("("));
		//			if(widgets_classes.containsKey(objectName)){
		//				resolveMethod(mi, objectName, methodName, true);
		//			}
		//			else if(composite_classes.containsKey(objectName)){
		//				resolveMethod(mi, objectName, methodName, false);
		//			}
		//		}


		System.out.println("COMPOSITES: " + composites);
		System.out.println("WIDGETS: " + widgets);
		System.out.println("WIDGETS_CLASSES: " + widgets_classes);

	}

	private void resolveAssignment(Assignment node, Class<?> variable_class) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		String expression = node.toString().substring(node.toString().indexOf("=") + 1).trim();
		String objectName = node.toString().substring(0, node.toString().indexOf("=")).trim();
		if(expression.matches(Regex.newArguments)){
			VariableDeclarationStatementVisitor variable_visitor = new VariableDeclarationStatementVisitor();
			node.accept(variable_visitor);
			ClassInstanceCreation c_node = variable_visitor.getNode();
			Class<?> [] class_args = new Class<?> [c_node.arguments().size()];
			for(int i = 0; i != c_node.arguments().size(); i++){
				Object o = c_node.arguments().get(i);
				class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
			}
			Class<?> [] class_args2 = class_args.clone();
			for(int i = 0; i != class_args2.length; i++) {
				if(equivalent_classes.containsKey(class_args2[i]))
					class_args2[i] = equivalent_classes.get(class_args2[i]);
			}	
			Object[] objects = new Object[c_node.arguments().size()];
			for(int i = 0; i != class_args.length; i++){
				Expression exp = (Expression)c_node.arguments().get(i);
				objects[i] = resolveType(class_args, exp, i); 
			}
			Object variable;
			if(class_args.length == 0){
				variable = variable_class.newInstance();
			}
			else { 
				Constructor<?> constr = getConstructor(variable_class, class_args);
				variable = constr.newInstance(objects);
			}
			if(Composite.class.isAssignableFrom(variable_class)){
				Object o = composites.get(objectName);
				o = variable;
			}
			else if(Widget.class.isAssignableFrom(variable_class)){
				Object o = widgets.get(objectName);
				o = variable;
			}
			else {
				Object o = variables.get(objectName);
				o = variable;
			}
		}
		else if(expression.matches(Regex.methodDeclarations)){
			MethodInvocationVisitor method_visitor = new MethodInvocationVisitor();
			node.accept(method_visitor);
			MethodInvocation m_node = method_visitor.getNode();
			String objectMethodName = m_node.toString().substring(0, m_node.toString().indexOf("."));
			System.out.println("OLAAAAAAAAAA " + objectMethodName + " -- " + method_visitor.getNode());
			if(Composite.class.isAssignableFrom(variable_class)){
				Object o = composites.get(objectName);
				o = getObjectMethod(m_node, objectMethodName, m_node.getName().toString());
			}
			else if(Widget.class.isAssignableFrom(variable_class)){
				Object o = widgets.get(objectName);
				o = getObjectMethod(m_node, objectMethodName, m_node.getName().toString());
			}
			else {
				System.out.println("ENTREI " + objectMethodName + " -- " + method_visitor.getNode());
				Object o = variables.get(objectName);
				o = getObjectMethod(m_node, objectMethodName, m_node.getName().toString());
			}
		}
		else if(expression.matches(Regex.constantDeclarations)){
			ConstantStatementVisitor visitor = new ConstantStatementVisitor();
			node.accept(visitor);
			if(visitor.getObjects_map().containsKey(expression.toString())){
				Object variable = visitor.getObjects_map().get(expression.toString());
				if(Composite.class.isAssignableFrom(variable_class)){
					Object o = composites.get(objectName);
					o = variable;
				}
				else if(Widget.class.isAssignableFrom(variable_class)){
					Object o = widgets.get(objectName);
					o = variable;
				}
				else {
					Object o = variables.get(objectName);
					o = variable;
				}
			}
		}
		else {
			if(variables.containsKey(expression.toString())){
				Object variable = variables.get(expression.toString());
				Object o = variables.get(objectName);
				o = variable;
			}
			else if(composites.containsKey(expression.toString())){
				Object variable = composites.get(expression.toString());
				Object o = composites.get(objectName);
				o = variable;
			}
			else if(widgets.containsKey(expression.toString())){
				Object variable = widgets.get(expression.toString());
				Object o = widgets.get(objectName);
				o = variable;
			}
			else {
				Object variable = getConstructor(variable_class, String.class).newInstance(expression.toString().replaceAll("\"", ""));
				Object o = variables.get(objectName);
				o = variable;
			}
		}
	}

	private void resolveDeclaration(VariableDeclarationFragment node, Class<?> variable_class) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String expression = node.toString().substring(node.toString().indexOf("=") + 1).trim();
		if(expression.matches(Regex.newArguments)){
			VariableDeclarationStatementVisitor variable_visitor = new VariableDeclarationStatementVisitor();
			node.accept(variable_visitor);
			ClassInstanceCreation c_node = variable_visitor.getNode();
			Class<?> [] class_args = new Class<?> [c_node.arguments().size()];
			for(int i = 0; i != c_node.arguments().size(); i++){
				Object o = c_node.arguments().get(i);
				class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
			}
			Class<?> [] class_args2 = class_args.clone();
			for(int i = 0; i != class_args2.length; i++) {
				if(equivalent_classes.containsKey(class_args2[i]))
					class_args2[i] = equivalent_classes.get(class_args2[i]);
			}	
			Object[] objects = new Object[c_node.arguments().size()];
			for(int i = 0; i != class_args.length; i++){
				Expression exp = (Expression)c_node.arguments().get(i);
				objects[i] = resolveType(class_args, exp, i); 
			}
			System.out.println("OLAAAAAAAAAA " + Arrays.toString(objects) + " -- " + Arrays.toString(class_args));
			Object variable;
			if(class_args.length == 0){
				variable = variable_class.newInstance();
			}
			else { 
				Constructor<?> constr = getConstructor(variable_class, class_args);
				variable = constr.newInstance(objects);
			}
			variables.put(node.getName().toString(), variable);
			variables_classes.put(node.getName().toString(), variable_class);
		}
		else if(expression.matches(Regex.methodDeclarations)){
			MethodInvocationVisitor method_visitor = new MethodInvocationVisitor();
			node.accept(method_visitor);
			MethodInvocation m_node = method_visitor.getNode();
			String objectName = m_node.toString().substring(0, m_node.toString().indexOf("."));
			Object variable = getObjectMethod(m_node, objectName, m_node.getName().toString());
			variables.put(node.getName().toString(), variable);
			variables_classes.put(node.getName().toString(), variable_class);
		}
		else if(expression.matches(Regex.constantDeclarations)){
			ConstantStatementVisitor visitor = new ConstantStatementVisitor();
			node.accept(visitor);
			if(visitor.getObjects_map().containsKey(expression.toString())){
				Object variable = visitor.getObjects_map().get(expression.toString());
				variables.put(node.getName().toString(), variable);
				variables_classes.put(node.getName().toString(), variable_class);
			}
		}
		else {
			if(variables.containsKey(expression.toString())){
				Object variable = variables.get(expression.toString());
				variables.put(node.getName().toString(), variable);
				variables_classes.put(node.getName().toString(), variable_class);
			}
			else {
				Object variable = getConstructor(variable_class, String.class).newInstance(expression.toString().replaceAll("\"", ""));
				variables.put(node.getName().toString(), variable);
				variables_classes.put(node.getName().toString(), variable_class);
			}
		}
	}

	private void resolveDeclarationSWT(VariableDeclarationFragment vf, Class<?> variable_class) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		VariableDeclarationStatementVisitor variable_visitor = new VariableDeclarationStatementVisitor();
		vf.accept(variable_visitor);
		ClassInstanceCreation node = variable_visitor.getNode();
		if(node.arguments().size() == 1 && variable_class.equals(Shell.class)){
			composites.put(getNameDeclaration(vf), composite);
			shells.add(getNameDeclaration(vf));
		}
		else {
			Object int_aux = null;
			if(node != null && node.arguments().size() == 2){
				if(((Expression)node.arguments().get(1)).toString().matches(Regex.constantDeclarations)){
					ConstantStatementVisitor c_visitor = new ConstantStatementVisitor();
					((Expression)node.arguments().get(1)).accept(c_visitor);
					int_aux = c_visitor.getObjects_map().get(((Expression)node.arguments().get(1)).toString());
				}
				else int_aux = getConstructor(Integer.class, String.class).newInstance(((Expression)node.arguments().get(1)).toString());
				if(Composite.class.isAssignableFrom(variable_class)){
					Composite aux_composite;
					Constructor<?> constr = getConstructor(variable_class, Composite.class, int.class);
					if(composites.containsKey(node.arguments().get(0).toString())){
						Composite aux = composites.get(node.arguments().get(0).toString());
						aux_composite = (Composite) constr.newInstance(aux, int_aux);
					}
					else aux_composite = (Composite) constr.newInstance(composite, int_aux);
					composites.put(getNameDeclaration(vf), aux_composite);
				}
				else {
					Constructor<?> constr = getConstructor(variable_class, Composite.class, int.class);
					Widget widget;
					if(composites.containsKey(node.arguments().get(0).toString())){
						Composite aux = composites.get(node.arguments().get(0).toString());
						widget = (Widget) constr.newInstance(aux, int_aux);
					}
					else widget = (Widget) constr.newInstance(composite, int_aux);
					widgets.put(getNameDeclaration(vf), widget);
					widgets_classes.put(getNameDeclaration(vf), variable_class);
				}
			}
		}
	}

	private void resolveMethod(MethodInvocation mi, String objectName, String methodName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		for(int i = 0; i != mi.arguments().size(); i++){
			Object o = mi.arguments().get(i);
			class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
		}
		Class<?> methodClass = null;
		if(widgets.containsKey(objectName))
			methodClass = widgets_classes.get(objectName);
		else if(composites.containsKey(objectName)){
			methodClass = Composite.class;
		}
		else if(variables.containsKey(objectName)){
			methodClass = variables_classes.get(objectName);
		}
		Class<?> [] class_args2 = class_args.clone();
		for(int i = 0; i != class_args2.length; i++) {
			if(equivalent_classes.containsKey(class_args2[i]))
				class_args2[i] = equivalent_classes.get(class_args2[i]);
		}
		System.out.println("AQUIIIIII " + objectName + " -- " + methodClass + " -- " + methodName + " --- " + Arrays.toString(class_args2));
		Method m = methodClass.getMethod(methodName, class_args2);
		Object[] objects = new Object[mi.arguments().size()];
		for(int i = 0; i != class_args.length; i++){
			Expression exp = (Expression)mi.arguments().get(i);
			objects[i] = resolveType(class_args, exp, i); 
		}
		if(widgets.containsKey(objectName))
			m.invoke(widgets.get(objectName), objects);
		else if(composites.containsKey(objectName)){
			m.invoke(composites.get(objectName), objects);
		}
		else if(variables.containsKey(objectName)){
			m.invoke(variables.get(objectName), objects);
		}
		else {
			throw new NoSuchMethodException(m.getName());
		}
	}

	private Object getObjectMethod(MethodInvocation mi, String objectName, String methodName) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		for(int i = 0; i != mi.arguments().size(); i++){
			Object o = mi.arguments().get(i);
			class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
		}
		Class<?> methodClass = null;
		if(widgets.containsKey(objectName))
			methodClass = widgets_classes.get(objectName);
		else if(composites.containsKey(objectName)){
			methodClass = Composite.class;
		}
		else if(variables.containsKey(objectName)){
			methodClass = variables_classes.get(objectName);
		}
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
		if(widgets.containsKey(objectName))
			return m.invoke(widgets.get(objectName), objects);
		else if(composites.containsKey(objectName)){
			return m.invoke(composites.get(objectName), objects);
		}
		else if(variables.containsKey(objectName)){
			return m.invoke(variables.get(objectName), objects);
		}
		else return null;
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
					return getObjectMethod(node, objectName, node.getName().toString());
				}
				else {
					String objectName = argument_expression.toString();
					if(widgets.containsKey(objectName))
						return widgets.get(objectName);
					else if(composites.containsKey(objectName)){
						return composites.get(objectName);
					}
					else if(variables.containsKey(objectName)){
						return variables.get(objectName);
					}
					else return getConstructor(class_object_args[j], String.class).newInstance(argument_expression.toString().replaceAll("\"", ""));
				}
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

	private String getNameDeclaration(VariableDeclarationFragment vf) {
		return vf.getName().toString();
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
