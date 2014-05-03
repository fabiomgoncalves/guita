package org.eclipselabs.guita.ipreviews.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipselabs.guita.ipreviews.regex.Regex;
import org.eclipselabs.guita.ipreviews.view.ClassLoading;
import org.eclipselabs.guita.ipreviews.view.PreviewView;
import org.eclipselabs.guita.ipreviews.view.SupportClasses;

public class VObtainerVisitor extends ASTVisitor {

	Map<String, ASTNode> variables = new HashMap<String, ASTNode>();
	Map<String, List<ASTNode>> variables_methods = new HashMap<String, List<ASTNode>>();
	Map<String, List<ASTNode>> variables_assigns = new HashMap<String, List<ASTNode>>();

	Map<String, Class<?>> primitive_classes = new SupportClasses().primitive_classes_string;

	Map<String, ASTNode> nodes = new HashMap<String, ASTNode>();

	private ASTNode block_to_visit;

	private int firstLine;
	private int lastLine;

	public VObtainerVisitor(int firstLine, int lastLine) {
		this.firstLine = firstLine;
		this.lastLine = lastLine;
	}


	private boolean withinSelection(ASTNode node) {
		CompilationUnit unit = (CompilationUnit) node.getRoot();
		int start = unit.getLineNumber(node.getStartPosition());
		int end = unit.getLineNumber(node.getStartPosition() + node.getLength());
		return start >= firstLine && end <= lastLine;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		VariableDeclarationFragment vf = (VariableDeclarationFragment)node.fragments().get(0);
		String name = ((VariableDeclarationFragment)node.fragments().get(0)).getName().toString();
		ITypeBinding binding = vf.resolveBinding().getType();
		String class_name = binding.getQualifiedName();
		Class<?> variable_class = null;
		try {
			if(binding.getQualifiedName().contains("[]") && !primitive_classes.containsKey(binding.getQualifiedName()))
				class_name = "[L" + class_name.replace("[]","") + ";";
			if(primitive_classes.containsKey(class_name)){
				variable_class = primitive_classes.get(class_name);
			}
			else variable_class = resolveClass(class_name);
			variables.put(name, vf);
			variables_methods.put(name, new ArrayList<ASTNode>());
			variables_assigns.put(name, new ArrayList<ASTNode>());
			if(variable_class.equals(Display.class)){
				variables.remove(name);
				variables_methods.remove(name);
				variables_assigns.remove(name);
			}
			if(Control.class.isAssignableFrom(variable_class) || Control[].class.isAssignableFrom(variable_class)|| Widget.class.isAssignableFrom(variable_class) || Widget[].class.isAssignableFrom(variable_class)){
				nodes.put(name, vf);
				variables.remove(name);
				if(node.toString().contains("=")){
					String full_args = node.toString().substring(node.toString().indexOf("=") + 1).trim().replaceAll(";", "");
					resolveType(node, full_args);
				}
			}
		}
		catch (ClassNotFoundException e) {
			if(PreviewView.devMode)
				e.printStackTrace();
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if(!withinSelection(node))
			return false;

		createBlockToVisit(node);

		if(ifBlockToVisit(node)){

			VariableDeclarationFragment vf = (VariableDeclarationFragment) node.fragments().get(0);
			String name = vf.getName().toString();

			ITypeBinding binding = node.getType().resolveBinding();
			Class<?> variable_class = null;

			try {
				String class_name = binding.getQualifiedName();
				if(binding.getQualifiedName().contains("[]") && !primitive_classes.containsKey(binding.getQualifiedName()))
					class_name = "[L" + class_name.replace("[]","") + ";";
				if(primitive_classes.containsKey(class_name)){
					variable_class = primitive_classes.get(class_name);
				}
				else variable_class = resolveClass(class_name);
				variables.put(name, vf);
				variables_methods.put(name, new ArrayList<ASTNode>());
				variables_assigns.put(name, new ArrayList<ASTNode>());
				if(variable_class.equals(Display.class)){
					variables.remove(name);
					variables_methods.remove(name);
					variables_assigns.remove(name);
				}
				if(Control.class.isAssignableFrom(variable_class) || Control[].class.isAssignableFrom(variable_class)|| Widget.class.isAssignableFrom(variable_class) || Widget[].class.isAssignableFrom(variable_class)){
					nodes.put(name, vf);
					variables.remove(name);
					if(node.toString().contains("=")){
						String full_args = node.toString().substring(node.toString().indexOf("=") + 1).trim().replaceAll(";", "");
						resolveType(node, full_args);
					}
				}
			} catch (ClassNotFoundException e) {
				if(PreviewView.devMode)
					e.printStackTrace();
			}
			return super.visit(node);
		}
		else return false;
	}


	@Override
	public boolean visit(Assignment node) {
		if(!withinSelection(node))
			return false;

		createBlockToVisit(node);

		if(ifBlockToVisit(node)){
			boolean arrayAccess = false;
			String objectName;
			if(node.getLeftHandSide().toString().matches(Regex.arrayAccess)){
				objectName = node.toString().substring(0, node.toString().indexOf("["));
				if(nodes.containsKey(objectName)){
					ArrayAccessVisitor visitor = new ArrayAccessVisitor();
					node.accept(visitor);
					ArrayAccess aux_node = visitor.getNode();
					resolveType(aux_node.getIndex(), aux_node.getIndex().toString());
				}
				arrayAccess = true;
			}
			else objectName = node.getLeftHandSide().toString();
			if(variables.containsKey(objectName)){
				variables_assigns.get(objectName).add(node);
			}
			else if(nodes.containsKey(objectName)){
				if(node.getLeftHandSide().toString().matches(Regex.arrayAccess)){
					arrayAnalyse(node.getLeftHandSide());
				}
				resolveType(node, node.getRightHandSide().toString());
			}
			else {
				if(!arrayAccess){
					ITypeBinding binding = node.resolveTypeBinding();
					Class<?> variable_class = null;
					try {
						String class_name = binding.getQualifiedName();
						if(binding.getQualifiedName().contains("[]") && !primitive_classes.containsKey(binding.getQualifiedName()))
							class_name = "[L" + class_name.replace("[]","") + ";";
						if(primitive_classes.containsKey(class_name)){
							variable_class = primitive_classes.get(class_name);
						}
						else variable_class = resolveClass(class_name);
						variables.put(objectName, node);
						variables_methods.put(objectName, new ArrayList<ASTNode>());
						variables_assigns.put(objectName, new ArrayList<ASTNode>());
						if(variable_class.equals(Display.class)){
							variables.remove(objectName);
							variables_methods.remove(objectName);
							variables_assigns.remove(objectName);
						}
						if(Control.class.isAssignableFrom(variable_class) || Control[].class.isAssignableFrom(variable_class)|| Widget.class.isAssignableFrom(variable_class) || Widget[].class.isAssignableFrom(variable_class)){
							nodes.put(objectName, node);
							variables.remove(objectName);
							if(node.toString().contains("=")){
								String full_args = node.toString().substring(node.toString().indexOf("=") + 1).trim().replaceAll(";", "");
								resolveType(node, full_args);
							}
						}
					}catch (ClassNotFoundException e){
						if(PreviewView.devMode)
							e.printStackTrace();
					}
				}
			}
			return super.visit(node);
		}
		else return false;
	}


	@Override
	public boolean visit(MethodInvocation node) {
		if(!withinSelection(node))
			return false;
		createBlockToVisit(node);
		if(ifBlockToVisit(node)){
			if(node.toString().matches(Regex.methodDeclarations) || node.toString().matches(Regex.methodArrayDeclarations)){
				if(node.getParent().getClass().equals(ExpressionStatement.class)){
					String objectName;
					if(node.toString().matches(Regex.methodArrayDeclarations))
						objectName = node.toString().substring(0, node.toString().indexOf("["));
					else objectName = node.toString().substring(0, node.toString().indexOf("."));
					if(variables_methods.containsKey(objectName)){
						variables_methods.get(objectName).add(node);
					}
					if(nodes.containsKey(objectName)){
						if(node.toString().matches(Regex.methodArrayDeclarations)){
							arrayAnalyse(node.getExpression());
						}
						resolveType(node, node.toString());
					}
				}
			}
			return super.visit(node);
		}
		else return false;
	}

	private boolean ifBlockToVisit(ASTNode node) {
		boolean stop = false;
		ASTNode test = node.getParent();
		while(!stop){
			if(test == null)
				stop = true;
			else if(test.getParent() == null){
				return block_to_visit.equals(test);
			}
			else test = test.getParent();
		}
		return false;
	}


	private void createBlockToVisit(ASTNode node) {
		if(block_to_visit == null){
			boolean stop = false;
			ASTNode test = node.getParent();
			while(!stop){
				if(test == null)
					stop = true;
				else if(test.getParent() == null){
					System.out.println("JAKAKAKAKAKAKA " + test + " --- " + node);
					block_to_visit = test;
					stop = true;
				}
				else test = test.getParent();
			}
		}
	}


	private void analyseVariable(String variable_name) {
		String args;
		if(nodes.get(variable_name).toString().contains("=")){
			args = nodes.get(variable_name).toString().substring(nodes.get(variable_name).toString().indexOf("=") + 1).trim().replaceAll(";", "");
		}
		else args = nodes.get(variable_name).toString();
		resolveType(nodes.get(variable_name), args);
		if(variables_methods.containsKey(variable_name)){
			List<ASTNode> methods = variables_methods.get(variable_name);
			variables_methods.remove(variable_name);
			for(ASTNode an : methods){
				String full_args = an.toString();
				resolveType(an, full_args);
			}
		}
		if(variables_assigns.containsKey(variable_name)){
			List<ASTNode> assigns = variables_assigns.get(variable_name);
			variables_assigns.remove(variable_name);
			for(ASTNode an : assigns){
				Assignment aux = ((Assignment) an);
				if(aux.getLeftHandSide().toString().matches(Regex.arrayAccess)){
					ArrayAccessVisitor visitor = new ArrayAccessVisitor();
					an.accept(visitor);
					ArrayAccess aux_node = visitor.getNode();
					resolveType(aux_node.getIndex(), aux_node.getIndex().toString());
				}
				String full_args = aux.getRightHandSide().toString();
				resolveType(an, full_args);
			}
		}
	}


	private void resolveType(ASTNode an, String full_args) {
		if(full_args.matches(Regex.newArguments) || full_args.matches(Regex.newArgumentsArray)){
			analyseStringNewStatement(an);
		}
		else if(full_args.matches(Regex.methodDeclarations) || full_args.toString().matches(Regex.methodArrayDeclarations)){
			analyseStringMethod(an);
		}
		else if(full_args.matches(Regex.constantDeclarations) || full_args.toString().matches(Regex.constantArrayDeclarations)){
			analyseStringConstant(an);
		}
		else if(full_args.matches(Regex.arrayAccess)){
			ArrayAccessVisitor visitor = new ArrayAccessVisitor();
			an.accept(visitor);
			resolveType(visitor.getNode().getIndex(), visitor.getNode().getIndex().toString());
			String aux = visitor.getNode().getArray().toString();
			if(variables.containsKey(aux)){
				nodes.put(aux, variables.get(aux));
				variables.remove(aux);
				analyseVariable(aux);
			}
		}
		else if(full_args.matches(Regex.operatorsDeclarations)){
			analyseOperations(an);
		}
		else if(variables.containsKey(full_args)){
			nodes.put(full_args, variables.get(full_args));
			variables.remove(full_args);
			analyseVariable(full_args);
		}
	}

	private void analyseOperations(ASTNode an) {
		InfixVisitor visitor = new InfixVisitor();
		an.accept(visitor);
		InfixExpression node = visitor.getNode();
		resolveType(node.getLeftOperand(), node.getLeftOperand().toString());
		resolveType(node.getRightOperand(), node.getRightOperand().toString());
		if(node.hasExtendedOperands()){
			for(Object o : node.extendedOperands()){
				Expression exp = (Expression) o;
				resolveType(exp, exp.toString());
			}
		}

	}

	private void analyseStringConstant(ASTNode an) {
		ArrayAccessVisitor a_visitor = new ArrayAccessVisitor();
		an.accept(a_visitor);
		String objectName;
		if(a_visitor.getNode() != null && an.toString().matches(Regex.constantArrayDeclarations)){
			objectName = a_visitor.getNode().getArray().toString();
			analyseVariable(a_visitor.getNode().getIndex().toString());
		}
		else objectName = an.toString().substring(0, an.toString().indexOf("."));
		List<String> newVariables = new ArrayList<String>();
		Iterator<String> it = variables.keySet().iterator();
		analyseValue2(newVariables, it, objectName);
	}

	private void arrayAnalyse(Expression exp){
		ArrayAccessVisitor a_visitor = new ArrayAccessVisitor();
		exp.accept(a_visitor);
		if(a_visitor.getNode() != null){
			resolveType(a_visitor.getNode().getIndex(), a_visitor.getNode().getIndex().toString());
		}
	}

	private void analyseStringNewStatement(ASTNode node) {
		String full_args;
		if(node.toString().contains("="))
			full_args = node.toString().substring(node.toString().indexOf("=") + 1).trim().replace(";", "");
		else full_args = node.toString();
		if(full_args.matches(Regex.newArgumentsArray)){
			ArrayInitVisitor ai_visitor = new ArrayInitVisitor();
			node.accept(ai_visitor);
			ArrayCreation ac_node = ai_visitor.getNode();
			for(Object o : ac_node.dimensions()){
				Expression exp = (Expression) o;
				resolveType(exp, exp.toString());
			}
		}
		else {
			NewStatementVisitor visitor = new NewStatementVisitor();
			node.accept(visitor);
			for(Object o : visitor.getNew_argument_classes().get(0).arguments()){
				ASTNode exp = (ASTNode) o;
				if(exp.toString().matches(Regex.newArguments) ||  exp.toString().matches(Regex.newArgumentsArray)){
					analyseStringNewStatement(exp);
				}
				else{
					if(exp.toString().matches(Regex.methodDeclarations) || exp.toString().matches(Regex.methodArrayDeclarations)){
						analyseStringMethod(exp);
					}
					else {
						if(exp.toString().matches(Regex.constantDeclarations) || exp.toString().matches(Regex.constantArrayDeclarations)){
							analyseStringConstant(exp);
						}
						else {
							analyseValue(exp);
						}
					}
				}
			}
		}
	}

	private void analyseStringMethod(ASTNode node) {
		MethodInvocationVisitor visitor = new MethodInvocationVisitor();
		node.accept(visitor);
		ArrayAccessVisitor a_visitor = new ArrayAccessVisitor();
		node.accept(a_visitor);
		String name;
		if(a_visitor.getNode() != null && node.toString().matches(Regex.methodArrayDeclarations)){
			name = a_visitor.getNode().getArray().toString();
			resolveType(a_visitor.getNode().getIndex(), a_visitor.getNode().getIndex().toString());
		}
		else name = node.toString().substring(0,node.toString().indexOf("."));
		Iterator<String> it = variables.keySet().iterator();
		List<String> newVariables = new ArrayList<String>();
		while(it.hasNext()){
			String s = it.next();
			if(name.contains("=")){
				name = name.substring(name.indexOf("=") + 1);
			}
			if(name.equals(s)){
				nodes.put(s, variables.get(s));
				it.remove();
				newVariables.add(s);
			}
		}
		for(String s : newVariables){
			analyseVariable(s);
		}
		for(Object o : visitor.getNode().arguments()){
			ASTNode exp = (ASTNode) o;
			if(exp.toString().matches(Regex.newArguments) || exp.toString().matches(Regex.newArgumentsArray)){
				analyseStringNewStatement(exp);
			}
			else{
				if(exp.toString().matches(Regex.methodDeclarations) || exp.toString().matches(Regex.methodArrayDeclarations)){
					analyseStringMethod(exp);
				}
				else {
					if(exp.toString().matches(Regex.constantDeclarations) || exp.toString().matches(Regex.constantArrayDeclarations) ){
						analyseStringConstant(exp);
					}
					else {
						analyseValue(exp);
					}
				}
			}
		}
	}

	private void analyseValue(ASTNode exp) {
		List<String> newVariables;
		Iterator<String> it = variables.keySet().iterator();
		newVariables = new ArrayList<String>();
		String objectName;
		if(exp.toString().matches(Regex.arrayAccess)){
			objectName = exp.toString().substring(0, exp.toString().indexOf("["));
			ArrayAccessVisitor visitor = new ArrayAccessVisitor();
			exp.accept(visitor);
			resolveType(visitor.getNode().getIndex(),visitor.getNode().getIndex().toString());
		}
		else objectName = exp.toString();
		analyseValue2(newVariables, it, objectName);
	}

	private void analyseValue2(List<String> newVariables, Iterator<String> it, String objectName) {
		while(it.hasNext()){
			String s = it.next();
			if(objectName.equals(s)){
				nodes.put(s, variables.get(s));
				it.remove();
				newVariables.add(s);
			}
		}
		for(String s : newVariables){
			analyseVariable(s);
		}
	}

	public Map<String, ASTNode> getNodes() {
		return nodes;
	}

	public Class<?> resolveClass(String classpath) throws ClassNotFoundException{
		try {
			return Class.forName(classpath);
		} catch (ClassNotFoundException e) {
			Class<?> clazz = ResourcesPlugin.getWorkspace().getRoot().getProjects()[1].getClass().getClassLoader().loadClass(classpath);
			if(clazz == null){
				throw new ClassNotFoundException(classpath);
			}
			else return clazz;
		}
	}

}
