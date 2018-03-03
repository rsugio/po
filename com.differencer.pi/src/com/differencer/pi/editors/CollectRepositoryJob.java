package com.differencer.pi.editors;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
import com.differencer.pi.nodes.ConfigurationNode;
import com.differencer.pi.nodes.DifferencerNodeEx;
import com.pinternals.diffo.DiffItem;
import com.pinternals.diffo.impl.DifferencerNode;

public class CollectRepositoryJob extends Job {
	private IProgressMonitor group;
	private CloseableHttpClient client;
	private Server description;
	DifferencerNodeEx rep_root;
	private ConfigurationNode rep_root_conf;

	public CollectRepositoryJob(String name, Server d, CloseableHttpClient c, DifferencerNodeEx r, ConfigurationNode u, IProgressMonitor g) {
		super(name);
		description = d;
		client = c;
		rep_root = r;
		rep_root_conf = u;
		group = g;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
        HashMap<String, String> repent = Differencer.getTypeList(description, client, s731repsup);
		if (repent == null) return Status.CANCEL_STATUS;
		monitor.beginTask("Collect Repository Job", repent.size());
        List<Job> jobs = new ArrayList<Job>(); 
        for (String q:repent.keySet()) {
        	DifferencerNodeEx node = new DifferencerNodeEx(repent.get(q));
        	rep_root.addChild(node);
        	ConfigurationNode node_conf = new ConfigurationNode(description, node);
        	rep_root_conf.addChild(node_conf);
			Job entity = new CollectEntityJob(q, repent.get(q), description, client, node_conf, s731repsup);
			//entity.setProgressGroup(group, 1);
			entity.schedule();
			jobs.add(entity);
			if (monitor.isCanceled()) {
				Activator.log(Status.WARNING, "Repository ñonfiguration collection cancelled");
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
        monitor.done();
		Activator.log(Status.INFO, "Repository configuration collected");
		return Status.OK_STATUS;
	}
	static String s731repsup = "/rep/support/SimpleQuery";
}
