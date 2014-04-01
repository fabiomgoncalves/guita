package org.eclipselabs.guita.ipreviews.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipselabs.guita.ipreviews.regex.Regex;

public class VObtainerVisitor extends ASTVisitor {

	Map<String, Class<?>> primitive_classes;

	Map<String, ASTNode> variables = new HashMap<String, ASTNode>();
	Map<String, List<ASTNode>> variables_methods = new HashMap<String, List<ASTNode>>();
	Map<String, List<ASTNode>> variables_assigns = new HashMap<String, List<ASTNode>>();

	Map<String, ASTNode> nodes = new HashMap<String, ASTNode>();

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		VariableDeclarationFragment vf = (VariableDeclarationFragment) node.fragments().get(0);
		String name = vf.getName().toString();
		variables.put(name, vf);
		variables_methods.put(name, new ArrayList<ASTNode>());
		variables_assigns.put(name, new ArrayList<ASTNode>());
		primitive_classes = new HashMap<String, Class<?>>();
		primitive_classes.put("byte", byte.class);
		primitive_classes.put("short", short.class);
		primitive_classes.put("int", int.class);
		primitive_classes.put("long", long.class);
		primitive_classes.put("float", float.class);
		primitive_classes.put("double", double.class);
		primitive_classes.put("boolean", boolean.class);
		primitive_classes.put("char", char.class);

		ITypeBinding binding = node.getType().resolveBinding();
		Class<?> variable_class = null;

		try {
			String class_name = binding.getQualifiedName();
			if(binding.getQualifiedName().contains("[]"))
				class_name = "[L" + class_name.replace("[]","") + ";";
			if(primitive_classes.containsKey(class_name)){
				variable_class = primitive_classes.get(class_name);
			}
			else variable_class = Class.forName(class_name);
			if(variable_class.equals(Display.class)){
				variables.remove(name);
				variables_methods.remove(name);
				variables_assigns.remove(name);
			}
			if(Composite.class.isAssignableFrom(variable_class) || Widget.class.isAssignableFrom(variable_class)){
				nodes.put(name, vf);
				variables.remove(name);
				String full_args = node.toString().substring(node.toString().indexOf("=") + 1).trim().replaceAll(";", "");
				resolveType(node, full_args);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return super.visit(node);
	}

	private void analyseVariable(String variable_name) {
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
			variables_methods.remove(assigns);
			for(ASTNode an : assigns){
				String full_args = an.toString().substring(an.toString().indexOf("=") + 1).trim().replaceAll(";", "");
				resolveType(an, full_args);
			}
		}
	}


	private void resolveType(ASTNode an, String full_args) {
		if(full_args.matches(Regex.newArguments)){
			analyseStringNewStatement(an);
		}
		else{
			if(full_args.matches(Regex.methodDeclarations)){
				analyseStringMethod(an);
			}
			else{
				if(full_args.matches(Regex.constantDeclarations)){
					analyseStringConstant(an);
				}
				else if(variables.containsKey(full_args)){
					nodes.put(full_args, variables.get(full_args));
					variables.remove(full_args);
					analyseVariable(full_args);
				}
			}
		}
	}

	private void analyseStringConstant(ASTNode an) {
		String objectName = an.toString().substring(0, an.toString().indexOf("."));
		Iterator<String> it = variables.keySet().iterator();
		while(it.hasNext()){
			String s = it.next();
			if(objectName.equals(s)){
				nodes.put(s, variables.get(s));
				it.remove();
				analyseVariable(s);
			}
		}
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if(node.getParent().getClass().equals(ExpressionStatement.class)){
			String objectName = node.toString().substring(0, node.toString().indexOf("."));
			if(variables_methods.containsKey(objectName)){
				variables_methods.get(objectName).add(node);
			}
			if(nodes.containsKey(objectName)){
				resolveType(node, node.toString());
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		String objectName = node.toString().replaceAll(" ", "").substring(0, node.toString().indexOf("="));
		if(variables_assigns.containsKey(objectName)){
			variables_assigns.get(objectName).add(node);
		}
		if(nodes.containsKey(objectName)){
			resolveType(node, node.toString().substring(node.toString().indexOf("=")).trim());
		}
		return super.visit(node);
	}

	private void analyseStringNewStatement(ASTNode node) {
		NewStatementVisitor visitor = new NewStatementVisitor();
		node.accept(visitor);
		for(Object o : visitor.getNew_argument_classes().get(0).arguments()){
			ASTNode exp = (ASTNode) o;
			if(exp.toString().matches(Regex.newArguments)){
				analyseStringNewStatement(exp);
			}
			else{
				if(exp.toString().matches(Regex.methodDeclarations)){
					analyseStringMethod(exp);
				}
				else {
					if(exp.toString().matches(Regex.constantDeclarations)){
						analyseStringConstant(exp);
					}
					else {
						Iterator<String> it = variables.keySet().iterator();
						while(it.hasNext()){
							String s = it.next();
							if(exp.toString().equals(s)){
								nodes.put(s, variables.get(s));
								it.remove();
								analyseVariable(s);
							}
						}
					}
				}
			}
		}
	}

	private void analyseStringMethod(ASTNode node) {
		MethodInvocationVisitor visitor = new MethodInvocationVisitor();
		node.accept(visitor);
		Iterator<String> it = variables.keySet().iterator();
		while(it.hasNext()){
			String s = it.next();
			String name = node.toString().substring(0,node.toString().indexOf("."));
			if(name.contains("=")){
				name = name.substring(name.indexOf("=") + 1);
			}
			if(name.equals(s)){
				nodes.put(s, variables.get(s));
				it.remove();
				analyseVariable(s);
			}
		}
		for(Object o : visitor.getNode().arguments()){
			ASTNode exp = (ASTNode) o;
			if(exp.toString().matches(Regex.newArguments)){
				analyseStringNewStatement(exp);
			}
			else{
				if(exp.toString().matches(Regex.methodDeclarations)){
					analyseStringMethod(exp);
				}
				else {
					if(exp.toString().matches(Regex.constantDeclarations)){
						analyseStringConstant(exp);
					}
					else {
						Iterator<String> it2 = variables.keySet().iterator();
						while(it2.hasNext()){
							String s = it2.next();
							if(exp.toString().equals(s)){
								nodes.put(s, variables.get(s));
								it2.remove();
								analyseVariable(s);
							}
						}
					}
				}
			}
		}
	}

	public Map<String, ASTNode> getNodes() {
		return nodes;
	}

}
