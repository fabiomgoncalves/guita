package org.eclipselabs.guita.ipreviews.view;


import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;
import org.eclipselabs.guita.ipreviews.actions.DefaultLayoutChanger;
import org.eclipselabs.guita.ipreviews.handler.ArrayAccessVisitor;
import org.eclipselabs.guita.ipreviews.handler.ArrayInitVisitor;
import org.eclipselabs.guita.ipreviews.handler.ConstantStatementVisitor;
import org.eclipselabs.guita.ipreviews.handler.InfixVisitor;
import org.eclipselabs.guita.ipreviews.handler.MenuLayoutVariables;
import org.eclipselabs.guita.ipreviews.handler.MethodInvocationVisitor;
import org.eclipselabs.guita.ipreviews.handler.NewStatementVisitor;
import org.eclipselabs.guita.ipreviews.handler.VariableDeclarationStatementVisitor;
import org.eclipselabs.guita.ipreviews.regex.Regex;

public class PreviewView extends ViewPart {

	private final Map<Class<?>, Class<?>> primitive_classes = SupportClasses.primitive_classes;
	private final Map<Class<?>, Class<?>> primitive_classes_inverse = SupportClasses.primitive_classes_inverse;
	public final static boolean devMode = false;
	private boolean error = false;
	private final String error_string_initial = "The following expression(s) could not be handled:";
	private String error_string = error_string_initial;

	private HashMap<String, Control> controls = new HashMap<String, Control>();
	private HashMap<String, Class<?>> controls_classes = new HashMap<String, Class<?>>();
	private HashMap<String, Widget> widgets = new HashMap<String, Widget>();
	private	HashMap<String, Class<?>> widgets_classes = new HashMap<String, Class<?>>();

	private	HashMap<String, Object> variables = new HashMap<String, Object>();
	private	HashMap<String, Class<?>> variables_classes = new HashMap<String, Class<?>>();

	private List<String> shells = new ArrayList<String>();

	public static final String ID = "guidesign.previewview";

	private Composite composite;

	public PreviewView(){

	}

	@Override
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(MenuLayoutVariables.getInstance().getLayout());
		//		 andre
		//				getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(new ISelectionListener() {
		//		
		//					@Override
		//					public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		//						if(selection instanceof ITextSelection) {
		//							ITextSelection textSelection = (TextSelection) selection;
		//		
		//							if(textSelection.getEndLine() - textSelection.getStartLine() > 0) {
		//								IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		//								try {
		//									handlerService.executeCommand("ipreviews.preview", null);
		//								} catch (Exception ex) {
		//									ex.printStackTrace();
		//								}
		//							} 
		//						}
		//					}
		//				});
	}


	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void constructPreview(List<ASTNode> nodes, List<Class<?>> nodes_classes) {
		// andre
		for(Control c : composite.getChildren())
			c.dispose();
		cleanAllVariables();

		// clean();

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
						variable_class = resolveClass(class_name);
					}
					if(Widget.class.isAssignableFrom(variable_class) || Control.class.isAssignableFrom(variable_class) || variable_class.equals(Shell.class)){
						resolveDeclarationSWT(node, variable_class);
					}
					else {
						resolveDeclaration(node, variable_class);
					}
				} catch (Exception e) {
					if(node.toString().contains("{"))
						error_string += "\n>> " + node.getName() + "(...)";
					else error_string += "\n>> " + node.toString();
					error = true;
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
						if(node.toString().contains("{"))
							error_string += "\n>> " + node.getName() + "(...)";
						else error_string += "\n>> " + node.toString();
						error = true;
						if(devMode)
							e.printStackTrace();
					}
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					if(node.toString().contains("{"))
						error_string += "\n>> " + node.getName() + "(...)";
					else error_string += "\n>> " + node.toString();
					error = true;
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
					else variable_class = resolveClass(node.resolveTypeBinding().getQualifiedName());
					resolveAssignment(node, variable_class);
				} catch (Exception e) {
					error_string += "\n>> " + node.toString();
					error = true;
					if(devMode)
						e.printStackTrace();
				}
			}
		}

		if(error){
			MessageBox messageBox = new MessageBox(composite.getShell(), SWT.OK);
			messageBox.setMessage(error_string);
			messageBox.open();
			error = false;
			error_string = "";
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


		// andre
		composite.redraw();
		composite.layout();
		// refresh()

		System.out.println("COMPOSITES: " + controls);
		System.out.println("WIDGETS: " + widgets);
		System.out.println("WIDGETS_CLASSES: " + widgets_classes);
		System.out.println("VARIABLES: " + variables);

	}

	private void cleanAllVariables() {
		//		Composite parent = composite.getParent();
		//		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(MenuLayoutVariables.getInstance().getLayout());
		error = false;
		error_string = error_string_initial;
		controls.clear();
		controls_classes.clear();
		widgets.clear();
		widgets_classes.clear();
		variables.clear();
		variables_classes.clear();
	}

	private void resolveDeclaration(VariableDeclarationFragment node, Class<?> variable_class) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ArrayIndexOutOfBoundsException, NoSuchFieldException, SecurityException, IllegalArgumentException {
		String expression = node.toString().substring(node.toString().indexOf("=") + 1).trim();
		if(!node.toString().contains("=")){
			resolveNewVariable(variable_class, node.getName().toString(), null);
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
				Constructor<?> constr = findCompatibleConstructor(variable_class, class_args);
				variable = constr.newInstance(objects);
			}
			resolveNewVariable(variable_class, node.getName().toString(), variable);
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
			resolveNewVariable(variable_class, node.getName().toString(), variable);
		}
		else if(expression.matches(Regex.constantDeclarations) || expression.matches(Regex.constantArrayDeclarations)){
			ConstantStatementVisitor visitor = new ConstantStatementVisitor();
			node.accept(visitor);
			QualifiedName object = visitor.getQualifiedName();
			Object variable = getConstantObject(object);
			resolveNewVariable(variable_class, node.getName().toString(), variable);
		}
		else if(expression.matches(Regex.newArgumentsArray)){
			Object variable = resolveNewArgumentsArray(node, variable_class);
			resolveNewVariable(variable_class, node.getName().toString(), variable);
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
				resolveNewVariable(variable_class, node.getName().toString(), Array.get(variables.get(aux_name), (int) index));
			}
			else if(variables.containsKey(expression.toString())){
				Object variable = variables.get(expression.toString());
				resolveNewVariable(variable_class, node.getName().toString(), variable);
			}
			else {
				Object variable = findCompatibleConstructor(variable_class, String.class).newInstance(expression.toString().replaceAll("\"", ""));
				resolveNewVariable(variable_class, node.getName().toString(), variable);
			}
		}
	}

	private void resolveDeclarationSWT(VariableDeclarationFragment vf, Class<?> variable_class) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ArrayIndexOutOfBoundsException, ClassNotFoundException, NoSuchFieldException {
		if(!vf.toString().contains("=")){
			resolveNewVariable(variable_class, getNameDeclaration(vf), null);
		}
		else{
			VariableDeclarationStatementVisitor variable_visitor = new VariableDeclarationStatementVisitor();
			vf.accept(variable_visitor);
			ClassInstanceCreation node = variable_visitor.getNode();
			if(node != null && node.arguments().size() == 1 && variable_class.equals(Shell.class)){
				controls.put(getNameDeclaration(vf), composite);
				controls_classes.put(getNameDeclaration(vf), Composite.class);
				shells.add(getNameDeclaration(vf));
			}
			else {
				Object int_aux = null;
				if(node != null && node.arguments().size() == 2 && (((Expression)node.arguments().get(1)).resolveTypeBinding().getName().equals("Integer") || ((Expression)node.arguments().get(1)).resolveTypeBinding().getName().equals("int"))){
					Class<?>[] aux_array = new Class<?>[1];
					aux_array[0] = Integer.class;
					int_aux = resolveType(aux_array, (Expression) node.arguments().get(1), 0);
					if(Composite.class.isAssignableFrom(variable_class)){
						Control aux_control;
						Constructor<?> constr = findCompatibleConstructor(variable_class, Composite.class, int.class);
						if(controls.containsKey(node.arguments().get(0).toString())){
							Composite aux = (Composite) controls.get(node.arguments().get(0).toString());
							aux_control = (Control) constr.newInstance(aux, int_aux);
						}
						else{
							Class<?> variable_class2 = resolveClass(((Expression)node.arguments().get(0)).resolveTypeBinding().getQualifiedName());
							if(variable_class2.equals(Shell.class))
								variable_class2 = Composite.class;
							Constructor<?> constr2 = findCompatibleConstructor(variable_class2, Composite.class, int.class);
							Composite aux_composite = (Composite)constr2.newInstance(composite, SWT.NONE);
							((Composite)aux_composite).setLayout(MenuLayoutVariables.getInstance().getLayout());
							controls.put(node.arguments().get(0).toString(), aux_composite);
							controls_classes.put(node.arguments().get(0).toString(), variable_class2);

							aux_control = (Control) constr.newInstance(aux_composite, int_aux);
						}
						controls.put(getNameDeclaration(vf), aux_control);
						controls_classes.put(getNameDeclaration(vf), variable_class);
					}
					else {
						Constructor<?> constr;
						Class<?> variable_class2 = resolveClass(((Expression)node.arguments().get(0)).resolveTypeBinding().getQualifiedName());
						if(variable_class2.equals(Shell.class))
							variable_class2 = Composite.class;
						if(Item.class.isAssignableFrom(variable_class)){
							constr = findCompatibleConstructor(variable_class, variable_class2, int.class);
						} 
						else constr = findCompatibleConstructor(variable_class, Composite.class, int.class);
						Widget widget;
						if(controls.containsKey(node.arguments().get(0).toString())){
							Composite aux = (Composite) controls.get(node.arguments().get(0).toString());
							widget = (Widget) constr.newInstance(aux, int_aux);
						}
						else{
							Constructor<?> constr2 = findCompatibleConstructor(variable_class2, Composite.class, int.class);
							Composite aux_composite = (Composite)constr2.newInstance(composite, SWT.NONE);
							((Composite)aux_composite).setLayout(MenuLayoutVariables.getInstance().getLayout());
							controls.put(node.arguments().get(0).toString(), aux_composite);
							controls_classes.put(node.arguments().get(0).toString(), variable_class2);

							widget = (Widget) constr.newInstance(aux_composite, int_aux);
						}
						widgets.put(getNameDeclaration(vf), widget);
						widgets_classes.put(getNameDeclaration(vf), variable_class);
					}
				}
				else {
					resolveDeclaration(vf, variable_class);
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
			name = aux_name;
			if(!variables.containsKey(aux_name)){
				throw new IllegalAccessException(aux_name);
			}
		}
		if(controls.containsKey(name)){
			object = controls.get(name);
		}
		else if(widgets.containsKey(name)){
			object = widgets.get(name);
		}
		else if(variables.containsKey(name)){
			object = variables.get(name);
		}
		else{
			if(!node.toString().contains("="))
				resolveNewVariable(variable_class, name, null);
			else
				resolveNewVariableAssignment(variable_class, name, null);
		}
		if(expression.matches(Regex.newArguments)){
			VariableDeclarationStatementVisitor variable_visitor = new VariableDeclarationStatementVisitor();
			node.accept(variable_visitor);
			ClassInstanceCreation c_node = variable_visitor.getNode();
			if(c_node.arguments().size() == 1 && variable_class.equals(Shell.class)){
				newComposite(name);
				shells.add(name);
			}
			else {
				Class<?> [] class_args = new Class<?> [c_node.arguments().size()];
				for(int i = 0; i != c_node.arguments().size(); i++){
					Object o = c_node.arguments().get(i);
					class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
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
					Constructor<?> constr = findCompatibleConstructor(variable_class, class_args);
					variable = constr.newInstance(objects);
				}
				if(object == null){
					resolveAssignmentNull(name, variable);
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(variable));
				}
				else setObject(name, variable);
			}
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
				if(object == null){
					resolveAssignmentNull(name, getObjectMethod(m_node, aux_name, m_node.getName().toString()));
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(getObjectMethod(m_node, objectMethodName, m_node.getName().toString())));
				}
				else setObject(name,getObjectMethod(m_node, aux_name, m_node.getName().toString()));
			}
			else{
				if(object == null){
					resolveAssignmentNull(name, getObjectMethod(m_node, objectMethodName, m_node.getName().toString()));
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(getObjectMethod(m_node, objectMethodName, m_node.getName().toString())));
				}
				else setObject(name, getObjectMethod(m_node, objectMethodName, m_node.getName().toString()));
			}
		}
		else if(expression.matches(Regex.constantDeclarations) || expression.matches(Regex.constantArrayDeclarations)){
			ConstantStatementVisitor visitor = new ConstantStatementVisitor();
			node.accept(visitor);
			QualifiedName constant = visitor.getQualifiedName();
			Object variable = getConstantObject(constant);
			if(object == null){
				resolveAssignmentNull(name, variable);
			}
			else if(index != -1){
				Array.set(getObject(name), index, variable_class.cast(variable));
			}
			else setObject(name,variable);
		}
		else if(expression.matches(Regex.newArgumentsArray)){
			Object variable = resolveNewArgumentsArray(node, variable_class);
			if(object == null){
				resolveAssignmentNull(name, variable);
			}
			else setObject(name,variable);
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
				if(object == null){
					resolveAssignmentNull(name, Array.get(variables.get(aux_name), (int) index));
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(Array.get(variables.get(aux_name), (int) index)));
				}
				else setObject(name,Array.get(variables.get(aux_name), (int) index));
			}
			else if(variables.containsKey(expression.toString())){
				Object variable = variables.get(expression.toString());
				if(object == null){
					resolveAssignmentNull(name, variable);
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(variable));
				}
				else setObject(name,variable);
			}
			else if(controls.containsKey(expression.toString())){
				Object variable = controls.get(expression.toString());
				if(object == null){
					resolveAssignmentNull(name, variable);
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(variable));
				}
				else setObject(name, variable);
			}
			else if(widgets.containsKey(expression.toString())){
				Object variable = widgets.get(expression.toString());
				if(object == null){
					resolveAssignmentNull(name, variable);
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(variable));
				}
				else setObject(name, variable);
			}
			else {
				Object variable = findCompatibleConstructor(variable_class, String.class).newInstance(expression.toString().replaceAll("\"", ""));
				if(object == null){
					resolveAssignmentNull(name, variable);
				}
				else if(index != -1){
					Array.set(getObject(name), index, variable_class.cast(variable));
				}
				else setObject(name,variable);
				System.out.println("ENTREI * 2 " + name + " --- " + variables.get(name) + " --- " +  expression.toString() + " --- " + variable);
			}
		}
	}

	private void setObject(String name, Object new_object){
		if(controls.containsKey(name)){
			controls.put(name, (Control)new_object);
		}
		else if(widgets.containsKey(name)){
			widgets.put(name, (Widget)new_object);
		}
		else if(variables.containsKey(name)){
			variables.put(name, new_object);
		}
		else{
			throw new NoSuchElementException(name);
		}
	}

	private Object getObject(String name) {
		if(controls.containsKey(name)){
			return controls.get(name);
		}
		else if(widgets.containsKey(name)){
			return widgets.get(name);
		}
		else if(variables.containsKey(name)){
			return variables.get(name);
		}
		return null;
	}

	private Composite newComposite(String name) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Class<?>[] params = new Class<?>[2];
		params[0] = Composite.class;
		params[1] = int.class;
		Object[] objects = new Object[2];
		objects[0] = composite;
		objects[1] = SWT.NONE;
		controls.put(name, (Control)findCompatibleConstructor(Composite.class, params).newInstance(objects));
		controls_classes.put(name, Composite.class);
		((Composite)controls.get(name)).setLayout(MenuLayoutVariables.getInstance().getLayout());
		return (Composite) controls.get(name);
	}

	private void resolveNewVariable(Class<?> variable_class, String name, Object object) {
		if(object == null && Widget.class.isAssignableFrom(variable_class)){
			if(widgets.containsKey(name) || controls.containsKey(name)){
				resolveNewVariableAssignment(variable_class, name, object);
			}
		}
		else { 
			resolveNewVariableAssignment(variable_class, name, object);
		}
	}

	private void resolveNewVariableAssignment(Class<?> variable_class, String name, Object object) {
		if(Composite.class.isAssignableFrom(variable_class)){
			controls.put(name, (Control)object);
			controls_classes.put(name, variable_class);
		}
		else if(Widget.class.isAssignableFrom(variable_class)){
			widgets.put(name, (Widget)object);
			widgets_classes.put(name, variable_class);
		}
		else{
			variables.put(name, object);
			variables_classes.put(name, variable_class);
		}
	}

	private void resolveAssignmentNull(String name, Object variable) {
		if(controls.containsKey(name)){
			controls.put(name, (Control)variable);
		}
		else if(widgets.containsKey(name)){
			widgets.put(name, (Widget)variable);
		}
		else if(variables.containsKey(name)){
			variables.put(name, variable);
		}
	}

	private void resolveMethod(MethodInvocation mi, String objectName, String methodName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, ArrayIndexOutOfBoundsException, NoSuchFieldException {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		Object object = null;
		Class<?> methodClass = null;
		System.out.println("GAGAGAG " + objectName);
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

		// PARA FAZER
		Method m = findCompatibleMethod(methodName, methodClass, class_args);
		Object[] objects = new Object[mi.arguments().size()];
		for(int i = 0; i != class_args.length; i++){
			Expression exp = (Expression)mi.arguments().get(i);
			objects[i] = resolveType(class_args, exp, i); 
		}
		if(m == null){
			m = methodClass.getDeclaredMethod(methodName, class_args);
		}
		try{
			m.invoke(object, objects);
		}
		catch(Error | Exception e){
			throw new NoSuchMethodException(methodName);
		}
	}

	private Method findCompatibleMethod(String methodName, Class<?> methodClass,
			Class<?>[] class_args) {
		System.out.println(methodClass + " --- " + Arrays.toString(methodClass.getMethods()));
		for(Method m : methodClass.getMethods()) {
			if(m.getName().equals(methodName) && checkParams(m, class_args))
				return m;
		}
		return null;
	}

	private boolean checkParams(Method m, Class<?>[] class_args) {
		Class<?>[] params = m.getParameterTypes();
		if(params.length != class_args.length)
			return false;

		for(int i = 0; i < params.length; i++)
			if(!params[i].isAssignableFrom(class_args[i]))
				return false;

		return true;
	}

	private Constructor<?> findCompatibleConstructor(Class<?> constructorClass,
			Class<?>... class_args) {
		if(constructorClass.isPrimitive()){
			constructorClass = primitive_classes.get(constructorClass);
		}
		Class<?>[] params_aux = class_args.clone();
		for(int i = 0; i != params_aux.length; i++){
			if(primitive_classes_inverse.containsKey(params_aux[i])){
				params_aux[i] = primitive_classes_inverse.get(params_aux[i]);
			}
		}
		for(Constructor<?> c : constructorClass.getConstructors()) {
			if(checkParamsConstructor(c, params_aux))
				return c;
		}
		return null;
	}

	private boolean checkParamsConstructor(Constructor<?> c, Class<?>[] class_args) {
		Class<?>[] params = c.getParameterTypes();
		if(params.length != class_args.length)
			return false;

		for(int i = 0; i < params.length; i++)
			if(!params[i].isAssignableFrom(class_args[i]))
				return false;

		return true;
	}

	private Object getObjectMethod(MethodInvocation mi, String objectName, String methodName) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, ArrayIndexOutOfBoundsException, NoSuchFieldException, IllegalArgumentException {
		Class<?> [] class_args = new Class<?> [mi.arguments().size()];
		Object object = null;
		Class<?> methodClass = null;
		if(objectName.matches(Regex.arrayAccess)){
			ArrayAccessVisitor aac_visitor = new ArrayAccessVisitor();
			mi.accept(aac_visitor);
			String aux_name = aac_visitor.getNode().getArray().toString();
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
			Class<?> staticClass = resolveClass(mi.resolveMethodBinding().getDeclaringClass().getQualifiedName());
			methodClass = staticClass;
			//			resolveStaticMethod();
		}
		for(int i = 0; i != mi.arguments().size(); i++){
			Object o = mi.arguments().get(i);
			class_args[i] = resolveClass(((Expression)o).resolveTypeBinding().getQualifiedName());
		}

		Method m = findCompatibleMethod(methodName, methodClass, class_args);
		Object[] objects = new Object[mi.arguments().size()];
		for(int i = 0; i != class_args.length; i++){
			Expression exp = (Expression)mi.arguments().get(i);
			objects[i] = resolveType(class_args, exp, i); 
		}
		if(m == null){
			m = methodClass.getDeclaredMethod(methodName, class_args);
		}
		try {
			return m.invoke(object, objects);
		}
		catch(Error | Exception e){
			if(resolveClass(mi.resolveMethodBinding().getReturnType().getQualifiedName()).equals(String.class)){
				return findCompatibleConstructor(String.class, String.class).newInstance(mi.toString());
			}
			else throw new NoSuchMethodException(methodName);
		}
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
			aux_class = resolveClass(aux_name.replace("[]", ""));
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
		return findCompatibleConstructor(c, class_object_args).newInstance(objects1);
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
				System.out.println("EADADADADAAAAAAAAAAAAAAAAAAAAAA " + argument_expression.toString());
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
						if(argument_expression.toString().matches(Regex.operatorsDeclarations)){
							return resolveOperations(argument_expression);
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
							else if(widgets.containsKey(objectName)){
								return widgets.get(objectName);
							}
							else if(controls.containsKey(objectName)){
								return controls.get(objectName);
							}
							else if(variables.containsKey(objectName)){
								return variables.get(objectName);
							}
							else
								if(Composite.class.isAssignableFrom(class_object_args[j])){
									Class<?>[] params = new Class<?>[2];
									params[0] = Composite.class;
									params[1] = int.class;
									Object[] objects = new Object[2];
									objects[0] = composite;
									objects[1] = SWT.NONE;
									if(class_object_args[j].equals(Shell.class)){
										class_object_args[j] = Composite.class;
										controls.put(argument_expression.toString(), (Control)findCompatibleConstructor(Composite.class, params).newInstance(objects));
										controls_classes.put(argument_expression.toString(), Composite.class);
										shells.add(argument_expression.toString());
									}
									else {
										controls.put(argument_expression.toString(), (Control)findCompatibleConstructor(class_object_args[j], params).newInstance(objects));
										controls_classes.put(argument_expression.toString(), class_object_args[j]);
									}
									((Composite)controls.get(argument_expression.toString())).setLayout(MenuLayoutVariables.getInstance().getLayout());
									return controls.get(argument_expression.toString());
									//									return composite;
								}
								else if(argument_expression.toString().equals("null"))
									return null;
								else {
									System.out.println("OLAAAAAA " + class_object_args[j].toString() + " --- " + argument_expression.toString());
									Object aux = findCompatibleConstructor(class_object_args[j], String.class).newInstance(argument_expression.toString().replaceAll("\"", ""));
									if(aux == null)
										throw new IllegalArgumentException(argument_expression.toString());
									return aux;
								}
						}
					}
				}
			}
		}
	}

	private Object resolveOperations(Expression argument_expression) throws ArrayIndexOutOfBoundsException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, SecurityException, IllegalArgumentException {
		InfixVisitor visitor = new InfixVisitor();
		argument_expression.accept(visitor);
		InfixExpression node = visitor.getNode();
		Operator operator = node.getOperator();
		Object result = null;
		switch(operator.toString()){
		case "&":
			Class<?>[] aux_left_and = new Class<?>[1];
			aux_left_and[0] = resolveClass(node.getLeftOperand().resolveTypeBinding().getQualifiedName());
			Class<?>[] aux_right_and = new Class<?>[1];
			aux_right_and[0] = resolveClass(node.getRightOperand().resolveTypeBinding().getQualifiedName());
			result = (int)resolveType(aux_left_and, node.getLeftOperand(), 0) & (int)resolveType(aux_right_and, node.getRightOperand(), 0);
			if(node.hasExtendedOperands()){
				for(Object o : node.extendedOperands()){
					Expression exp = (Expression) o;
					Class<?>[] aux_extra_and = new Class<?>[1];
					aux_extra_and[0] = resolveClass(exp.resolveTypeBinding().getQualifiedName());
					result = (int)result & (int)resolveType(aux_extra_and, exp, 0);
				}
			}
			break;
		case "|":
			Class<?>[] aux_left_or = new Class<?>[1];
			aux_left_or[0] = resolveClass(node.getLeftOperand().resolveTypeBinding().getQualifiedName());
			Class<?>[] aux_right_or = new Class<?>[1];
			aux_right_or[0] = resolveClass(node.getRightOperand().resolveTypeBinding().getQualifiedName());
			result = (int)resolveType(aux_left_or, node.getLeftOperand(), 0) | (int)resolveType(aux_right_or, node.getRightOperand(), 0);
			if(node.hasExtendedOperands()){
				for(Object o : node.extendedOperands()){
					Expression exp = (Expression) o;
					Class<?>[] aux_extra_or = new Class<?>[1];
					aux_extra_or[0] = resolveClass(exp.resolveTypeBinding().getQualifiedName());
					result = (int)result | (int)resolveType(aux_extra_or, exp, 0);
				}
			}
			break;
		}
		return result;
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

	//	private Constructor<?> getConstructor(Class <?> c, Class<?>... params) throws NoSuchMethodException, SecurityException{
	//		if(c.isPrimitive()){
	//			c = primitive_classes.get(c);
	//		}
	//		Class<?>[] params_aux = params.clone();
	//		for(int i = 0; i != params_aux.length; i++){
	//			if(primitive_classes_inverse.containsKey(params_aux[i])){
	//				params_aux[i] = primitive_classes_inverse.get(params_aux[i]);
	//			}
	//		}
	//		return c.getConstructor(params_aux);
	//	}

	private String getNameDeclaration(VariableDeclarationFragment vf) {
		return vf.getName().toString();
	}

	private Class<?> resolveClass(String classpath) throws ClassNotFoundException{
		for(Class<?> c : primitive_classes.keySet()){
			if(c.getName().equals(classpath))
				return c;
		}
		try {
			return Class.forName(classpath);
		} catch (ClassNotFoundException e) {
			Class<?> clazz = ClassLoading.getInstance().getClass(classpath);
			if(clazz == null){
				throw new ClassNotFoundException(classpath);
			}
			else return clazz;
		}
	}

	@Override
	public void dispose() {

		super.dispose();
	}

}
