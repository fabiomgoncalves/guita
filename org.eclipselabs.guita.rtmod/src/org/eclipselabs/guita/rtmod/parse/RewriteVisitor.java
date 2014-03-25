package org.eclipselabs.guita.rtmod.parse;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

public class RewriteVisitor extends ASTVisitor {

	private final CompilationUnit compilationUnit;
	private ASTRewrite rewrite;
	private int lineToReplace;
	private Object[] replaceParameters;
	private Map<String, Integer> variablesMap;
	private Map<String, Object> replaceVariablesMap;

	public RewriteVisitor(CompilationUnit compilationUnit, Object[] replaceParameters, int lineToReplace) {
		this.compilationUnit = compilationUnit;
		this.replaceParameters = replaceParameters;
		this.lineToReplace = lineToReplace;
		this.rewrite = ASTRewrite.create(compilationUnit.getAST());
		this.variablesMap = new HashMap<>();
		this.replaceVariablesMap = new HashMap<>();
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
		return compilationUnit.getLineNumber(node.getStartPosition());
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
				//				System.out.println("arg " + node.arguments().get(i));
				//				System.out.println(node.arguments().get(i).getClass().getName());
				//				System.out.println();
				if (node.arguments().get(i).getClass() == org.eclipse.jdt.core.dom.SimpleName.class) {	
					//System.out.println(((SimpleName)node.arguments().get(i)).getIdentifier());
					SimpleName simple = ast.newSimpleName(((SimpleName)node.arguments().get(i)).getIdentifier());
					methodInvocation.arguments().add(simple);
					replaceVariablesMap.put(simple.toString(), o);
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
				//System.out.println("Fragment: " + node.getType() + " " + frag.getName());
			}
		}

		return true;
	}

	private Object createNewObject(Object parameter) {
		AST ast = rewrite.getAST();
		Object literal = null;

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
	
	public Map<String, Integer> getVariablesMap() {
		return variablesMap;
	}

	public Map<String, Object> getReplaceVariablesMap() {
		return replaceVariablesMap;
	}
}
