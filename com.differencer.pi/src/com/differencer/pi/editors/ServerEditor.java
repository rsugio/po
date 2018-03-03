package com.differencer.pi.editors;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.xml.sax.SAXException;
import com.differencer.pi.Activator;
public class ServerEditor extends FormEditor implements IDocumentListener {
	public Server server;
	private ServerEditorForm serverFormEditor;
	private int mFormEditorIndex;
	private ServerEditorTree serverTreeEditor;
	private int mTreeEditorIndex;
	private TextEditor serverSourceEditor;
	private int mSourceEditorIndex;
	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		super.init(site, input);
		try {
			serverFormEditor = new ServerEditorForm(this, "formID", "Server Page", ((FileEditorInput) input).getFile().getContents());
			server = serverFormEditor.getServerDescription();
			serverTreeEditor = new ServerEditorTree(this, "treeID", "Server Tree", ((FileEditorInput) input).getFile().getContents());
		} catch (ParserConfigurationException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (SAXException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (IOException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (CoreException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
	}
	@Override
	protected void addPages() {
		serverSourceEditor = new TextEditor();
		//mSourceEditor.setEditorPart(this);
		try {
			// add form pages
			mFormEditorIndex = addPage(serverFormEditor);
			mTreeEditorIndex = addPage(serverTreeEditor);
			// add source page
			mSourceEditorIndex = addPage(serverSourceEditor, getEditorInput());
			setPageText(mSourceEditorIndex, "Source");
		} catch (final PartInitException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		// add listener for changes of the document source
		getDocument().addDocumentListener(this);
	}
	@Override
	public void doSaveAs() {
		// not allowed
	}
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	@Override
	public void doSave(final IProgressMonitor monitor) {
		if (getActivePage() != mSourceEditorIndex) updateSourceFromModel();
		serverSourceEditor.doSave(monitor);
	}
	@Override
	protected void pageChange(final int newPageIndex) {
		if (newPageIndex == mFormEditorIndex && (serverFormEditor.mSourceDirty)) updateModelFromSource();
		if (newPageIndex == mTreeEditorIndex && (serverFormEditor.mSourceDirty)) ;
		if (newPageIndex == mSourceEditorIndex && (serverFormEditor.mSourceDirty)) updateSourceFromModel();
		super.pageChange(newPageIndex);
		final IFormPage page = getActivePageInstance();
		if (page != null) {
			// TODO update form page with new model data
			page.setFocus();
		}
	}
	private void updateModelFromSource() {
		serverFormEditor.setServerDescription(new ByteArrayInputStream(getDocument().get().getBytes()));
		serverFormEditor.mSourceDirty = false;
	}
	private void updateSourceFromModel() {
		getDocument().set(serverFormEditor.getServerDescription().getXML());
		serverFormEditor.mSourceDirty = false;
	}
	public IDocument getDocument() {
		final IDocumentProvider provider = serverSourceEditor.getDocumentProvider();
		return provider.getDocument(getEditorInput());
	}
	public IFile getFile() {
		final IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput) return ((FileEditorInput) input).getFile();
		return null;
	}
	public String getContent() {
		return getDocument().get();
	}
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
	}
	@Override
	public void documentChanged(DocumentEvent event) {
		serverFormEditor.mSourceDirty = true;
	}
}
