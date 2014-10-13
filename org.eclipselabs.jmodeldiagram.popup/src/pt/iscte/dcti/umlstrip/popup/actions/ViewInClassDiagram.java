package pt.iscte.dcti.umlstrip.popup.actions;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
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
import org.eclipselabs.jmodeldiagram.model.reflection.JModelReflection;

import pt.iscte.dcti.umlviewer.service.ClickHandler;
import pt.iscte.dcti.umlviewer.service.ServiceHelper;

public class ViewInClassDiagram implements IObjectActionDelegate {

	private Shell shell;
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
			URL url = new File(p.toString() + File.separator + proj.getOutputLocation().lastSegment()).toURI().toURL(); // execution path
			URL[] urls = {url}; 
			URLClassLoader classLoader = new URLClassLoader(urls); 

			JModel model = new JModel();

			List<Class<?>> classes = new ArrayList<Class<?>>();
			for(Object obj : selection.toArray()) {
				IJavaElement o = (IJavaElement) obj;

				if(o instanceof ICompilationUnit) {
					classes.add(handleCompilationUnit((ICompilationUnit) o, classLoader, model));
				}
				else if(o instanceof IPackageFragment) {
					IPackageFragment pack = (IPackageFragment) o;
					for(ICompilationUnit u : pack.getCompilationUnits())
						classes.add(handleCompilationUnit(u, classLoader, model));
				}
			}
			
			for(Class<?> c : classes)
				if(!c.isInterface())
					handleAssociations(c, model);
			
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


	private Class<?> handleCompilationUnit(ICompilationUnit unit, URLClassLoader classLoader,
			JModel model)
					throws ClassNotFoundException {

		IType t = unit.findPrimaryType();
		Class<?> c = classLoader.loadClass(t.getFullyQualifiedName());

		if(c.isInterface()) {
			JInterface ji = JModelReflection.createInterface(c);
			model.addType(ji);
			for(Class<?> i : c.getInterfaces()) {
				JInterface si = JModelReflection.createInterface(i);
				ji.addSupertype(si);
			}
		}
		else {
			JClass jc = JModelReflection.createClass(c);
			model.addType(jc);
			if(!c.getSuperclass().equals(Object.class)) {
				JClass superclass = JModelReflection.createClass(c.getSuperclass());
				model.addType(superclass);
				jc.setSuperclass(superclass);
			}
			for(Class<?> i : c.getInterfaces()) {
				JInterface si = JModelReflection.createInterface(i);
				jc.addInterface(si);
			}
		}
		return c;
	}

	private void handleAssociations(Class<?> clazz, JModel model) {
		 JClass jc = (JClass) model.getType(clazz.getName());
		 
		for(Field field: clazz.getDeclaredFields()) {

			if(field.isSynthetic()) continue;

			Class<?> fieldType = field.getType();
			
			if(Collection.class.isAssignableFrom(fieldType)) { //Collection field
				Type genericFieldType = field.getGenericType();

				if(!ParameterizedType.class.isAssignableFrom(genericFieldType.getClass())) continue; //If the collection does not have a parametrized type

				Type aux = ((ParameterizedType)genericFieldType).getActualTypeArguments()[0];

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
