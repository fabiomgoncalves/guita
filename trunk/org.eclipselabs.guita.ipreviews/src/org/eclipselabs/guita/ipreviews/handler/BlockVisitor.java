package org.eclipselabs.guita.ipreviews.handler;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class BlockVisitor extends ASTVisitor{
	
	int line;
	Block node;
	
	public BlockVisitor(int line){
		this.line = line;
	}
	
	private boolean withinSelection(ASTNode node) {
		CompilationUnit unit = (CompilationUnit) node.getRoot();
		int start = unit.getLineNumber(node.getStartPosition());
		int end = unit.getLineNumber(node.getStartPosition() + node.getLength());
		return line >= start && line <= end;
	}

	@Override
	public boolean visit(Block node) {
		if(withinSelection(node))
			this.node = node;
		return super.visit(node);
	}
	
	public Block getNode() {
		return node;
	}
}
