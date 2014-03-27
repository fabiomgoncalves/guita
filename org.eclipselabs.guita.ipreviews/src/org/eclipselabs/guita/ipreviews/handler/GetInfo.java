package org.eclipselabs.guita.ipreviews.handler;


import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipselabs.guita.ipreviews.view.PreviewView;

public class GetInfo extends AbstractHandler {

  private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    // Get all projects in the workspace
    IProject[] projects = root.getProjects();
    // Loop over all projects
    for (IProject project : projects) {
      try {
        if (project.isNatureEnabled(JDT_NATURE)) {
          analyseMethods(project);
        }
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private void analyseMethods(IProject project) throws JavaModelException {
    IPackageFragment[] packages = JavaCore.create(project)
        .getPackageFragments();
    // parse(JavaCore.create(project));
    for (IPackageFragment mypackage : packages) {
      if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
        createAST(mypackage);
      }

    }
  }

  private void createAST(IPackageFragment mypackage)
      throws JavaModelException {
    for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
      // Now create the AST for the ICompilationUnits
      CompilationUnit parse = parse(unit);
      MethodVisitor visitor = new MethodVisitor();
      parse.accept(visitor);
      for(VariableDeclarationStatement v : visitor.getSwt_declarations()){
    	  System.out.println("SWT Declaration Statement: " + v.toString());
      }
      for(MethodInvocation m : visitor.getSwt_methods()){
          System.out.println("SWT Method used : " + m); 
      }
      try {
    	System.out.println(visitor.getVariables_used());
		PreviewView view = (PreviewView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("guidesign.previewview");
		view.constructPreview(visitor.getSwt_declarations(), visitor.getSwt_methods());
	} catch (PartInitException e) {
		e.printStackTrace();
	}

    }
  }

  
/** * Reads a ICompilationUnit and creates the AST DOM for manipulating the * Java source file * * @param unit * @return */


  private static CompilationUnit parse(ICompilationUnit unit) {
    ASTParser parser = ASTParser.newParser(AST.JLS4	);
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit) parser.createAST(null); // parse
  }
} 