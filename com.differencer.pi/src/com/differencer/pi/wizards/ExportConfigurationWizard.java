package com.differencer.pi.wizards;
import java.util.List;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import com.differencer.pi.Activator;
public class ExportConfigurationWizard extends Wizard implements IExportWizard {
	private IStructuredSelection selection;
	private ExportConfigurationWizardPage mainPage;
	public ExportConfigurationWizard() {
		IDialogSettings workbenchSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("piPayloadExportWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("piPayloadExportWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}
	public void addPages() {
		super.addPages();
		mainPage = new ExportConfigurationWizardPage(selection);
		addPage(mainPage);
	}
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.selection = currentSelection;
		List selectedResources = IDE.computeSelectedResources(currentSelection);
		if (!selectedResources.isEmpty()) {
			this.selection = new StructuredSelection(selectedResources);
		}
		if (selection.isEmpty() && workbench.getActiveWorkbenchWindow() != null) {
			IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
				IEditorPart currentEditor = page.getActiveEditor();
				if (currentEditor != null) {
					Object selectedResource = currentEditor.getEditorInput().getAdapter(IResource.class);
					if (selectedResource != null) {
						selection = new StructuredSelection(selectedResource);
					}
				}
			}
		}
		setWindowTitle("Export PI Payload");
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/sample.gif"));//$NON-NLS-1$
		setNeedsProgressMonitor(true);
	}
	public boolean performFinish() {
		return mainPage.finish();
	}
}
