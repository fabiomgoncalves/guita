package pt.iscte.dcti.umlviewer.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class Comando1 extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("OLAAA");
		return null;
	}

}
