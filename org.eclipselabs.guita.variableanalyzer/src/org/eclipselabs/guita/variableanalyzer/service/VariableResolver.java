package org.eclipselabs.guita.variableanalyzer.service;

import org.eclipse.core.resources.IFile;
import org.eclipselabs.guita.variableanalyzer.VariableAnalyzer;

public class VariableResolver {

	public static VariableInfo resolve(IFile file, String varName, int line, TypeFilter filter) {
		VariableAnalyzer analyzer = new VariableAnalyzer(file, filter);
		VariableInfo varInfo = analyzer.resolveType(varName, line);
		analyzer.print();
		return varInfo;
	}
	
	
}
