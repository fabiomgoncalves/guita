package org.eclipselabs.guita.rtmod;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;

class FileVisitor implements IResourceVisitor {

	ICompilationUnit unit;
	IFile file;
	String fileName;

	FileVisitor(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean visit(IResource resource) throws JavaModelException, CoreException { 

		if(resource.getType() == IResource.PROJECT) {
			IProject project = (IProject) resource;

			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) { // mudar constante

				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
				for (IPackageFragment mypackage : packages) {
					if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
						System.out.println("Package " + mypackage.getElementName());
						for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
							if (unit.getElementName().equals(fileName)) {
								this.unit = unit;
							printCompilationUnitDetails(unit);
							}
						}
					}

				}

			}
			return false;
		}

		return true; 
	}

	private void printCompilationUnitDetails(ICompilationUnit unit)
			throws JavaModelException {
		System.out.println("Source file " + unit.getElementName());
		Document doc = new Document(unit.getSource());
		System.out.println("Has number of lines: " + doc.getNumberOfLines());
	}
}