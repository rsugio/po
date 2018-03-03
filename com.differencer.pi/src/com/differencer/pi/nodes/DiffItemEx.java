package com.differencer.pi.nodes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.mozilla.universalchardet.UniversalDetector;

import com.differencer.pi.Differencer;
import com.differencer.pi.editors.Server;
import com.pinternals.diffo.UUtil;

public class DiffItemEx {
	private static Logger log = Logger.getLogger(DiffItemEx.class.getName());
	public String key;
	public String name;
	public byte[] version_id, object_id;
	public boolean deleted, db = false;
	public long oref;
	public String url;
	
	private HashMap<String,String> hmTN = null;
	private byte[] payload = null;
	private Server description = null;
	private boolean isXML = false;
	
	public DiffItemEx (Server d, String k, String n, byte[] oid, byte[] vid, long ref, boolean del, String u) {
		key = k;
		name = n;
		object_id = oid;
		version_id = vid;
		deleted = del;
		oref = ref;
		url = u;
		description = d;
	}
	
	public void setPayload(byte[] p) throws IOException {
		payload = p;
		isXML = p[0] == 60 && p[1] == 63 && p[2] == 120 && p[3] == 109 && p[4] == 108 && p[5] == 32; //(new String(payload).contains("<?xml version=\"1.0\""));
		if (!isXML) payload = null;
	}
	public InputStream getPayload() throws SQLException, IOException {
		if (payload != null && isXML ) return new ByteArrayInputStream(payload);
		ByteArrayInputStream bis = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<DeletedObject></DeletedObject>".getBytes("UTF-8"));
		if (!deleted) {
			try {//TODO decide how to better use it for
				HttpPost post = new HttpPost(url);
				CloseableHttpResponse httpresponse = null;
				CloseableHttpClient client = Differencer.login(description);
				httpresponse = client.execute(post, HttpClientContext.create());
				httpresponse.getStatusLine().getStatusCode();
				if (httpresponse.getStatusLine().getStatusCode() > 200) {
					setPayload(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<FailedPayload>" + "'" + url.replaceAll("&", "&amp;") + "' " + httpresponse.getStatusLine() + "</FailedPayload>").getBytes("UTF-8"));
					return new ByteArrayInputStream(payload);
				}
				HttpEntity instance = httpresponse.getEntity();
				if (instance != null) {
					String out = EntityUtils.toString(instance, "cp1251");
//					byte[] buf = new byte[4096];
//					UniversalDetector detector = new UniversalDetector(null);
//					int nread;
//					byte[] b = EntityUtils.toByteArray(instance);
//					ByteArrayInputStream cis = new ByteArrayInputStream(b);
//					while ((nread = cis.read(buf)) > 0 && !detector.isDone()) {
//					  detector.handleData(buf, 0, nread);
//					}
//					detector.dataEnd();
//					String encoding = detector.getDetectedCharset();
//					if (encoding != null) {
//					  System.out.println(key + " " + name + "Detected encoding = " + encoding);
//					} else {
//					  System.out.println(key + " " + name + "No encoding detected.");
//					}
//					detector.reset();
					setPayload(out.getBytes("UTF-8"));
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bis = new ByteArrayInputStream(payload); 
		}
		//GZIPInputStream gis = new GZIPInputStream(bis);
		return bis;
	}
	
	public String getTransportAttr(String an)  {
//		if (hmTN==null) 
//			try {
//				hmTN = d.readTransportNames(oref);
//			} catch (Exception e) {
//				throw new RuntimeException("Error when read transport attributes for object " + oref);
//			}
//		return hmTN.get(an);
		return "false";
	}
//	public String getSWCV() {
//		assert oref!=0 : "Object reference (oref) must be set before getting SWCV";
//		assert db : "DiffItem is not retrieved from DB yet";
//		return UUtil.getStringUUIDfromBytes(swcvid);
//	}
	
}
