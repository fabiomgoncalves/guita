package org.eclipselabs.guita.ipreviews.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class VResolverVisitor extends ASTVisitor{

	private Map<String, ASTNode> nodes;

	private List<ASTNode> swt_nodes;
	private List<Class<?>> swt_nodes_classes;

	public VResolverVisitor(Map<String, ASTNode> nodes){
		this.nodes = nodes;
		swt_nodes = new ArrayList<ASTNode>();
		swt_nodes_classes = new ArrayList<Class<?>>();
	}

	@Override
	public boolean visit(Assignment node) {
		String objectName = node.toString().replaceAll(" ", "").substring(0, node.toString().indexOf("="));
		if(nodes.containsKey(objectName)){
			swt_nodes.add(node);
			swt_nodes_classes.add(Assignment.class);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		String objectName = node.fragments().get(0).toString().replaceAll(" ", "").substring(0, node.fragments().get(0).toString().indexOf("="));
		if(nodes.containsKey(objectName)){
			swt_nodes.add((VariableDeclarationFragment)node.fragments().get(0));
			swt_nodes_classes.add(VariableDeclarationFragment.class);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if((node.getParent() instanceof ExpressionStatement)){
			String objectName = node.toString().replaceAll(" ", "").substring(0, node.toString().indexOf("."));
			if(nodes.containsKey(objectName)){
				swt_nodes.add(node);
				swt_nodes_classes.add(MethodInvocation.class);
			}
		}
		return super.visit(node);
	}

	public List<ASTNode> getSwt_nodes() {
		return swt_nodes;
	}

	public List<Class<?>> getSwt_nodes_classes() {
		return swt_nodes_classes;
	}

}
