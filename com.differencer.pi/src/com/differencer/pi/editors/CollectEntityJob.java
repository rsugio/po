package com.differencer.pi.editors;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
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
import com.differencer.pi.nodes.DiffItemEx;
import com.differencer.pi.nodes.DifferencerNodeEx;

public class CollectEntityJob extends Job {
	private CloseableHttpClient client;
	private Server description;
	private ConfigurationNode node_conf;
	private String key;
	private String path;
	public CollectEntityJob(String k, String name, Server d, CloseableHttpClient c, ConfigurationNode u, String p) {
		super(name);
		description = d;
		key = k;
		client = c;
		node_conf = u;
		path = p;
	}
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Reader reader;
		HTMLEditorKit.Parser parser;
		//if (!node_conf.getName().equals("External Definition")) return Status.CANCEL_STATUS;
		if (key.equals("workspace") || key.contains("aris") || key.contains("ARIS")) return Status.CANCEL_STATUS; //TODO show stopper
		try {
			HttpPost request = new HttpPost(description.getURL() + path);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			BasicNameValuePair deleted = new BasicNameValuePair("deletedL", "N");
			if (path.contains("/rep/")) nvps.add(new BasicNameValuePair("qc", "All software components"));
			else if (path.contains("/dir/")) nvps.add(new BasicNameValuePair("qc", "Default (for directory objects)"));
			nvps.add(new BasicNameValuePair("syncTabL", "true"));
			nvps.add(deleted);
			nvps.add(new BasicNameValuePair("xmlReleaseL", "7.1"));
			nvps.add(new BasicNameValuePair("queryRequestXMLL", ""));
			nvps.add(new BasicNameValuePair("types", key));
			nvps.add(new BasicNameValuePair("qcActiveL0", "true"));
			nvps.add(new BasicNameValuePair("qcValueL0", ""));
			nvps.add(new BasicNameValuePair("result", "FOLDERREF"));
			nvps.add(new BasicNameValuePair("action", "Start query"));
			HttpEntity entity = null;
			CloseableHttpResponse response = null;
			request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
			long collectionEntityTime = System.currentTimeMillis();
			response = client.execute(request, HttpClientContext.create());
			switch (response.getStatusLine().getStatusCode()) {
				case 500: {
					Activator.log(Status.ERROR, "Entity page dured: " + (System.currentTimeMillis() - collectionEntityTime) + " for " + node_conf.getName() + "@" + description.getSID() + " " + response.getStatusLine());
					//Job job = new CollectEntityJob(key, this.getName() + " >", description, client, node_conf, path);
					//job.schedule();
					return Status.CANCEL_STATUS;
				}
			}
			//Activator.log(Status.INFO, "Entity page dured: " + (System.currentTimeMillis() - collectionEntityTime) + " for " + node_conf.getName() + "@" + description.getSID() + " " + response.getStatusLine());
			entity = response.getEntity();
			if (entity == null) {
				Activator.log(Status.ERROR, "Entity page empty entity: " + node_conf.getName() + "@" + description.getSID() + "\nURI " + request.getURI() + "\nNameValues " + EntityUtils.toString(request.getEntity()));
				return Status.CANCEL_STATUS;
			}
			String s = EntityUtils.toString(response.getEntity());
			int beginIndex = s.indexOf("<p><input type=submit name=action value=\"Start query\" /></p>");
			if (beginIndex < 0) {
				Activator.log(Status.ERROR, "Entity page empty begin index: " + node_conf.getName() + "@" + description.getSID() + "\nURI " + request.getURI() + "\nNameValues " + EntityUtils.toString(request.getEntity()) + "\n" + s);
				return Status.CANCEL_STATUS;
			}
			String str = s.substring(beginIndex);
			int beginIndexAmountTotal = str.indexOf("<h3>Amount of objects: ");
			int endIndexAmountTotal = str.indexOf("</h3>", beginIndexAmountTotal);
			int amountTotal = Integer.valueOf(str.substring(beginIndexAmountTotal + "<h3>Amount of objects: ".length(), endIndexAmountTotal)).intValue();
			if (amountTotal == 0) {
				Activator.log(Status.WARNING, "Entity page empty amount total: " + node_conf.getName() + "@" + description.getSID() + "\nURI " + request.getURI() + "\nNameValues " + EntityUtils.toString(request.getEntity()));
				return Status.CANCEL_STATUS;
			}
			monitor.beginTask("Parse " + key, amountTotal);
			nvps.add(new BasicNameValuePair("result", "TEXT"));
			nvps.add(new BasicNameValuePair("result", "NAME"));
			nvps.add(new BasicNameValuePair("result", "NAMESPACE"));
			nvps.add(new BasicNameValuePair("result", "OBJECTID"));
			nvps.add(new BasicNameValuePair("result", "VERSIONID"));
			nvps.add(new BasicNameValuePair("result", "RA_XILINK"));
			nvps.add(new BasicNameValuePair("result", "OWNER"));
			nvps.remove(deleted);
			nvps.add(new BasicNameValuePair("deletedL", "N"));
			entity = null;
			response = null;
			request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
			response = client.execute(request, HttpClientContext.create());
			switch (response.getStatusLine().getStatusCode()) {
				case 500: {
					Activator.log(Status.ERROR, "Entity collect dured: " + (System.currentTimeMillis() - collectionEntityTime) + " for " + node_conf.getName() + "@" + description.getSID() + " " + response.getStatusLine());
					Job job = new CollectEntityJob(key, this.getName() + " <", description, client, node_conf, path);
					job.schedule();
					return Status.CANCEL_STATUS;
				}
			}
			entity = response.getEntity();
			if (entity == null) {
				Activator.log(Status.ERROR, "Entity collection empty entity object: " + node_conf.getName() + "@" + description.getSID() + "\nURI " + request.getURI() + "\nNameValues " + EntityUtils.toString(request.getEntity()));
				return Status.CANCEL_STATUS;
			}
			s = EntityUtils.toString(response.getEntity());
			beginIndex = s.indexOf("<p><input type=submit name=action value=\"Start query\" /></p>");
			if (beginIndex < 0) {
				Activator.log(Status.ERROR, "Entity collection empty begin index object: " + node_conf.getName() + "@" + description.getSID() + "\nURI " + request.getURI() + "\nNameValues " + EntityUtils.toString(request.getEntity()) + "\n" + s);
				return Status.CANCEL_STATUS;
			}
			str = s.substring(beginIndex);
			int beginIndexAmount = str.indexOf("<h3>Amount of objects: ");
			int endIndexAmount = str.indexOf("</h3>", beginIndexAmount);
			int amount = Integer.valueOf(str.substring(beginIndexAmount + "<h3>Amount of objects: ".length(), endIndexAmount)).intValue();
			if (amount == 0) {
				Activator.log(Status.WARNING, "Entity collection empty amount: " + node_conf.getName() + "@" + description.getSID() + "\nURI " + request.getURI() + "\nNameValues " + EntityUtils.toString(request.getEntity()));
				return Status.CANCEL_STATUS;
			}
			reader = new StringReader(str);
			parser = new ParserDelegator();
			parser.parse(reader, new HTMLTableParser(description, node_conf, false, monitor), true);
			reader.close();
			response.close();

//			nvps.remove(deleted);
//			nvps.add(new BasicNameValuePair("deletedL", "D"));
//			entity = null;
//			response = null;
//			request.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
//			response = client.execute(request, HttpClientContext.create());
//			entity = response.getEntity();
//			if (entity == null) return Status.CANCEL_STATUS;
//			s = EntityUtils.toString(response.getEntity());
//			beginIndex = s.indexOf("<p><input type=submit name=action value=\"Start query\" /></p>");
//			if (beginIndex < 0) return Status.CANCEL_STATUS;
//			str = s.substring(beginIndex);
//			int beginIndexAmountDeleted = str.indexOf("<h3>Amount of objects: ");
//			int endIndexAmountDeleted = str.indexOf("</h3>", beginIndexAmountDeleted);
//			int amountDeleted = Integer.valueOf(str.substring(beginIndexAmountDeleted + "<h3>Amount of objects: ".length(), endIndexAmountDeleted)).intValue();
//			if (amountDeleted == 0) return Status.CANCEL_STATUS;
//			reader = new StringReader(str);
//			parser = new ParserDelegator();
//			parser.parse(reader, new HTMLTableParser(description, node_conf, true, monitor), true);
//			reader.close();
//			response.close();
/**/
			Calendar cal = Calendar.getInstance();
	    	SimpleDateFormat sdf = new SimpleDateFormat("mm");
	    	int minuten = Integer.parseInt(sdf.format(cal.getTime()));
	    	Object[] instancesEx = node_conf.getChildren();
	    	monitor.beginTask("Collect payloads", instancesEx.length);
	    	for (Object o: instancesEx) {
				ConfigurationNode item = (ConfigurationNode) o;
				HttpPost post = new HttpPost(((DifferencerNodeEx) item.node).item.url);
				CloseableHttpResponse httpresponse = null;
		    	int minutennow = Integer.parseInt(sdf.format(cal.getTime()));
				if ( ((minutennow - minuten) & 1) != 0 ) {
					client = Differencer.login(description);
					minuten = minutennow;
				}
				try {//TODO decide how to better use it for
					httpresponse = client.execute(post, HttpClientContext.create());
				} catch (NoHttpResponseException ee) {
					System.out.println("retry for " + ((DifferencerNodeEx) item.node).item.url);
					client = Differencer.login(description);
					httpresponse = client.execute(post, HttpClientContext.create());
				}
				HttpEntity instance = httpresponse.getEntity();
				if (instance != null) {
					//String out = EntityUtils.toString(instance, "UTF-8");
					((DifferencerNodeEx) item.node).item.setPayload(EntityUtils.toByteArray(instance));
//					StringBuilder builder = new StringBuilder();
//					InputStreamReader isr = new InputStreamReader(instance.getContent(), "Cp1251");
//				    while (isr.ready()) {
//				      builder.append(isr.read());
//				    }
//				    isr.close();
//				    String string = builder.toString();
//				    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				    Writer writer = new OutputStreamWriter(baos, "UTF-8");
//				    writer.write(string);
//				    writer.flush();
//				    writer.close();
				    //String payload = EntityUtils.toString(instance, "Cp1251");
					//((DifferencerNodeEx) item.node).item.setPayload(baos.toString("UTF-8").getBytes("UTF-8"));
//sapweb.gazprom-neft.local [10.80.67.245]if(payload.contains("smdportal") || payload.contains("10.80.67.245") || payload.contains("sapweb")) System.out.println("/n" + item.getElementName() + ", " + item.getName());
				}
				monitor.worked(1);
				if (monitor.isCanceled()) {
					Activator.log(Status.WARNING, "Configuration collection cancelled");
					return Status.CANCEL_STATUS;
				}
				Thread.yield();
			}
/**/
			monitor.done();
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
//"NAMESPACE"	"Namespace"	"108"
//"NAME"	"Object ID"	"152"
//"TEXT"	"Description"	"172"
//"OWNER"	"Person Responsible"	"176"
//"MODIFYDATE"	"Changed On"	"178"
//"MODIFYUSER"	"Changed By"	"178"
//"FOLDERREF"	"FOLDERREF"	"180"
//"OBJECTID"	"OBJECTID"	"180"
//"Q_TEXT_LABEL_LONG"	"Q_TEXT_LABEL_LONG"	"180"
//"Q_TEXT_LABEL_SHORT"	"Q_TEXT_LABEL_SHORT"	"180"
//"Q_TEXT_SHORT"	"Q_TEXT_SHORT"	"180"
//"RA_CHECK_EXISTENCE_OF_TYPE"	"RA_CHECK_EXISTENCE_OF_TYPE"	"180"
//"RA_CHECK_EXISTENCE_OF_TYPEnot"	"RA_CHECK_EXISTENCE_OF_TYPEnot"	"180"
//"RA_LINK_LIST"	"RA_LINK_LIST"	"180"
//"RA_LINK_LIST_ROLE"	"RA_LINK_LIST_ROLE"	"180"
//"RA_LINK_LIST_ROLE_POS"	"RA_LINK_LIST_ROLE_POS"	"180"
//"RA_LINK_POSITION"	"RA_LINK_POSITION"	"180"
//"RA_LINK_RESOLVEMODE"	"RA_LINK_RESOLVEMODE"	"180"
//"RA_LINK_ROLE"	"RA_LINK_ROLE"	"180"
//"RA_LINK_TARGET_OID"	"RA_LINK_TARGET_OID"	"180"
//"RA_LINK_TARGET_REFERENCE"	"RA_LINK_TARGET_REFERENCE"	"180"
//"RA_LINK_TARGET_TYPE"	"RA_LINK_TARGET_TYPE"	"180"
//"RA_TEXT_LANGUAGE_LONG"	"RA_TEXT_LANGUAGE_LONG"	"180"
//"RA_TEXT_LANGUAGE_SHORT"	"RA_TEXT_LANGUAGE_SHORT"	"180"
//"RA_TEXT_LONG"	"RA_TEXT_LONG"	"180"
//"RA_XILINK"	"Link to object"	"180"
//"VERSIONID"	"VERSIONID"	"180"
