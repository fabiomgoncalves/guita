package org.eclipselabs.guita.rtmod.parse;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class FirstPassVisitor extends Visitor {

	private int lineToReplace;
	private Object[] replaceParameters;
	private Map<String, Integer> variablesMap;
	private Map<String, Object> replaceVariablesMap;

	public FirstPassVisitor(CompilationUnit compilationUnit, Object[] replaceParameters, int lineToReplace) {
		super(compilationUnit);
		this.replaceParameters = replaceParameters;
		this.lineToReplace = lineToReplace;
		this.variablesMap = new HashMap<>();
		this.replaceVariablesMap = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodInvocation node) {
		int currentLine = getLine(node);

		if (currentLine == lineToReplace) {			
			AST ast = rewrite.getAST();
			MethodInvocation methodInvocation = ast.newMethodInvocation();
			SimpleName exp = ast.newSimpleName(node.getExpression().toString());			
			methodInvocation.setExpression(exp);
			SimpleName name = ast.newSimpleName(node.getName().toString());			
			methodInvocation.setName(name);				

			int i = 0;
			for (Object o : replaceParameters) {	
				Object argument = node.arguments().get(i);
				if (argument.getClass() == SimpleName.class) {	
					String variableName = ((SimpleName) argument).getIdentifier();
					SimpleName variable = ast.newSimpleName(variableName);
					methodInvocation.arguments().add(variable);
					replaceVariablesMap.put(variableName, o);
				} else {
					methodInvocation.arguments().add(createNewObject(o));
				}
				i++;
			}
			rewrite.replace(node, methodInvocation, null);
		}

		return true;		
	}	

	@Override
	public boolean visit(FieldDeclaration node) {
		int modifiers = node.getModifiers();

		if (Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
			for(int i = 0; i < node.fragments().size(); ++i) {
				VariableDeclarationFragment frag = (VariableDeclarationFragment)node.fragments().get(i);
				variablesMap.put(frag.getName().toString(), getLine(node));
			}
		}

		return true;
	}

	public Map<String, Integer> getVariablesMap() {
		return variablesMap;
	}

	public Map<String, Object> getReplaceVariablesMap() {
		return replaceVariablesMap;
	}
}
