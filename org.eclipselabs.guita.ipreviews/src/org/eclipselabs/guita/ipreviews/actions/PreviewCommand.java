package org.eclipselabs.guita.ipreviews.actions;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipselabs.guita.ipreviews.handler.VObtainerVisitor;
import org.eclipselabs.guita.ipreviews.handler.VResolverVisitor;
import org.eclipselabs.guita.ipreviews.handler.VResolverVisitor2;
import org.eclipselabs.guita.ipreviews.view.PreviewView;

public class PreviewCommand extends AbstractHandler {

	private static final String JDT_NATURE = "org.eclipse.jdt.core.javanature";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = page.getActiveEditor();


		IEditorInput input = editor.getEditorInput();
		IJavaElement e = JavaUI.getEditorInputJavaElement(input);

		ISelection selection = editor.getSite().getSelectionProvider().getSelection();

		if(selection instanceof ITextSelection) {

			ITextSelection textSelection = ((ITextSelection) selection);
			System.out.println(textSelection.getStartLine() + " - " + textSelection.getEndLine());
			System.out.println(e);

			try {
				createAST((ICompilationUnit) e, textSelection.getStartLine()+1, textSelection.getEndLine()+1);
			} catch (JavaModelException e1) {
				e1.printStackTrace();
			}
		}

		return null;
	}



	private void createAST(ICompilationUnit unit, int start, int end)
			throws JavaModelException {
		// Now create the AST for the ICompilationUnits
		CompilationUnit parse = parse(unit);
		////			ArrayInitVisitor a_visitor = new ArrayInitVisitor();
		//			parse.accept(a_visitor);
		VObtainerVisitor visitor = new VObtainerVisitor();
		parse.accept(visitor);
		VResolverVisitor2 visitor2 = new VResolverVisitor2(visitor.getNodes(), start, end);
		parse.accept(visitor2);
		for(int i = 0; i != visitor2.getSwt_nodes().size(); i++){
			System.out.println("Variables " + (i+1) + ": " + visitor2.getSwt_nodes().get(i));
			System.out.println("Classes: " + (i+1) + ": " + visitor2.getSwt_nodes_classes().get(i));
		}
		try {
			PreviewView view = (PreviewView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("guidesign.previewview");
			view.constructPreview(visitor2.getSwt_nodes(), visitor2.getSwt_nodes_classes());
		} catch (PartInitException e) {
			e.printStackTrace();
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