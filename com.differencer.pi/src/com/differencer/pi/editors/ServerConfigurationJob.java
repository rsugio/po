package com.differencer.pi.editors;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
public class ServerConfigurationJob extends Job {
	private Server description;
	public ServerConfigurationJob(String name, Server d) {
		super(name);
		description = d;
	}
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			Differencer.collectConfiguration(description);
			monitor.beginTask("Doing something time consuming here", 100);
			for (int i = 0; i < 5; i++) {
				monitor.subTask("I'm doing something here " + i);
				monitor.worked(20);
				if (monitor.isCanceled()) {
					Activator.log(Status.WARNING, "Configuration collection cancelled");
					return Status.CANCEL_STATUS;
				}
			}
		} finally {
			monitor.done();
		}
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Activator.log(Status.INFO, "Configuration collected");
				// CompareUI.openCompareEditor(new
				// CompareInput());
			}
		});
		return Status.OK_STATUS;
	}
}
