package pt.guita.tasks;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class Rec implements IViewActionDelegate {

	@Override
	public void run(IAction action) {
//		System.out.println("CHECKED: " + action.isChecked());
		if(action.isChecked()) {
			Activator.getInstance().getRecorder().startRecording();
			
		}
		else {
			Activator.getInstance().getRecorder().stopRecording();			
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void init(IViewPart view) {

	}

}
