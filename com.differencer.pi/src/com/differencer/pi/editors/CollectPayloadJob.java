package com.differencer.pi.editors;

import java.io.IOException;
import java.util.ArrayList;
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

public class CollectPayloadJob extends Job {
	private CloseableHttpClient client;
	private Server description;
	private String url;

	public CollectPayloadJob(String name, Server d, CloseableHttpClient c, String u) {
		super(name);
		description = d;
		client = c;
		url = u;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			HttpPost p = new HttpPost(url);
			CloseableHttpResponse response;
			response = client.execute(p, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String s = EntityUtils.toString(response.getEntity());
				System.out.println("\nurl " + url + "\nname " + getName() + "\ntotal " + s.length());
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

}
