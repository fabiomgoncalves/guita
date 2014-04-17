package org.eclipselabs.guita.ipreviews.regex;

import java.util.Arrays;

import org.eclipse.swt.widgets.Button;

public class Regextest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//teste acceptedArguments
		System.out.println("New Declarations");
		String teste1 = "new FillLayout()";
		String teste2 = "new FillLayout(aaaa, new blablabla())";
		String teste3 = "newFillLayout(aaaa, new blablabla())";
		String teste4 = "aaaa";
		System.out.println(teste1.matches(Regex.newArguments));
		System.out.println(teste2.matches(Regex.newArguments));
		System.out.println(teste3.matches(Regex.newArguments));
		System.out.println(teste4.matches(Regex.newArguments));
		
		System.out.println("Constant Declarations");
		String teste5 = "SWT.NONE";
		String teste6 = "new FillLayout(aaaa, new blablabla())";
		String teste7 = "widget.getOla()";
		String teste8 = "aaaa";
		System.out.println(teste5.matches(Regex.constantDeclarations));
		System.out.println(teste6.matches(Regex.constantDeclarations));
		System.out.println(teste7.matches(Regex.constantDeclarations));
		System.out.println(teste8.matches(Regex.constantDeclarations));
		
		System.out.println("Array Declarations");
		String teste9 = "x[0]";
		String teste10 = "new String[1]";
		System.out.println(teste9.matches(Regex.arrayAccess));
		System.out.println(teste10.matches(Regex.newArgumentsArray));
		
		System.out.println("Operators");
		String teste11 = "SWT.NONE | SWT.SHADOW";
		String teste12 = "2 | SWT.NONE | teste.getInt(teste)";
		String teste13 = "teste.getInt(int)";
		System.out.println(teste11.matches(Regex.operatorsDeclarations));
		System.out.println(teste12.matches(Regex.operatorsDeclarations));
		System.out.println(teste13.matches(Regex.operatorsDeclarations));
	}

}
