package org.eclipselabs.guita.ipreviews.view;


import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
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
import org.eclipselabs.guita.ipreviews.handler.ArrayAccessVisitor;
import org.eclipselabs.guita.ipreviews.handler.ArrayInitVisitor;
import org.eclipselabs.guita.ipreviews.handler.ConstantStatementVisitor;
import org.eclipselabs.guita.ipreviews.handler.MethodInvocationVisitor;
import org.eclipselabs.guita.ipreviews.handler.NewStatementVisitor;
import org.eclipselabs.guita.ipreviews.handler.VariableDeclarationStatementVisitor;
import org.eclipselabs.guita.ipreviews.regex.Regex;

public class PreviewView extends ViewPart {

	private final Map<Class<?>, Class<?>> equivalent_classes = SupportClasses.equivalent_classes;
	private final Map<Class<?>, Class<?>> primitive_classes = SupportClasses.primitive_classes;
	private final Map<Class<?>, Class<?>> primitive_classes_inverse = SupportClasses.primitive_classes_inverse;
	public final static boolean devMode = true;

	private HashMap<String, Control> controls = new HashMap<String, Control>();
	private HashMap<String, Class<?>> controls_classes = new HashMap<String, Class<?>>();
	private HashMap<String, Widget> widgets = new HashMap<String, Widget>();
	private	HashMap<String, Class<?>> widgets_classes = new HashMap<String, Class<?>>();

	private	HashMap<String, Object> variables = new HashMap<String, Object>();
	private	HashMap<String, Class<?>> variables_classes = new HashMap<String, Class<?>>();

	private List<String> shells = new ArrayList<String>();

	public static final String ID = "guidesign.previewview";

	Composite composite;

	public PreviewView(){


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
					Class<?> variable_class = null;
					String class_name = node.resolveBinding().getType().getQualifiedName();
					if(SupportClasses.primitive_classes_string.containsKey(class_name)){
						variable_class = SupportClasses.primitive_classes_string.get(class_name);
					}
					else {
						if(class_name.contains("[]") && !SupportClasses.primitive_classes_string.containsKey(class_name))
							class_name = "[L" + class_name.replace("[]","") + ";";
						variable_class = Class.forName(class_name);
					}
					if(Widget.class.isAssignableFrom(variable_class) || Control.class.isAssignableFrom(variable_class) || variable_class.equals(Shell.class)){
						resolveDeclarationSWT(node, variable_class);
					}
					else {
						resolveDeclaration(node, variable_class);
					}
				} catch (Exception e) {
					System.out.println("Falhei aqui - " + node);
					if(devMode)
						e.printStackTrace();
				}
			}
			else if(node_class.equals(MethodInvocation.class)){
				MethodInvocation node = (MethodInvocation) nodes.get(i);
				String objectName = node.toString().substring(0, node.toString().indexOf("."));
				try {
					resolveMethod(node, objectName, node.getName().toString());
				} catch (NoSuchMethodException e){
					if(!shells.contains(objectName)){
						System.out.println("Falhei aqui - " + node);
						if(devMode)
							e.printStackTrace();
					}
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("Falhei aqui - " + node);
					if(devMode)
						e.printStackTrace();
				}
			}
			else if(node_class.equals(Assignment.class)){
				Assignment node = (Assignment) nodes.get(i);
				try {
					Class<?> variable_class = null;
					if(SupportClasses.primitive_classes_string.containsKey(node.resolveTypeBinding().getQualifiedName())){
						variable_class = SupportClasses.primitive_classes_string.get(node.resolveTypeBinding().getQualifiedName());
						variable_class = primitive_classes.get(variable_class);
					}
					else variable_class = Class.forName(node.resolveTypeBinding().getQualifiedName());
					resolveAssignment(node, variable_class);
				} catch (Exception e) {
					System.out.println("Falhei aqui - " + node);
					if(devMode)
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


		System.out.println("COMPOSITES: " + controls);
		System.out.println("WIDGETS: " + widgets);
		System.out.println("WIDGETS_CLASSES: " + widgets_classes);

	}

	private void resolveDeclaration(VariableDeclarationFragment node, Class<?> variable_class) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArrayIndexOutOfBoundsException, NoSuchFieldException, SecurityException, IllegalArgumentException {
		String expression = node.toString().substring(node.toString().indexOf("=") + 1).trim();
		if(!node.toString().contains("=")){
			variables.put(node.getName().toString(), null);
			variables_classes.put(node.getName().toString(), variable_class);
		}
		else if(expression.matches(Regex.newArguments)){
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
			variables.put(node.getName().toString(), variable);
			variables_classes.put(node.getName().toString(), variable_class);
		}
		else if(expression.matches(Regex.methodDeclarations) || expression.matches(Regex.methodArrayDeclarations)){
			MethodInvocationVisitor method_visitor = new MethodInvocationVisitor();
			node.accept(method_visitor);
			MethodInvocation m_node = method_visitor.getNode();
			String objectName;
			objectName = m_node.toString().substring(0, m_node.toString().indexOf("."));
			Object variable;
			if(m_node.toString().matches(Regex.methodArrayDeclarations)){
				objectName = m_node.toString().substring(0, m_node.toString().indexOf("["));
				ArrayAccessVisitor array_visitor = new ArrayAccessVisitor();
				m_node.accept(array_visitor);
				Class<?>[] aux = new Class<?>[1];
				aux[0] = Integer.class;
				int index = (int) resolveType(aux, array_visitor.getNode().getIndex(), 0);
				String aux_name = objectName+"["+index+"]";
				variable = getObjectMethod(m_node, aux_name, m_node.getName().toString());
			}
			else variable = getObjectMethod(m_node, objectName, m_node.getName().toString());
			variables.put(node.getName().toString(), variable);
			variables_classes.put(node.getName().toString(), variable_class);
		}
		else if(expression.matches(Regex.constantDeclarations) || expression.matches(Regex.constantArrayDeclarations)){
			ConstantStatementVisitor visitor = new ConstantStatementVisitor();
			node.accept(visitor);
			QualifiedName object = visitor.getQualifiedName();
			Object variable = getConstantObject(object);
			variables.put(node.getName().toString(), variable);
			variables_classes.put(node.getName().toString(), variable_class);
		}
		else if(expression.matches(Regex.newArgumentsArray)){
			Object variable = resolveNewArgumentsArray(node, variable_class);
			variables.put(node.getName().toString(), variable);
			variables_classes.put(node.getName().toString(), variable_class);
		}
		else {
			if(expression.matches(Regex.arrayAccess)){
				ArrayAccessVisitor aac_visitor = new ArrayAccessVisitor();
				node.accept(aac_visitor);
				String aux_name = aac_visitor.getNode().getArray().toString();
				String aux_index = aac_visitor.getNode().getIndex().toString();
				Class<?>[] aux = new Class<?>[1];
				aux[0] = Integer.class;
				Object index = resolveType(aux, aac_visitor.getNode().getIndex(), 0);
				variables.put(node.getName().toString(), Array.get(variables.get(aux_name), (int) index));
				variables_classes.put(node.getName().toString(), variable_class);
			}
			else if(variables.containsKey(expression.toString())){
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

	private void resolveDeclarationSWT(VariableDeclarationFragment vf, Class<?> variable_class) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ArrayIndexOutOfBoundsException, ClassNotFoundException, NoSuchFieldException {
		if(!vf.toString().contains("=")){
			if(Control.class.isAssignableFrom(variable_class) && !Widget.class.isAssignableFrom(variable_class)){
				controls.put(getNameDeclaration(vf), null);
				controls_classes.put(getNameDeclaration(vf), variable_class);
			}
			else {
				widgets.put(getNameDeclaration(vf), null);
				widgets_classes.put(getNameDeclaration(vf), variable_class);
			}
		}
		else{
			VariableDeclarationStatementVisitor variable_visitor = new VariableDeclarationStatementVisitor();
			vf.accept(variable_visitor);
			ClassInstanceCreation node = variable_visitor.getNode();
			if(node.arguments().size() == 1 && variable_class.equals(Shell.class)){
				controls.put(getNameDeclaration(vf), composite);
				controls_classes.put(getNameDeclaration(vf), Composite.class);
				shells.add(getNameDeclaration(vf));
			}
			else {
				Object int_aux = null;
				if(node != null && node.arguments().size() == 2){
					if(((Expression)node.arguments().get(1)).toString().matches(Regex.constantDeclarations)){
						ConstantStatementVisitor constant_visitor = new ConstantStatementVisitor();
						((Expression)node.arguments().get(1)).accept(constant_visitor);
						int_aux = getConstantObject(constant_visitor.getQualifiedName());
					}
					else int_aux = getConstructor(Integer.class, String.class).newInstance(((Expression)node.arguments().get(1)).toString());
					if(Composite.class.isAssignableFrom(variable_class)){
						Control aux_control;
						Constructor<?> constr = getConstructor(variable_class, Composite.class, int.class);
						if(controls.containsKey(node.arguments().get(0).toString())){
							Composite aux = (Composite) controls.get(node.arguments().get(0).toString());
							aux_control = (Control) constr.newInstance(aux, int_aux);
						}
						else aux_control = (Control) constr.newInstance(composite, int_aux);
						controls.put(getNameDeclaration(vf), aux_control);
						controls_classes.put(getNameDeclaration(vf), variable_class);
					}
					else {
						System.out.println("NAHHHHHHHHHHHHHHHHH " + node.arguments().get(0).toString());
						Constructor<?> constr = getConstructor(variable_class, Composite.class, int.class);
						Widget widget;
						if(controls.containsKey(node.arguments().get(0).toString())){
							Composite aux = (Composite) controls.get(node.arguments().get(0).toString());
							widget = (Widget) constr.newInstance(aux, int_aux);
						}
						else widget = (Widget) constr.newInstance(composite, int_aux);
						widgets.put(getNameDeclaration(vf), widget);
						widgets_classes.put(getNameDeclaration(vf), variable_class);
					}
				}
			}
		}
	}

	private void resolveAssignment(Assignment node, Class<?> variable_class) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
		String expression = node.getRightHandSide().toString();
		String name = node.getLeftHandSide().toString();
		Object object = null;
		int index = -1;
		if(name.matches(Regex.arrayAccess)){
			ArrayAccessVisitor aac_visitor = new ArrayAccessVisitor();
			node.accept(aac_visitor);
			String aux_name = aac_visitor.getNode().getArray().toString();
			String aux_index = aac_visitor.getNode().getIndex().toString();
			Class<?>[] aux = new Class<?>[1];
			aux[0] = Integer.class;
			index = (int) resolveType(aux, aac_visitor.getNode().getIndex(), 0);
			object = variables.get(aux_name);
			if(!variables.containsKey(aux_name)){
				throw new IllegalAccessException(aux_name);
			}
		}
		else if(controls.containsKey(name)){
			object = controls.get(name);
		}
		else if(widgets.containsKey(name)){
			object = widgets.get(name);
		}
		else if(variables.containsKey(name)){
			object = variables.get(name);
		}
		else throw new NoSuchFieldException(name);
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
				if(Control.class.isAssignableFrom(variable_class)){
					for(int i = 0; i != class_args.length; i++){
						if(Composite.class.isAssignableFrom(class_args[i])){
							class_args[i] = Composite.class;
						}
					}
				}
				Constructor<?> constr = getConstructor(variable_class, class_args);
				variable = constr.newInstance(objects);
			}
			if(name.matches(Regex.arrayAccess)){
				Array.set(object, index, variable_class.cast(variable));
			}
			else object = variable;
		}
		else if(expression.matches(Regex.methodDeclarations) || expression.matches(Regex.methodArrayDeclarations)){
			MethodInvocationVisitor method_visitor = new MethodInvocationVisitor();
			node.accept(method_visitor);
			MethodInvocation m_node = method_visitor.getNode();
			String objectMethodName = m_node.toString().substring(0, m_node.toString().indexOf("."));
			if(m_node.toString().matches(Regex.methodArrayDeclarations)){
				objectMethodName = m_node.toString().substring(0, m_node.toString().indexOf("["));
				ArrayAccessVisitor array_visitor = new ArrayAccessVisitor();
				m_node.accept(array_visitor);
				Class<?>[] aux = new Class<?>[1];
				aux[0] = Integer.class;
				index = (int) resolveType(aux, array_visitor.getNode().getIndex(), 0);
				String aux_name = objectMethodName+"["+index+"]";
				if(name.matches(Regex.arrayAccess)){
					Array.set(object, index, variable_class.cast(getObjectMethod(m_node, objectMethodName, m_node.getName().toString())));
				}
				else object = getObjectMethod(m_node, aux_name, m_node.getName().toString());
			}
			else{
				if(name.matches(Regex.arrayAccess)){
					Array.set(object, index, variable_class.cast(getObjectMethod(m_node, objectMethodName, m_node.getName().toString())));
				}
				else object = getObjectMethod(m_node, objectMethodName, m_node.getName().toString());
			}
		}
		else if(expression.matches(Regex.constantDeclarations) || expression.matches(Regex.constantArrayDeclarations)){
			ConstantStatementVisitor visitor = new ConstantStatementVisitor();
			node.accept(visitor);
			QualifiedName constant = visitor.getQualifiedName();
			Object variable = getConstantObject(constant);
			if(name.matches(Regex.arrayAccess)){
				Array.set(object, index, variable_class.cast(variable));
			}
			else object = variable;
		}
		else if(expression.matches(Regex.newArgumentsArray)){
			Object variable = resolveNewArgumentsArray(node, variable_class);
			object = variable;
		}
		else {
			if(expression.matches(Regex.arrayAccess)){
				ArrayAccessVisitor aac_visitor = new ArrayAccessVisitor();
				node.accept(aac_visitor);
				String aux_name = aac_visitor.getNode().getArray().toString();
				String aux_index = aac_visitor.getNode().getIndex().toString();
				Class<?>[] aux = new Class<?>[1];
				aux[0] = Integer.class;
				index = (int) resolveType(aux, aac_visitor.getNode().getIndex(), 0);
				if(name.matches(Regex.arrayAccess)){
					Array.set(object, index, variable_class.cast(Array.get(variables.get(aux_name), (int) index)));
				}
				else object = Array.get(variables.get(aux_name), (int) index);
			}
			else if(variables.containsKey(expression.toString())){
				Object variable = variables.get(expression.toString());
				if(name.matches(Regex.arrayAccess)){
					Array.set(object, index, variable_class.cast(variable));
				}
				else object = variable;
			}
			else if(controls.containsKey(expression.toString())){
				Object variable = controls.get(expression.toString());
				if(name.matches(Regex.arrayAccess)){
					Array.set(object, index, variable_class.cast(variable));
				}
				else object = variable;
			}
			else if(widgets.containsKey(expression.toString())){
				Object variable = widgets.get(expression.toString());
				if(name.matches(Regex.arrayAccess)){
					Array.set(object, index, variable_class.cast(variable));
				}
				else object = variable;
			}
			else {
				Object variable = getConstructor(variable_class, String.class).newInstance(expression.toString().replaceAll("\"", ""));
				if(name.matches(Regex.arrayAccess)){
					System.out.println(object  + " --- " +  index + " --- " + variable_class);
					Array.set(object, index, variable_class.cast(variable));
				}
				else object = variable;
			}
		}
	}

	private void resolveMethod(MethodInvocation mi, String objectName, String methodName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, ArrayIndexOutOfBoundsException, NoSuchFieldException {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		Object object = null;
		Class<?> methodClass = null;
		if(objectName.matches(Regex.arrayAccess)){
			ArrayAccessVisitor aac_visitor = new ArrayAccessVisitor();
			mi.accept(aac_visitor);
			String aux_name = aac_visitor.getNode().getArray().toString();
			String aux_index = aac_visitor.getNode().getIndex().toString();
			Class<?>[] aux = new Class<?>[1];
			aux[0] = Integer.class;
			Object index = resolveType(aux, aac_visitor.getNode().getIndex(), 0);
			object = Array.get(variables.get(aux_name), (int) index);
			methodClass = object.getClass();
		}
		else if(controls.containsKey(objectName)){
			object = controls.get(objectName);
			methodClass = controls_classes.get(objectName);

		}
		else if(widgets.containsKey(objectName)){
			object = widgets.get(objectName);
			methodClass = widgets_classes.get(objectName);
		}
		else if(variables.containsKey(objectName)){
			object = variables.get(objectName);
			methodClass = variables_classes.get(objectName);
		}
		else {
			throw new NoSuchMethodException(methodName);
		}
		for(int i = 0; i != mi.arguments().size(); i++){
			Object o = mi.arguments().get(i);
			class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
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
		m.invoke(object, objects);
	}

	private Object getObjectMethod(MethodInvocation mi, String objectName, String methodName) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, ArrayIndexOutOfBoundsException, NoSuchFieldException, IllegalArgumentException {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		Object object = null;
		Class<?> methodClass = null;
		if(objectName.matches(Regex.arrayAccess)){
			ArrayAccessVisitor aac_visitor = new ArrayAccessVisitor();
			mi.accept(aac_visitor);
			String aux_name = aac_visitor.getNode().getArray().toString();
			String aux_index = aac_visitor.getNode().getIndex().toString();
			Class<?>[] aux = new Class<?>[1];
			aux[0] = Integer.class;
			Object index = resolveType(aux, aac_visitor.getNode().getIndex(), 0);
			if(variables.get(aux_name) == null)
				throw new IllegalArgumentException(aux_name);
			object = Array.get(variables.get(aux_name), (int) index);
			methodClass = object.getClass();
		}
		else if(controls.containsKey(objectName)){
			object = controls.get(objectName);
			methodClass = controls_classes.get(objectName);

		}
		else if(widgets.containsKey(objectName)){
			object = widgets.get(objectName);
			methodClass = widgets_classes.get(objectName);
		}
		else if(variables.containsKey(objectName)){
			object = variables.get(objectName);
			methodClass = variables_classes.get(objectName);
		}
		else {
			throw new NoSuchMethodException(methodName);
		}
		for(int i = 0; i != mi.arguments().size(); i++){
			Object o = mi.arguments().get(i);
			class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
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
		return m.invoke(object, objects);
	}

	private Object resolveNewArgumentsArray(ASTNode node,
			Class<?> variable_class) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ArrayIndexOutOfBoundsException, NoSuchFieldException, SecurityException, IllegalArgumentException {
		ArrayInitVisitor visitor = new ArrayInitVisitor();
		node.accept(visitor);
		ArrayCreation c_node = visitor.getNode();
		Class<?>[] aux = new Class<?>[1];
		aux[0] = Integer.class;
		Expression exp = (Expression) c_node.dimensions().get(0);
		Object length = resolveType(aux, exp, 0);
		Class<?> aux_class = null;
		System.out.println("CLAZZZZ " + variable_class.getCanonicalName());
		String aux_name = variable_class.getCanonicalName();
		if(SupportClasses.primitive_array_to_object.containsKey(variable_class))
			aux_class = SupportClasses.primitive_array_to_object.get(variable_class);
		else if(variable_class.getCanonicalName().contains("[]"))
			aux_class = Class.forName(aux_name.replace("[]", ""));
		else throw new ClassNotFoundException(variable_class.toString());
		return Array.newInstance(aux_class, (int) length);
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

	private Object constructObject(Expression exp, Class<?> c) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ArrayIndexOutOfBoundsException, NoSuchFieldException {
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

	private Object resolveType(Class<?>[] class_object_args, Expression argument_expression, int j)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, ArrayIndexOutOfBoundsException, NoSuchFieldException, SecurityException, IllegalArgumentException {
		if(argument_expression.toString().matches(Regex.newArguments)){
			return constructObject(argument_expression, class_object_args[j]);
		}
		else {
			if(argument_expression.toString().matches(Regex.constantDeclarations) || argument_expression.toString().matches(Regex.constantArrayDeclarations)){
				ConstantStatementVisitor constant_visitor = new ConstantStatementVisitor();
				argument_expression.accept(constant_visitor);
				return getConstantObject(constant_visitor.getQualifiedName());
			}
			else {
				if(argument_expression.toString().matches(Regex.methodDeclarations) || argument_expression.toString().matches(Regex.methodArrayDeclarations)){
					MethodInvocationVisitor method_visitor = new MethodInvocationVisitor();
					argument_expression.accept(method_visitor);
					MethodInvocation node = method_visitor.getNode();
					String objectName = node.toString().substring(0, node.toString().indexOf("."));
					if(argument_expression.toString().matches(Regex.methodArrayDeclarations)){
						objectName = node.toString().substring(0, node.toString().indexOf("["));
						ArrayAccessVisitor array_visitor = new ArrayAccessVisitor();
						node.accept(array_visitor);
						Class<?>[] aux = new Class<?>[1];
						aux[0] = Integer.class;
						int index = (int) resolveType(aux, array_visitor.getNode().getIndex(), 0);
						String aux_name = objectName+"["+index+"]";
						return getObjectMethod(node, aux_name, node.getName().toString());
					}
					else return getObjectMethod(node, objectName, node.getName().toString());
				}
				else {
					if(argument_expression.toString().matches(Regex.newArgumentsArray)){
						return resolveNewArgumentsArray(argument_expression, class_object_args[j]);
					}
					else {
						String objectName = argument_expression.toString();
						if(objectName.matches(Regex.arrayAccess)){
							ArrayAccessVisitor aac_visitor = new ArrayAccessVisitor();
							argument_expression.accept(aac_visitor);
							String aux_name = aac_visitor.getNode().getArray().toString();
							Class<?>[] aux = new Class<?>[1];
							aux[0] = Integer.class;
							Object index = resolveType(aux, aac_visitor.getNode().getIndex(), 0);
							if(variables.get(aux_name) == null)
								throw new IllegalArgumentException(argument_expression.toString());
							return Array.get(variables.get(aux_name), (int) index);
						}
						else if(widgets.containsKey(objectName))
							return widgets.get(objectName);
						else if(controls.containsKey(objectName)){
							return controls.get(objectName);
						}
						else if(variables.containsKey(objectName)){
							return variables.get(objectName);
						}
						else {
							Object aux = getConstructor(class_object_args[j], String.class).newInstance(argument_expression.toString().replaceAll("\"", ""));
							if(aux == null)
								throw new IllegalArgumentException(argument_expression.toString());
							return aux;
						}
					}
				}
			}
		}
	}

	private Object getConstantObject(QualifiedName node) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArrayIndexOutOfBoundsException, NoSuchFieldException, SecurityException, IllegalArgumentException{
		String objectName = node.getQualifier().toString();
		String fieldName = node.getName().toString();
		if(node.toString().matches(Regex.constantArrayDeclarations)){
			Field field;
			objectName = node.getQualifier().toString().substring(0, node.toString().indexOf("["));
			ArrayAccessVisitor visitor = new ArrayAccessVisitor();
			node.accept(visitor);
			ArrayAccess a_node = visitor.getNode();
			Class<?>[] aux = new Class<?>[1];
			aux[0] = Integer.class;
			Object index = resolveType(aux, a_node.getIndex(), 0);
			field = Array.get(variables_classes.get(objectName), (int)index).getClass().getDeclaredField(fieldName);
			return field.get(variables.get(objectName));
		}
		else if(variables.containsKey(objectName)){
			Field field = variables_classes.get(objectName).getDeclaredField(fieldName);
			return field.get(variables.get(objectName));
		}
		else if(widgets.containsKey(objectName)){
			Field field = widgets_classes.get(objectName).getDeclaredField(fieldName);
			return field.get(widgets.get(objectName));
		}
		else if(controls.containsKey(objectName)){
			Field field = controls_classes.get(objectName).getDeclaredField(fieldName);
			return field.get(controls.get(objectName));
		}
		else return node.resolveConstantExpressionValue();
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

	private Constructor<?> getConstructor(Class <?> c, Class<?>... params) throws NoSuchMethodException, SecurityException{
		if(c.isPrimitive()){
			c = primitive_classes.get(c);
		}
		Class<?>[] params_aux = params.clone();
		for(int i = 0; i != params_aux.length; i++){
			if(primitive_classes_inverse.containsKey(params_aux[i])){
				params_aux[i] = primitive_classes_inverse.get(params_aux[i]);
			}
		}
		return c.getConstructor(params_aux);
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
			if(devMode)
				e.printStackTrace();
			return null;
		}
	}

}
