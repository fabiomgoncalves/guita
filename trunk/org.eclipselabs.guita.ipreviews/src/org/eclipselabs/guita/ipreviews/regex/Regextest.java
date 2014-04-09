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
		
		String ola = "ola.teste()";
		String adeus = "ola[2.0].teste()";
		ola.split(Regex.arrayAccess);
		System.out.println(Arrays.toString(ola.split(Regex.arrayAccess)));
		System.out.println(adeus.split(Regex.arrayAccess).length);
	}

}
