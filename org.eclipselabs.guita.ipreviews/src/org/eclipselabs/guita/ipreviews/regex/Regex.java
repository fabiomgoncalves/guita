package org.eclipselabs.guita.ipreviews.regex;

public class Regex {
	public static final String arrayAccess = "([A-Z]|[a-z]|[0-9]|_)+\\[(.)*\\]";
	public static final String newArguments = "new ([A-Z]|[a-z]|[0-9]|_)+((<[A-Z]|[a-z]|[0-9]|_)+(( )*,([A-Z]|[a-z]|[0-9]|_))+>)?\\((.)*\\)";
	public static final String newArgumentsArray = "new ([A-Z]|[a-z]|[0-9]|_)+\\[(.)*\\]";
	public static final String constantDeclarations = "([A-Z]|[a-z]|[0-9]|_)+(\\.)([A-Z]|[a-z]|[0-9]|_)+(\\[([A-Z]|[a-z]|[0-9]|_|\\.)+\\])?";
	public static final String constantArrayDeclarations = "([A-Z]|[a-z]|[0-9]|_)+(\\[(.)*\\])(\\.)([A-Z]|[a-z]|[0-9]|_)+(\\[([A-Z]|[a-z]|[0-9]|_)+\\])?";
	public static final String methodDeclarations = "([A-Z]|[a-z]|[0-9]|_)+(\\.)([A-Z]|[a-z]|[0-9]|_)+\\((.)*\\)";
	public static final String methodArrayDeclarations = "([A-Z]|[a-z]|[0-9]|_)+(\\[(.)*\\])(\\.)([A-Z]|[a-z]|[0-9]|_)+\\((.)*\\)";
	public static final String operatorsDeclarations = "(.)+(\\||&)(.)+((.)+(\\||&)(.)+)*";
}
