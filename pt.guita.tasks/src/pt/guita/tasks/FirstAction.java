package pt.guita.tasks;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class FirstAction implements IViewActionDelegate {
	private IViewPart view;

	@Override
	public void run(IAction action) {
		Recorder recorder = Activator.getInstance().getRecorder();
		recorder.goToFirst();
		IMethod method = (IMethod) recorder.getFirstElement();
		if(method != null) {
			Common.openMethod(method, recorder.getElementLine());
//			view.getSite().getSelectionProvider().setSelection(activator);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}


	@Override
	public void init(IViewPart view) {
		this.view = view;
	}

}
