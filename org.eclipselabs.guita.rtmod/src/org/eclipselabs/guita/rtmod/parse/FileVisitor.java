package org.eclipselabs.guita.rtmod.parse;

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

public class FileVisitor implements IResourceVisitor {

	private ICompilationUnit unit;
	private String fileName;

	public FileVisitor(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean visit(IResource resource) throws JavaModelException, CoreException { 
		if(resource.getType() == IResource.PROJECT) {
			IProject project = (IProject) resource;
			
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProject = JavaCore.create(project);
				IPackageFragment[] packages = javaProject.getPackageFragments();
				for (IPackageFragment mypackage : packages) {
					if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
							if (unit.getElementName().equals(fileName)) {
								this.unit = unit;
								printCompilationUnitDetails(unit, false);
							}
						}
					}
				}
			}
			
			return false;
		}
		
		return true; 
	}

	private void printCompilationUnitDetails(ICompilationUnit unit, boolean print) throws JavaModelException {
		if (print) {
			System.out.println("Source file " + unit.getElementName());
			Document doc = new Document(unit.getSource());
			System.out.println("Has number of lines: " + doc.getNumberOfLines());
		}
	}

	public ICompilationUnit getICompilationUnit() {
		return this.unit;
	}
}