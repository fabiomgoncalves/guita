package org.eclipselabs.guita.ipreviews.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipselabs.guita.ipreviews.regex.Regex;

public class VResolverVisitor2 extends ASTVisitor{

	private Map<String, ASTNode> nodes;

	private List<ASTNode> swt_nodes;
	private List<Class<?>> swt_nodes_classes;

	private int firstLine;
	private int lastLine;
	
	public VResolverVisitor2(Map<String, ASTNode> nodes, int firstLine, int lastLine){
		this.nodes = nodes;
		swt_nodes = new ArrayList<ASTNode>();
		swt_nodes_classes = new ArrayList<Class<?>>();
		
		this.firstLine = firstLine;
		this.lastLine = lastLine;
	}
	
	private boolean withinSelection(ASTNode node) {
		CompilationUnit unit = (CompilationUnit) node.getRoot();
		int start = unit.getLineNumber(node.getStartPosition());
		int end = unit.getLineNumber(node.getStartPosition() + node.getLength());
		return start >= firstLine && end <= lastLine;
	}
 	
//	@Override
//	public boolean visit(Block node) {
//		return withinSelection(node);
//	}

	@Override
	public boolean visit(Assignment node) {
		if(!withinSelection(node))
			return false;
		
		String objectName;
		if(node.getLeftHandSide().toString().matches(Regex.arrayAccess)){
			objectName = node.toString().substring(0,node.toString().indexOf("["));
		}
		else objectName = node.toString().replaceAll(" ", "").substring(0, node.toString().indexOf("="));
		if(nodes.containsKey(objectName)){
			swt_nodes.add(node);
			swt_nodes_classes.add(Assignment.class);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		if(!withinSelection(node))
			return false;
		
		String objectName = ((VariableDeclarationFragment)node.fragments().get(0)).getName().toString();
		if(nodes.containsKey(objectName)){
			swt_nodes.add((VariableDeclarationFragment)node.fragments().get(0));
			swt_nodes_classes.add(VariableDeclarationFragment.class);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if(!withinSelection(node))
			return false;
		
		if((node.getParent() instanceof ExpressionStatement)){
			String objectName;
			if(node.toString().matches(Regex.methodArrayDeclarations)){
				objectName = node.toString().substring(0,node.toString().indexOf("["));
			}
			else objectName = node.toString().replaceAll(" ", "").substring(0, node.toString().indexOf("."));
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
