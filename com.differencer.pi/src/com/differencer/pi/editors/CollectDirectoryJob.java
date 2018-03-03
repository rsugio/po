package com.differencer.pi.editors;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.differencer.pi.nodes.DifferencerNodeEx;

public class CollectDirectoryJob extends Job {
	private CloseableHttpClient client;
	private Server description;
	DifferencerNodeEx dir_root;
	private ConfigurationNode dir_root_conf;
	private IProgressMonitor group;

	public CollectDirectoryJob(String name, Server d, CloseableHttpClient c, DifferencerNodeEx r, ConfigurationNode u, IProgressMonitor g) {
		super(name);
		description = d;
		client = c;
		dir_root = r;
		dir_root_conf = u;
		group = g;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
        HashMap<String, String> dirent = Differencer.getTypeList(description, client, s731dirsup);
		if (dirent == null) return Status.CANCEL_STATUS;
		monitor.beginTask("Collect Directory Job", dirent.size());
        List<Job> jobs = new ArrayList<Job>(); 
        for (String q:dirent.keySet()) {
        	DifferencerNodeEx node = new DifferencerNodeEx(dirent.get(q));
        	dir_root.addChild(node);
        	ConfigurationNode node_conf = new ConfigurationNode(description, node);
        	dir_root_conf.addChild(node_conf);
			Job entity = new CollectEntityJob(q, dirent.get(q), description, client, node_conf, s731dirsup);
			//entity.setProgressGroup(group, 1);
			entity.schedule();
			jobs.add(entity);
			if (monitor.isCanceled()) {
				Activator.log(Status.WARNING, "Configuration collection cancelled");
				return Status.CANCEL_STATUS;
			}
		}
        for (Job j: jobs)
			try {
				j.join();
				monitor.worked(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Activator.log(Status.INFO, "Directory configuration collected");
		return Status.OK_STATUS;
	}
	static String s731dirsup = "/dir/support/SimpleQuery";
}