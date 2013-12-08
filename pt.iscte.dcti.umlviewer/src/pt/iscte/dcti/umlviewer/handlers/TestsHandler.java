package pt.iscte.dcti.umlviewer.handlers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import pt.iscte.dcti.umlviewer.service.Service;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;
import pt.iscte.dcti.umlviewer.test.*;

public class TestsHandler extends AbstractHandler {

	private static final boolean SHOW_REPORTS = true;

	private Service service;

	private int current_index;
	private Object[] tests_array;

	public TestsHandler() {
		service = ServiceHelper.getService();
		setUpTestsArray();
		if(SHOW_REPORTS) {
			System.out.println("TESTS READY");
		}
	}

	private void setUpTestsArray() {
		current_index = 0;
		tests_array = new Object[2];
		
		//PRIMEIRO
		
		Map<Class<?>, Collection<Method>> fragmento_1 = new HashMap<Class<?>, Collection<Method>>();
		
		Collection<Method> metodos_1 = new LinkedList<Method>();
		try {
			Method m = ArmazemObjectos.class.getDeclaredMethod("adicionarObjecto", Object.class);
			metodos_1.add(m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		fragmento_1.put(ArmazemObjectos.class, metodos_1);
		
		Collection<Method> metodos_2 = new LinkedList<Method>();
		try {
			Method m = Lista.class.getDeclaredMethod("adicionar", Object.class);
			metodos_2.add(m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		fragmento_1.put(Lista.class, metodos_2);
		
		Collection<Method> metodos_3 = new LinkedList<Method>();
		try {
			Method m = ListaLigada.class.getDeclaredMethod("adicionar", Object.class);
			metodos_3.add(m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		fragmento_1.put(ListaLigada.class, metodos_3);
		
		tests_array[0] = fragmento_1;
		
		//SEGUNDO
		
		Map<Class<?>, Collection<Method>> fragmento_2 = new HashMap<Class<?>, Collection<Method>>();
		
		Collection<Method> metodos_4 = new LinkedList<Method>();
		try {
			Method m = Lista.class.getDeclaredMethod("adicionar", Object.class);
			metodos_4.add(m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		fragmento_2.put(Lista.class, metodos_4);
		
		Collection<Method> metodos_5 = new LinkedList<Method>();
		try {
			Method m = LojaObjectos.class.getDeclaredMethod("vender", int.class);
			metodos_5.add(m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		fragmento_2.put(LojaObjectos.class, metodos_5);
		
		Collection<Method> metodos_6 = Arrays.asList(Iterador.class.getDeclaredMethods());
		fragmento_2.put(Iterador.class, metodos_6);
		
		Collection<Method> metodos_7 = Arrays.asList(ListaIterador.class.getDeclaredMethods());
		fragmento_2.put(ListaIterador.class, metodos_7);
		
		Collection<Method> metodos_8 = new LinkedList<Method>();
		try {
			Method m = ListaLigada.class.getDeclaredMethod("iterador");
			metodos_8.add(m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		fragmento_2.put(ListaLigada.class, metodos_8);
		
		tests_array[1] = fragmento_2;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(SHOW_REPORTS) {
			System.out.println("TESTS PRESSED");
		}
		if(current_index < tests_array.length) {
			Map<Class<?>, Collection<Method>> aux = (Map<Class<?>, Collection<Method>>)tests_array[current_index];
			service.showFragment(aux);
			current_index++;
		}
		return null;
	}

}
