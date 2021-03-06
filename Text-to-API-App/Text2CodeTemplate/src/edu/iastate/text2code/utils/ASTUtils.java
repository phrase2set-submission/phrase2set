package edu.iastate.text2code.utils;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.iastate.text2code.Activator;

public class ASTUtils {
	
	@SuppressWarnings("rawtypes")
	/** Find method declaration based on position to insert additional comments and statements */
	public static MethodDeclaration findMethodDeclaration(int offset, CompilationUnit astRoot){
		MethodDeclaration methodDecl = null;
		
		List decls =((TypeDeclaration) astRoot.types().get(0)).bodyDeclarations();
		for (Iterator iterator = decls.iterator(); iterator.hasNext();){
			BodyDeclaration decl = (BodyDeclaration) iterator.next();
			if(decl instanceof MethodDeclaration){
				methodDecl = (MethodDeclaration) decl;
				int startRange = methodDecl.getBody().getStartPosition();
				int endRange = methodDecl.getBody().getStartPosition() + methodDecl.getBody().getLength();
				if(offset > startRange && offset < endRange){
					return methodDecl;
				}
			}
		}
		return methodDecl;
	}
	
	/** Jump to the line of code that has been inserted and hightlight */
	public static void selectInEditor(int offset, int length) {
		IEditorPart active = getActiveEditor();
		if (active instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) active;
			editor.selectAndReveal(offset, length);
		}
	}

	/** Current active editor */
	public static IEditorPart getActiveEditor() {
		IWorkbenchWindow window= Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page= window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}
		return null;
	}
	
	/** Position of current cursor */
	public static int getCurrentCursor() {
		IEditorPart editor =  Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof ITextEditor) {
		  ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
		  ISelection selection = selectionProvider.getSelection();
		  if (selection instanceof ITextSelection) {
		    ITextSelection textSelection = (ITextSelection)selection;
		    int offset = textSelection.getOffset(); // etc.
		    System.out.println("Offset: " + offset);
		    return offset;
		  }
		}
		return 0;
	}

}
