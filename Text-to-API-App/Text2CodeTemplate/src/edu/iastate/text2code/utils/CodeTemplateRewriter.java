package edu.iastate.text2code.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class CodeTemplateRewriter {
	
	// insert comments right below the Method Declaration given position  
	public static void insertComments(String comment, String code, int pos) {
		ICompilationUnit unit = getICompilationUnit();
		CompilationUnit astRoot = parse(unit);
	 
		//create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
	 
		//for getting insertion position
		MethodDeclaration cursoredMethod = ASTUtils.findMethodDeclaration(pos, astRoot);
		Block block = cursoredMethod.getBody();
	 
		ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) rewriter.createStringPlaceholder("//" + comment +"\n" + code
				, ASTNode.EMPTY_STATEMENT);
		listRewrite.insertLast(placeHolder, null);
	 
		TextEdit edits = null;
		try {
			edits = rewriter.rewriteAST();
		}
		catch (JavaModelException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	 
		// apply the text edits to the compilation unit
		Document document = null;
		try {
			document = new Document(unit.getSource());
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
	 
		try {
			edits.apply(document);
		}
		catch (MalformedTreeException | BadLocationException e) {
			e.printStackTrace();
		}
	 
		// insert statements
		try {
			unit.getBuffer().setContents(document.get());
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		ASTUtils.selectInEditor(cursoredMethod.getStartPosition(), 0);
		System.out.println("done at " + cursoredMethod.getStartPosition());
	}
	
	public static void insertCodeTemplate(String code, int pos){
		ICompilationUnit unit = getICompilationUnit();
		CompilationUnit astRoot = parse(unit);
	 
		//create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
	 
		//for getting insertion position
		MethodDeclaration cursoredMethod = ASTUtils.findMethodDeclaration(pos, astRoot);
		Block block = cursoredMethod.getBody();
 
		// create new statements for insertion
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName("add"));
		Statement newStatement = ast.newExpressionStatement(newInvocation);
		
		//create ListRewrite
		ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		listRewrite.insertFirst(newStatement, null);
		TextEdit edits = null;;
		try {
			edits = rewriter.rewriteAST();
		}
		catch (JavaModelException | IllegalArgumentException e) {
			e.printStackTrace();
		}
 
		// apply the text edits to the compilation unit
		Document document = null;
		try {
			document = new Document(unit.getSource());
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
 
		try {
			edits.apply(document);
		}
		catch (MalformedTreeException | BadLocationException e) {
			e.printStackTrace();
		}
 
		// add statements
		try {
			unit.getBuffer().setContents(document.get());
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	public static void insertImportDeclaration() {
		ICompilationUnit unit = getICompilationUnit();
		CompilationUnit astRoot = parse(unit);
	 
		//create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		
		// create a new import statement
		ImportDeclaration newImportDecl = ast.newImportDeclaration();
		SimpleName javaSimple = ast.newSimpleName("java");
		org.eclipse.jdt.core.dom.QualifiedName qualName = ast.newQualifiedName(javaSimple, ast.newSimpleName("FileInputStream"));
		newImportDecl.setName(qualName);
		newImportDecl.setOnDemand(true);
	}
	
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
	
	private static ICompilationUnit getICompilationUnit() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ITextEditor activeEditor = (ITextEditor) page.getActiveEditor();
		IJavaElement javaElem = JavaUI.getEditorInputJavaElement(activeEditor.getEditorInput());
		
		if(javaElem instanceof ICompilationUnit) {
			ITextSelection sel = (ITextSelection) activeEditor.getSelectionProvider().getSelection();
	    IJavaElement selected = null;
			try {
				selected = ((ICompilationUnit) javaElem).getElementAt(sel.getOffset());
			}
			catch (JavaModelException e) {
				e.printStackTrace();
			}
	    if (selected != null && selected.getElementType() == IJavaElement.METHOD) {
	         return ((IMethod) selected).getCompilationUnit();
	    }
		}
		return null;
	}

}
