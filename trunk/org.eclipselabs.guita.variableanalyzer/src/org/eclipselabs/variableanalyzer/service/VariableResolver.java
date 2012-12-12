package org.eclipselabs.variableanalyzer.service;

import org.eclipse.core.resources.IFile;
import org.eclipselabs.variableanalyzer.VariableAnalyzer;

public class VariableResolver {

	public static String resolve(IFile file, String varName, int line) {
		VariableAnalyzer analyzer = new VariableAnalyzer(file);
		String type = analyzer.resolveType(varName, line);
		return type;
	}
}
