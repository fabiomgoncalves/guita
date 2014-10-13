package org.eclipselabs.jmodeldiagram.model.tests;


import java.io.IOException;
import java.io.ObjectOutputStream;

import org.eclipselabs.jmodeldiagram.model.JClass;
import org.eclipselabs.jmodeldiagram.model.JInterface;
import org.eclipselabs.jmodeldiagram.model.JModel;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.Stereotype;

public class TestSerialize {

	public static void main(String[] args) {
	

		JModel model = new JModel();
		JClass a = new JClass("A");
		new JOperation(a, "op1");
		a.addStereotype(new Stereotype("component"));
		model.addType(a);
		
		JInterface i = new JInterface("I");
		new JOperation(i, "op2");
		
		try {
			ObjectOutputStream oos = new ObjectOutputStream(System.out);
			oos.writeObject(model);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
