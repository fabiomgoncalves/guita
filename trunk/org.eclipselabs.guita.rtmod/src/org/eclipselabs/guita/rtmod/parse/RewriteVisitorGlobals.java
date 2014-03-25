package org.eclipselabs.guita.rtmod.parse;

import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class RewriteVisitorGlobals extends Visitor {

	private Map<String, Integer> variablesMap;
	private Map<String, Object> replaceVariablesMap;

	public RewriteVisitorGlobals(CompilationUnit compilationUnit, Map<String, Integer> variablesMap, Map<String, Object> replaceVariablesMap) {
		super(compilationUnit);
		this.variablesMap = variablesMap;
		this.replaceVariablesMap = replaceVariablesMap;
		System.out.println("vmap " + variablesMap.size());
		System.out.println("rmap " + replaceVariablesMap.size());
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		for (Map.Entry<String, Object> entry : replaceVariablesMap.entrySet()) {	
			String key = entry.getKey();
			if (variablesMap.containsKey(key) && variablesMap.get(key) == getLine(node)) {
				AST ast = rewrite.getAST();
				VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
				fragment.setName(ast.newSimpleName(key));
				fragment.setInitializer(createNewObject(replaceVariablesMap.get(key)));
				rewrite.replace(node, fragment, null);
			}
		}
		
		return true;
	}
}
