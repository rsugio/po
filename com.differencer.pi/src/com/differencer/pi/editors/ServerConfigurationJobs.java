package com.differencer.pi.editors;
import java.util.Iterator;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
import com.differencer.pi.nodes.ConfigurationNode;
import com.differencer.pi.nodes.DiffItemEx;
import com.differencer.pi.nodes.DifferencerNodeEx;
import com.pinternals.diffo.DiffItem;
import com.pinternals.diffo.Diffo;
import com.pinternals.diffo.PiEntity;
import com.pinternals.diffo.PiHost;
import com.pinternals.diffo.Side;
import com.pinternals.diffo.api.IDifferencerNode;
import com.pinternals.diffo.impl.DifferencerNode;
public class ServerConfigurationJobs extends Job {
	private Server description;
	private IProgressMonitor group;
	public ServerConfigurationJobs(String name, Server d, IProgressMonitor g) {
		super(name);
		description = d;
		group = g;
	}
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		description.root = new ConfigurationNode(description, new DifferencerNodeEx("root"));
		DifferencerNodeEx rep_root = new DifferencerNodeEx("Repository");
		ConfigurationNode rep_root_conf = new ConfigurationNode(description, rep_root);
		description.root.addChild(rep_root_conf);
		DifferencerNodeEx dir_root = new DifferencerNodeEx("Directory");
		ConfigurationNode dir_root_conf = new ConfigurationNode(description, dir_root);
		description.root.addChild(dir_root_conf);
		try {
	        monitor.beginTask("Collect " + description.getSID(), 2);
	        CloseableHttpClient client = Differencer.login(description);
			if(client == null) return Status.CANCEL_STATUS;
			Job rep = new CollectRepositoryJob("Collect Repository " + description.getSID(), description, client, rep_root, rep_root_conf, group);
	        //rep.setProgressGroup(group, 1);
	        rep.schedule();
	        Job dir = new CollectDirectoryJob("Collect Directory " + description.getSID(), description, client, dir_root, dir_root_conf, group);
	        //dir.setProgressGroup(group, 1);
	        dir.schedule();
	        try {

				//rep.join();
				monitor.worked(1);
		        dir.join();
				monitor.worked(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } finally {
	        monitor.done();
	    }
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Activator.log(Status.INFO, "Collection started");
				// CompareUI.openCompareEditor(new
				// CompareInput());
			}
		});
		return Status.OK_STATUS;
	}
}
