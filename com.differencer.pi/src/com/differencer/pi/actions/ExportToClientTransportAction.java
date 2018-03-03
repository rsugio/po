package com.differencer.pi.actions;
import java.util.HashMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
import com.differencer.pi.Exporter;
import com.differencer.pi.nodes.ConfigurationNode;
public class ExportToClientTransportAction extends Action {
	private HashMap<ConfigurationNode, ConfigurationNode> nodes;
	private String transportDirectory;
	private String transportArchiveDirectory;
	public ExportToClientTransportAction(HashMap<ConfigurationNode, ConfigurationNode> n, String t, String a) {
		nodes = n;
		transportDirectory = t;
		transportArchiveDirectory = a;
		setText("Export to Client");
		setToolTipText("Export transport to client");
		setDescription("Export transport to file (.tpz)");
//		ImageDescriptor id = Activator.getImageDescriptor("icons/sample.gif"); // we
//		if (id != null) setDisabledImageDescriptor(id);
//		id = Activator.getImageDescriptor("icons/sample.gif");
//		if (id != null) {
//			setImageDescriptor(id);
//			setHoverImageDescriptor(id);
//		}
	}
	public void run() {
		String leftURL = ((ConfigurationNode) nodes.values().toArray()[0]).getServer().getURL();
		String confirmText = "Please, confirm differencing machine transport action";
		confirmText = confirmText + "\n from " + leftURL + " to ";
		for (ConfigurationNode i : nodes.values()) {
			String filename = transportDirectory + System.getProperty("file.separator") + i.getOBJECTNAME() + ".tpz";
			confirmText = confirmText + "\n" + filename;
		}
		if (MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Export Transport", confirmText)) {
			Job job = new Job("Export Transport") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						monitor.beginTask("Transport ", nodes.size());
						for (ConfigurationNode i: nodes.values()) {
							monitor.subTask("exporting " + i);
							HashMap<ConfigurationNode, ConfigurationNode> nodesitto = new HashMap<ConfigurationNode, ConfigurationNode>();
							nodesitto.put(i,i);
							Exporter.exportTransportToClient(nodesitto, transportDirectory);
							monitor.worked(1);
							if (monitor.isCanceled()) {
								Activator.log(Status.WARNING, "Export transport cancelled");
								return Status.CANCEL_STATUS;
							}
						}
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}
}