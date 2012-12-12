package org.eclipselabs.variableanalyzer;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class VariableAnalyzer {

	private Visitor visitor;
	
	public VariableAnalyzer(IFile file) {
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		final ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
		parser.setSource(unit);
		final ASTNode node = parser.createAST(null);
		visitor = new Visitor((CompilationUnit) node);
		node.accept(visitor);
	}
	
	public String resolveType(String varName, int line) {
		return visitor.resolveType(varName, line);
	}

	public List<String> resolveTypes(int line) {
		return visitor.resolveTypes(line);
	}
	
	public void print() {
		visitor.printVars();
	}
}
