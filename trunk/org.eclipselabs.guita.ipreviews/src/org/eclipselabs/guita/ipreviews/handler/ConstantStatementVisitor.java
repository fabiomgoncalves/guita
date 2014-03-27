package org.eclipselabs.guita.ipreviews.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.QualifiedName;

public class ConstantStatementVisitor extends ASTVisitor {
	
	Map<String, Object> objects_map = new HashMap<String, Object>();
	
	@Override
	public boolean visit(QualifiedName node) {
		String name = node.getFullyQualifiedName();
		objects_map.put(name, node.resolveConstantExpressionValue());
		return super.visit(node);
	}
	
	public Map<String, Object> getObjects_map() {
		return objects_map;
	}
}
