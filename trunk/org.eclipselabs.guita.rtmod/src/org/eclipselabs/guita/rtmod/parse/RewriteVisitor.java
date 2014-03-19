package org.eclipselabs.guita.rtmod.parse;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;


public class RewriteVisitor extends ASTVisitor {

	private final ASTRewrite rewrite;
	private final CompilationUnit unit;
	private int linha;
	private Object[] replace;

	public RewriteVisitor(CompilationUnit unit, Object[] replace, int linha) {
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

	@Override
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
			
			for (Object o : replace) {
				methodInvocation.arguments().add(createNewObject(o));
			}

			rewrite.replace(node, methodInvocation, null);
		}
		return true;		
	}

	private Object createNewObject(Object parameter) {
		AST ast = rewrite.getAST();
		Object literal = null;
		//System.out.println(parameter.toString() + " > " + parameter.getClass().getName());
		
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
