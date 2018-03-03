package com.differencer.pi.editors;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.ide.ResourceUtil;
import org.xml.sax.SAXException;
import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
import com.differencer.pi.actions.ConfigurationCollectAction;
import com.differencer.pi.preferences.PreferenceConstants;
public class ServerEditorForm extends FormPage implements ModifyListener {
	public boolean mSourceDirty = false;
	private Server server = null;
	ServerEditor serverDescriptionEditor;
	Text textSID, textURL, textUSER, textPASSWORD;
	public ServerEditorForm(final FormEditor editor, final String id, final String title, InputStream s) throws ParserConfigurationException, SAXException, IOException, CoreException {
		super(editor, id, title);
		serverDescriptionEditor = (ServerEditor) editor;
		setServerDescription(s);
	}
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final FormToolkit toolkit = managedForm.getToolkit();
		final ScrolledForm form = managedForm.getForm();
		IPreferencesService service = Platform.getPreferencesService();
		String value = service.getString(Activator.PLUGIN_ID, PreferenceConstants.P_DATABASE_PATH, "not found database directory preference", null);
		form.setText(server == null ? "" : server.toString() + " Database location at " + value);
		final Composite body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		body.setLayout(layout);
		Label labelSID = new Label(body, SWT.NULL);
		labelSID.setText("SID:");
		textSID = new Text(body, SWT.BORDER);
		textSID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label labelURL = new Label(body, SWT.NULL);
		labelURL.setText("URL:");
		textURL = new Text(body, SWT.BORDER);
		textURL.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label labelUSER = new Label(body, SWT.NULL);
		labelUSER.setText("User:");
		textUSER = new Text(body, SWT.BORDER);
		textUSER.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label labelPASSWORD = new Label(body, SWT.NULL);
		labelPASSWORD.setText("Password:");
		textPASSWORD = new Text(body, SWT.BORDER | SWT.PASSWORD);
		textPASSWORD.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label labelSTRUCTURE = new Label(body, SWT.NULL);
		labelSTRUCTURE.setText("Structure:");
		Button getStructure = new Button(body, SWT.PUSH);
		getStructure.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getStructure.setText("Reload");
		getStructure.setToolTipText("Reload structure from database");
		getStructure.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Differencer.reloadStructure(server);
			}
		});
		Label labelCONFIGURATION = new Label(body, SWT.NULL);
		labelCONFIGURATION.setText("Configuration:");
		Button getConfiguration = new Button(body, SWT.PUSH);
		getConfiguration.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getConfiguration.setText("Get");
		getConfiguration.setToolTipText("Start configuration collection job");
		getConfiguration.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				ConfigurationCollectAction.startCollectionJob(server);
			}
		});
		try {
			setServerDescription(((IFile) ResourceUtil.getResource(serverDescriptionEditor.getEditorInput())).getContents());
		} catch (CoreException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		textSID.addModifyListener(this);
		textURL.addModifyListener(this);
		textUSER.addModifyListener(this);
		textPASSWORD.addModifyListener(this);
	}
	@Override
	public void modifyText(ModifyEvent e) {
		Text thetext = (Text) e.widget;
		if (thetext.equals(textSID)) {
			getServerDescription().setSID(thetext.getText());
			serverDescriptionEditor.getDocument().set(getServerDescription().getXML());
			mSourceDirty = true;
		} else if (thetext.equals(textURL)) {
			getServerDescription().setURL(thetext.getText());
			serverDescriptionEditor.getDocument().set(getServerDescription().getXML());
			mSourceDirty = true;
		} else if (thetext.equals(textUSER)) {
			getServerDescription().setUSER(thetext.getText());
			serverDescriptionEditor.getDocument().set(getServerDescription().getXML());
			mSourceDirty = true;
		} else if (thetext.equals(textPASSWORD)) {
			getServerDescription().setPASSWORD(thetext.getText());
			serverDescriptionEditor.getDocument().set(getServerDescription().getXML());
			mSourceDirty = true;
		}
	}
	public void setServerDescription(InputStream s) {
		try {
			if (server == null) server = new Server(s);
			else server.setXML(s);
		} catch (ParserConfigurationException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (SAXException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (IOException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (CoreException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		if (textSID != null) textSID.setText(server.getSID());
		if (textURL != null) textURL.setText(server.getURL());
		if (textUSER != null) textUSER.setText(server.getUSER());
		if (textPASSWORD != null) textPASSWORD.setText(server.getPASSWORD());
	}
	public Server getServerDescription() {
		return server;
	}
}
