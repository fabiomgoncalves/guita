package org.eclipselabs.guita.rtmod.parse;

import java.util.LinkedList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;


public class RewriteVisitor extends ASTVisitor {

	private final ASTRewrite rewrite;
	private final CompilationUnit unit;
	private int linha;
	private LinkedList<String> replace;

	public RewriteVisitor(CompilationUnit unit, LinkedList<String> replace, int linha) {
		this.unit = unit;
		this.replace = replace;
		this.linha = linha;
		rewrite = ASTRewrite.create(unit.getAST());
	}

	public void applyChanges(Document document, ICompilationUnit compUnit) throws JavaModelException {
		ICompilationUnit workingCopy = compUnit.getWorkingCopy(null);

		TextEdit edits = rewrite.rewriteAST(document, null);
		workingCopy.applyTextEdit(edits, null);
		workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		workingCopy.commitWorkingCopy(true, null);
		workingCopy.discardWorkingCopy();
	}

	public void applyChanges(Document document) throws Exception {
		TextEdit edits = rewrite.rewriteAST(document, null);
		edits.apply(document);
	}


	private int getLine(ASTNode node) {
		return unit.getLineNumber(node.getStartPosition());
	}

	@SuppressWarnings("unchecked")
	public boolean visit(MethodInvocation node) {
		int line = getLine(node);
		if (line == linha) {			
			AST ast = rewrite.getAST();
			MethodInvocation methodInvocation = ast.newMethodInvocation();
			SimpleName exp = ast.newSimpleName(node.getExpression().toString());			
			methodInvocation.setExpression(exp);
			SimpleName name = ast.newSimpleName(node.getName().toString());			
			methodInvocation.setName(name);				
			
			for (String o : replace) {
				StringLiteral textParam = ast.newStringLiteral();
				textParam.setLiteralValue(o);		
				methodInvocation.arguments().add(textParam);
			}

			rewrite.replace(node, methodInvocation, null);
		}
		return true;		
	}
	
	public boolean visit(IMethod node) {
		return false;
		
	}

	public boolean visit(StringLiteral node) {
//		int line = getLine(node);
//		if (line == linha) {
//			StringLiteral newLiteral = rewrite.getAST().newStringLiteral();
//			newLiteral.setLiteralValue(replace);
//			rewrite.replace(node, newLiteral, null);			
//		}

		return true;
	}
}
