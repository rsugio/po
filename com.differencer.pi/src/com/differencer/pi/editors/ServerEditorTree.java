package com.differencer.pi.editors;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.xml.sax.SAXException;
import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
import com.differencer.pi.preferences.PreferenceConstants;
public class ServerEditorTree extends FormPage implements ModifyListener, ServerListener {
	ServerEditor serverDescriptionEditor;
	ServerTreeViewer treeViewer;
	public ServerEditorTree(final FormEditor editor, final String id, final String title, InputStream s) throws ParserConfigurationException, SAXException, IOException, CoreException {
		super(editor, id, title);
		serverDescriptionEditor = (ServerEditor) editor;
		serverDescriptionEditor.server.addListener(this);
	}
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final FormToolkit toolkit = managedForm.getToolkit();
		final ScrolledForm form = managedForm.getForm();
		IPreferencesService service = Platform.getPreferencesService();
		String value = service.getString(Activator.PLUGIN_ID, PreferenceConstants.P_DATABASE_PATH, "not found database directory preference", null);
		form.setText("PI site tree" + " Database location at " + value);
		Composite body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
        FillLayout fillLayout = new FillLayout();
        fillLayout.type = SWT.VERTICAL;
		body.setLayout(fillLayout);
		//body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer = new ServerTreeViewer(body);
		treeViewer.setContentProvider(new ServerTreeContentProvider());
		//treeViewer.setLabelProvider(new ServerDescriptionTreeLabelProvider());
		treeViewer.setInput(getInitalInput());
		//treeViewer.expandAll();
	}
	private Object getInitalInput() {
		return Differencer.getTree(serverDescriptionEditor.server);
	}
	@Override
	public void modifyText(ModifyEvent e) {
	}
	@Override
	public void serverChanged(Server server) {
		treeViewer.setInput(Differencer.getTree(serverDescriptionEditor.server));
	}
}
