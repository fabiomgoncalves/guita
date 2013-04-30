package org.eclipselabs.variableanalyzer.service;

import org.eclipse.core.resources.IFile;
import org.eclipselabs.variableanalyzer.VariableAnalyzer;

public class VariableResolver {

	public static VariableInfo resolve(IFile file, String varName, int line) {
		VariableAnalyzer analyzer = new VariableAnalyzer(file);
		VariableInfo varInfo = analyzer.resolveType(varName, line);
		analyzer.print();
		return varInfo;
	}
}
