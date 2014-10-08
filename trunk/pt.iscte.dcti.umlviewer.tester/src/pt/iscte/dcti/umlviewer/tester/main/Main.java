package pt.iscte.dcti.umlviewer.tester.main;

import java.util.Scanner;
import pt.iscte.dcti.umlviewer.tester.model.Class1;
import pt.iscte.dcti.umlviewer.tester.model.Class2;
import pt.iscte.dcti.umlviewer.tester.model.Class3;

public class Main {

	public static void main(String[] args) {
		Class1 c1 = new Class1();
		Class2 c2 = new Class2();

		Scanner teclado = new Scanner(System.in);
		teclado.nextLine();
		c1.method1();

		teclado = new Scanner(System.in);
		teclado.nextLine();
		c2.method2();

		teclado = new Scanner(System.in);
		teclado.nextLine();
		c1.method5();
		c2.method4();
		Class3 c3 = new Class3();
		c3.method3();
	}

}