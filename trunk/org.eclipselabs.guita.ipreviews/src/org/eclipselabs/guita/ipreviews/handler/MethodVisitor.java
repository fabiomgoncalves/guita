package org.eclipselabs.guita.ipreviews.handler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ResourceBundle.Control;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.swt.widgets.Composite;
import org.eclipselabs.guita.ipreviews.regex.Regex;



public class MethodVisitor extends ASTVisitor {

	Map<String, VariableDeclarationFragment> variables = new HashMap<String, VariableDeclarationFragment>();
	Map<String, VariableDeclarationFragment> variables_used = new HashMap<String, VariableDeclarationFragment>();
	List<VariableDeclarationFragment> swt_variables = new ArrayList<VariableDeclarationFragment>();
	List<MethodInvocation> swt_methods = new ArrayList<MethodInvocation>();
	List<VariableDeclarationStatement> swt_declarations = new ArrayList<VariableDeclarationStatement>();

	@Override
	public boolean visit(Assignment node) {
		System.out.println("Assigment Node: " + node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		variables.put(((VariableDeclarationFragment)node.fragments().get(0)).getName().toString(), (VariableDeclarationFragment)node.fragments().get(0));
		List<String> args = new ArrayList<String>();
		String full_args = node.toString().substring(node.toString().indexOf("=") + 1);
		Class<?> variable_class = null;
		try {
			ITypeBinding binding = node.getType().resolveBinding();

			boolean hasSWTVariable = false;

			variable_class = Class.forName(binding.getQualifiedName());
			if(full_args.trim().replaceAll(";", "").matches(Regex.newArguments)){
				NewStatementVisitor visitor = new NewStatementVisitor();
				node.accept(visitor);
				for(Object o : visitor.getNew_argument_classes().get(0).arguments()){
					Expression exp = (Expression) o;
					if(exp.toString().matches(Regex.newArguments)){
						analyseStringNewStatement(exp, hasSWTVariable);
					}
					else{
						if(exp.toString().matches(Regex.methodDeclarations)){
							analyseStringMethod(exp, hasSWTVariable);
						}
						else {
							for(String s: variables.keySet()){
								if(exp.toString().equals(s)){
									variables_used.put(s, variables.get(s));
								}
							}
							for(VariableDeclarationFragment v : swt_variables){
								if(exp.toString().equals(v.toString())){
									hasSWTVariable = true;
								}
							}
						}
					}
				}
			}
			else variable_class = null;

			for(VariableDeclarationFragment v : swt_variables){	
				if(full_args.contains(v.getName().toString())){
					hasSWTVariable = true;
				}
			}

			if(variable_class != null && !Composite.class.isAssignableFrom(variable_class) && !hasSWTVariable){
				variable_class = null;
			}
			
		} catch (ClassNotFoundException e) {
			variable_class = null;
		}
		
		VariableDeclarationFragment aux = (VariableDeclarationFragment)node.fragments().get(0);
		if(variable_class != null){
			variables.remove(((VariableDeclarationFragment)node.fragments().get(0)).getName().toString());
			swt_variables.add(aux);
			swt_declarations.add(node);
			variable_class = null;
		}
		return super.visit(node);
	}

	private void analyseStringNewStatement(Expression expression, boolean hasSWTVariable) {
		NewStatementVisitor visitor = new NewStatementVisitor();
		expression.accept(visitor);
		for(Object o : visitor.getNew_argument_classes().get(0).arguments()){
			Expression exp = (Expression) o;
			if(exp.toString().matches(Regex.newArguments)){
				analyseStringNewStatement(exp, hasSWTVariable);
			}
			else{
				if(exp.toString().matches(Regex.methodDeclarations)){
					analyseStringMethod(exp, hasSWTVariable);
				}
				else {
					for(String s: variables.keySet()){
						if(exp.toString().equals(s)){
							variables_used.put(s, variables.get(s));
						}
					}
					for(VariableDeclarationFragment v : swt_variables){
						if(exp.toString().equals(v.toString())){
							hasSWTVariable = true;
						}
					}
				}
			}
		}
	}

	private void analyseStringMethod(Expression expression, boolean hasSWTVariable) {
		MethodInvocationVisitor visitor = new MethodInvocationVisitor();
		expression.accept(visitor);
		for(String s: variables.keySet()){
			if(expression.toString().substring(0,expression.toString().indexOf(".")).equals(s)){
				variables_used.put(s, variables.get(s));
			}
		}
		for(Object o : visitor.getNode().arguments()){
			Expression exp = (Expression) o;
			if(exp.toString().matches(Regex.newArguments)){
				analyseStringNewStatement(exp, hasSWTVariable);
			}
			else{
				if(exp.toString().matches(Regex.methodDeclarations)){
					analyseStringMethod(exp, hasSWTVariable);
				}
				else {
					for(String s: variables.keySet()){
						if(exp.toString().equals(s)){
							variables_used.put(s, variables.get(s));
						}
					}
					for(VariableDeclarationFragment v : swt_variables){
						if(exp.toString().equals(v.toString())){
							hasSWTVariable = true;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if(node.toString().indexOf(".") != -1){
			String aux = node.toString().substring(0, node.toString().indexOf("."));
			for(VariableDeclarationFragment v : swt_variables){
				for(Object o : node.arguments()){
					Expression exp = (Expression) o;
					if(exp.toString().matches(Regex.newArguments)){
						analyseStringNewStatement(exp, true);
					}
					else{
						if(exp.toString().matches(Regex.methodDeclarations)){
							analyseStringMethod(exp, true);
						}
						else {
							for(String s: variables.keySet()){
								if(exp.toString().equals(s)){
									variables_used.put(s, variables.get(s));
								}
							}
						}
					}
				}
				if(v.getName().toString().equals(aux)){
					swt_methods.add(node);
				}
			}
		}

		return super.visit(node);
	}


	public List<VariableDeclarationFragment> getSwt_variables() {
		return swt_variables;
	}

	public List<MethodInvocation> getSwt_methods() {
		return swt_methods;
	}

	public List<VariableDeclarationStatement> getSwt_declarations() {
		return swt_declarations;
	}

	public Map<String, VariableDeclarationFragment> getVariables() {
		return variables;
	}

	public Map<String, VariableDeclarationFragment> getVariables_used() {
		return variables_used;
	}

	//	@Override
	//	public boolean visit(ClassInstanceCreation node) {
	//		ITypeBinding binding = node.getType().resolveBinding();
	//		try {
	//			Class<?> type = Class.forName(binding.getQualifiedName());
	//			if(type.isAssignableFrom(org.eclipse.swt.widgets.Control.class)) {
	//				
	//			}
	//		} catch (ClassNotFoundException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		return super.visit(node);
	//	}

}
