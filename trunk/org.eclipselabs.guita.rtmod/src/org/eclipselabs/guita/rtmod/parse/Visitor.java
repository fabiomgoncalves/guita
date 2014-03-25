package org.eclipselabs.guita.rtmod.parse;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public abstract class Visitor extends ASTVisitor {
	private final CompilationUnit compilationUnit;	
	protected ASTRewrite rewrite;	
	
	public Visitor(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
		this.rewrite = ASTRewrite.create(compilationUnit.getAST());
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

	protected int getLine(ASTNode node) {
		return compilationUnit.getLineNumber(node.getStartPosition());
	}
	
	protected Expression createNewObject(Object parameter) {
		AST ast = rewrite.getAST();
		Expression literal = null;

		switch (parameter.getClass().getName()) {
		case "int":
		case "java.lang.Integer":
		case "short":
		case "java.lang.Short":
		case "float":
		case "java.lang.Float":
		case "double":
		case "java.lang.Double":
		case "long":
		case "java.lang.Long":
			literal = ast.newNumberLiteral(parameter.toString());
			break;
		case "String":
		case "java.lang.String":
			literal = ast.newStringLiteral();
			((StringLiteral) literal).setLiteralValue((String) parameter);	
			break;
		case "boolean":
		case "java.lang.Boolean":
			literal = ast.newBooleanLiteral((boolean) parameter);
			break;
		case "char":
		case "java.lang.Character":
			literal = ast.newCharacterLiteral();
			((CharacterLiteral) literal).setCharValue((char) parameter);
			break;
		}

		return literal;
	}
}
