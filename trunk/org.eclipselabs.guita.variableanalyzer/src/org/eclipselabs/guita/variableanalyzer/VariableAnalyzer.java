package org.eclipselabs.guita.variableanalyzer;


import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipselabs.guita.variableanalyzer.service.TypeFilter;
import org.eclipselabs.guita.variableanalyzer.service.VariableInfo;

public class VariableAnalyzer {

	private Visitor visitor;
	
	public VariableAnalyzer(IFile file) {
		this(file, new TypeFilter() {
			
			@Override
			public boolean include(String type) {
				return true;
			}
		});
	}
	
	public VariableAnalyzer(IFile file, TypeFilter filter) {
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		final ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
		parser.setSource(unit);
		final ASTNode node = parser.createAST(null);
		visitor = new Visitor((CompilationUnit) node, filter);
		node.accept(visitor);
	}
	
	public VariableInfo resolveType(String varName, int line) {
		return visitor.resolveType(varName, line);
	}

	public void print() {
		visitor.printVars();
	}
}
