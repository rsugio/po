package com.differencer.pi.editors;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.differencer.pi.Activator;
import com.differencer.pi.nodes.ConfigurationNode;
public class ServerTreeViewer extends TreeViewer implements IDoubleClickListener {
	public ServerTreeViewer(Composite parent) {
		super(parent);
		addDoubleClickListener(this);
	}
	@Override
	public void doubleClick(DoubleClickEvent event) {
		Object element = ((TreeSelection) event.getSelection()).getFirstElement();
		if (!(element instanceof ConfigurationNode)) return;
		ConfigurationNode leaf = (ConfigurationNode) element;
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(leaf.getName() + "." + leaf.getType());
		try {
			page.openEditor(new ServerStorageEditorInput((ConfigurationNode) element), desc.getId());
			// TODO open XML editor here
			// page.openEditor(new ServerStorageEditorInput((ConfigurationNode) element), "org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart");
		} catch (PartInitException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
	}
}
