package pt.guita.tasks;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import pt.guita.tasks.tracer.Tracer;

public class Rec implements IViewActionDelegate {

	@Override
	public void run(IAction action) {
//		System.out.println("CHECKED: " + action.isChecked());
		if(action.isChecked()) {
			Activator.getInstance().startRecording();
			
		}
		else {
			Activator.getInstance().stopRecording();			
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void init(IViewPart view) {

	}

}
