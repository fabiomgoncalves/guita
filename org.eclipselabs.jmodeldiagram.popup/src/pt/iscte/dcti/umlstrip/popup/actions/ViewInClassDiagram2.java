package pt.iscte.dcti.umlstrip.popup.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipselabs.jmodeldiagram.model.Association;
import org.eclipselabs.jmodeldiagram.model.JClass;
import org.eclipselabs.jmodeldiagram.model.JInterface;
import org.eclipselabs.jmodeldiagram.model.JModel;
import org.eclipselabs.jmodeldiagram.model.JOperation;
import org.eclipselabs.jmodeldiagram.model.JType;

import pt.iscte.dcti.umlviewer.service.ClickHandler;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class ViewInClassDiagram2 implements IObjectActionDelegate {

	private IStructuredSelection selection;
	private ClickHandler handler;

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if(selection == null || selection.isEmpty())
			return;

		try {
			final IJavaProject proj = ((IJavaElement) selection.getFirstElement()).getJavaProject();
			IPath p = proj.getCorrespondingResource().getLocation();

			JModel model = new JModel();

			List<IType> classes = new ArrayList<IType>();
			for(Object obj : selection.toArray()) {
				IJavaElement o = (IJavaElement) obj;

				if(o instanceof ICompilationUnit) {
					classes.add(handleCompilationUnit((ICompilationUnit) o, model));
				}
				else if(o instanceof IPackageFragment) {
					IPackageFragment pack = (IPackageFragment) o;
					for(ICompilationUnit u : pack.getCompilationUnits())
						classes.add(handleCompilationUnit(u, model));
				}
			}
			
			for(IType t : classes)
				if(!t.isInterface())
					handleAssociations(t, model);
			
			ServiceHelper.getService().displayModel(model, false);

			if(handler != null)
				ServiceHelper.getService().removeClickHandler(handler);

			handler = new ClickHandler() {
				@Override
				public void methodClicked(JOperation op) {
					IType t = null;
					try {
						t = proj.findType(op.getOwner().getQualifiedName());
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						ITextEditor editor = (ITextEditor) IDE.openEditor(page, (IFile) t.getResource());
						for(IMethod im : t.getMethods()) {
							if(im.getElementName().equals(op.getName())) {
								ISourceRange range =  im.getNameRange();
								editor.selectAndReveal(range.getOffset(), range.getLength());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}

				@Override
				public void classClicked(JType type) {
					IType t = null;
					try {
						t = proj.findType(type.getQualifiedName());
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						ITextEditor editor = (ITextEditor) IDE.openEditor(page, (IFile) t.getResource());
						ISourceRange range =  t.getNameRange();
						editor.selectAndReveal(range.getOffset(), range.getLength());
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			};
			ServiceHelper.getService().addClickHandler(handler);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private IType handleCompilationUnit(ICompilationUnit unit, JModel model)
					throws Exception {

		IType t = unit.findPrimaryType();
		
		if(t.isInterface()) {
			JInterface ji = new JInterface(t.getElementName());
			model.addType(ji);
			
			for(String interfaceName : t.getSuperInterfaceNames()) {
				JInterface si = new JInterface(interfaceName);
				ji.addSupertype(si);
			}
		}
		else {
			JClass jc = new JClass(t.getElementName());
			model.addType(jc);
			if(!t.getSuperclassName().equals("Object")) {
				JClass superclass = new JClass(t.getSuperclassName());
				model.addType(superclass);
				jc.setSuperclass(superclass);
			}
			for(String interfaceName : t.getSuperInterfaceNames()) {
				JInterface si = new JInterface(interfaceName);
				jc.addInterface(si);
			}
		}
		return t;
	}

	private void handleAssociations(IType t, JModel model) {
		 JClass jc = (JClass) model.getType(t.getFullyQualifiedName());
		 
		 
		for(IField field : t.getFields()) {

			if(field.isSynthetic()) continue;

			Class<?> fieldType = field.getType();
			
			if(Collection.class.isAssignableFrom(fieldType)) { //Collection field
//				Type genericFieldType = field.getGenericType();
//
//				if(!ParameterizedType.class.isAssignableFrom(genericFieldType.getClass())) continue; //If the collection does not have a parametrized type
//
//				Type aux = ((ParameterizedType)genericFieldType).getActualTypeArguments()[0];

				if(!Class.class.isAssignableFrom(aux.getClass())) continue; //If the parametrized type is not a class type, i.e., Collection<Collection<Type>>

				Class<?> actualFieldType = (Class<?>)aux;

				if(actualFieldType.isArray()) continue; //If the parametrized type is an array

				new Association(jc, model.getType(actualFieldType.getName()), false);
	
//				if(set.contains(actualFieldType)) {
					//createMultipleConnection(clazz, actualFieldType, field);
//				}
			}
			else if(fieldType.isArray()) { //Array field
				Class<?> componentFieldType = fieldType.getComponentType();

				if(componentFieldType.isArray()) continue; //If the component type of the array is another array

//				if(set.contains(componentFieldType)) {
					//createMultipleConnection(clazz, componentFieldType, field);
//				}
				new Association(jc, model.getType(componentFieldType.getName()), false);
			}
			else if(model.getType(fieldType.getName()) != null) { //Class field
//				if(set.contains(fieldType)) {
					//createSimpleConnection(clazz, fieldType, field);
//				}
				new Association(jc, model.getType(fieldType.getName()), true);
			}

		}
	}


	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection) selection;
		else
			this.selection = null;
	}

}
