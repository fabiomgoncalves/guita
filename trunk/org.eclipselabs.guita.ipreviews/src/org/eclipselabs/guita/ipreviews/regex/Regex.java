package org.eclipselabs.guita.ipreviews.regex;

public class Regex {
	public static final String newArguments = "new ([A-Z]|[a-z])+\\((.)*\\)";
	public static final String constantDeclarations = "([A-Z]|[a-z]|[0-9])+(\\.)([A-Z]|[a-z]|[0-9])+";
	public static final String methodDeclarations = "([A-Z]|[a-z]|[0-9])+(\\.)([A-Z]|[a-z]|[0-9])+\\((.)*\\)";
}
