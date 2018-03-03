package com.differencer.pi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.http.impl.client.CloseableHttpClient;

import com.differencer.pi.editors.Server;
import com.differencer.pi.nodes.ConfigurationNode;
import com.differencer.pi.preferences.PreferenceConstants;
import com.pinternals.diffo.api.DifferencerFactory;
import com.pinternals.diffo.api.IDiffo;
public class Differencer {
	private static IDiffo diffo = null;
	private static HashMap<Server, ConfigurationNode> structures = new HashMap<Server, ConfigurationNode>();
	public static boolean initDatabase() {
		try {
			return (!diffo.opendb() || !diffo.createdb());
		} catch (ClassNotFoundException e) {
			Activator.log(Status.ERROR, "initDatabase failed", e);
		} catch (SQLException e) {
			Activator.log(Status.ERROR, "initDatabase failed", e);
		}
		return false;
	}
	public static void openDatabase() {
		IPreferencesService service = Platform.getPreferencesService();
		String directory = service.getString(Activator.PLUGIN_ID, PreferenceConstants.P_DATABASE_PATH, "not found database directory preference!", null);
		String file = service.getString(Activator.PLUGIN_ID, PreferenceConstants.P_DATABASE_FILE, "not found database file preference!", null);
		String database = directory + System.getProperty("file.separator") + file;
		Activator.log(Status.INFO, "Using Difference Database: " + database);
		diffo = DifferencerFactory.getDiffo(database, null, 10);
		try {
			if (diffo.opendb() && (diffo.isDbExist() || diffo.createdb())) {
				if (!diffo.start_session()) Activator.log(Status.ERROR, "session not started!");
			}
		} catch (SQLException e) {
			Activator.log(Status.ERROR, "openDatabase failed", e);
		} catch (ClassNotFoundException e) {
			Activator.log(Status.ERROR, "openDatabase failed", e);
		}
	}
	public static void closeDatabase() {
		try {
			diffo.finish_session();
			diffo.closedb();
			DifferencerFactory.remove(diffo);
			diffo = null;
			Activator.log(Status.INFO, "closeDatabase failed");
		} catch (SQLException e) {
			Activator.log(Status.ERROR, "closeDatabase failed", e);
		}
	}
	public static void collectConfiguration(Server description) {
		try {
			diffo.refresh(description.getSID(), description.getURL(), description.getUSER(), description.getPASSWORD());
			structures.remove(description);
		} catch (SQLException e) {
			Activator.log(Status.ERROR, "collectConfiguration failed", e);
		} catch (IOException e) {
			Activator.log(Status.ERROR, "collectConfiguration failed", e);
		} catch (SAXException e) {
			Activator.log(Status.ERROR, "collectConfiguration failed", e);
		} catch (ParseException e) {
			Activator.log(Status.ERROR, "collectConfiguration failed", e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Object getTree(Server description) {
		return getStructure(description);
	}
	public static void exportXML(Server description, OutputStream os) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();
			ConfigurationNode structure = (ConfigurationNode) getStructure(description);
			Element root = document.createElement(structure.getElementName());
			root.setAttribute("name", structure.getName());
			document.appendChild(root);
			addChidren(document, root, structure);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		} catch (DOMException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		} catch (ParserConfigurationException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		} catch (TransformerException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		}
	}
	private static void addChidren(Document document, Element root, ConfigurationNode element) {
		Element j = document.createElement(element.getElementName());
		j.setAttribute("name", element.getName());
		root.appendChild(j);
		for (Object i : element.getChildren()) {
			addChidren(document, j, (ConfigurationNode) i);
		}
	}
	public static void exportXMLWithPayload(Server description, OutputStream os) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();
			ConfigurationNode structure = (ConfigurationNode) getStructure(description);
			Element root = document.createElement(structure.getElementName());
			root.setAttribute("name", structure.getName());
			document.appendChild(root);
			addChidrenWithPayload(document, root, structure);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		} catch (DOMException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		} catch (ParserConfigurationException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		} catch (TransformerException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		} catch (SAXException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		} catch (IOException e) {
			Activator.log(Status.ERROR, "exportXML failed", e);
		}
	}
	public static String convertStreamToString(java.io.InputStream is, String encoding) {
	    java.util.Scanner s = new java.util.Scanner(is, encoding).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	private static void addChidrenWithPayload(Document document, Element root, ConfigurationNode element) throws ParserConfigurationException, SAXException, IOException {
		Element j = document.createElement(element.getElementName());
		j.setAttribute("name", element.getName());
		root.appendChild(j);
		for (Object i : element.getChildren()) {
			addChidrenWithPayload(document, j, (ConfigurationNode) i);
		}
		if (element.getContents().available() > 0) {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			InputSource is = new InputSource(element.getContents());
			is.setEncoding("cp1251");
			Document docPayload = docBuilder.parse(is);
			Element elementPayload = document.createElement("payload");
			elementPayload.setAttribute("name", "payload");
			Node firstDocImportedNode = document.importNode(docPayload.getFirstChild(), true);
			j.appendChild(firstDocImportedNode);
		}
		//System.out.println("YUPRT^: " + element.getName());
	}
	public static void reloadStructure(Server description) {
		structures.remove(description);
		getStructure(description);
	}
	public static IStructureComparator getStructure(Server description) {
		if (structures.containsKey(description)) return structures.get(description);
		ConfigurationNode root = description.root;
		try {
			if (false) root = new ConfigurationNode(description, DifferencerFactory.getDifferencerNode(diffo, description.getSID(), description.getURL(), description.getUSER(), description.getPASSWORD()));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		structures.put(description, root);
		description.fireEvent();
		return root;
	}
	public static CloseableHttpClient login(Server description) {
		boolean showCookies = false, getRep = true, getDir = true, useMTread = true;
		BasicCookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient;
		PoolingHttpClientConnectionManager cm;
		ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
		    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
		        // Honor 'keep-alive' header
		        HeaderElementIterator it = new BasicHeaderElementIterator(
		                response.headerIterator(HTTP.CONN_KEEP_ALIVE));
		        while (it.hasNext()) {
		            HeaderElement he = it.nextElement();
		            String param = he.getName();
		            String value = he.getValue();
		            if (value != null && param.equalsIgnoreCase("timeout")) {
		                try {
		                    return Long.parseLong(value) * 1000;
		                } catch(NumberFormatException ignore) {
		                }
		            }
		        }
		        HttpHost target = (HttpHost) context.getAttribute(
		                HttpClientContext.HTTP_TARGET_HOST);
		        if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
		            // Keep alive for 5 seconds only
		            return 5 * 1000;
		        } else {
		            // otherwise keep alive for 30 seconds
		            return 30 * 1000;
		        }
		    }

		};
		CloseableHttpClient client = HttpClients.custom().setKeepAliveStrategy(myStrategy).build();
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(description.getUSER(), description.getPASSWORD()));

        if (!useMTread) {
			httpclient = HttpClients.custom()
				.setKeepAliveStrategy(myStrategy)
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(credsProvider)
				.build();
		} else {
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(30);
			cm.setDefaultMaxPerRoute(30);
			httpclient = HttpClients.custom()
				.setKeepAliveStrategy(myStrategy)
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(credsProvider)
				.setConnectionManager(cm)
				.build();
		}
 		String salt = null;
			try {
			HttpGet httpget = new HttpGet(description.getURL() + s731uaa);
			httpget.setHeader("User-Agent", sUA);
			CloseableHttpResponse response1;
				response1 = httpclient.execute(httpget);
			try {
				HttpEntity entity = response1.getEntity();
				//System.out.println("Login form get: " + response1.getStatusLine());
			    java.util.Scanner scaner = new java.util.Scanner(entity.getContent(), "UTF-8").useDelimiter("\\A");
			    String s = scaner.hasNext() ? scaner.next() : "";
				int i = s.indexOf(saltB) + saltB.length(), j = s.indexOf(saltE, i);
				salt = s.substring(i, j);
				/*
 					System.out.println(s);
				System.out.println("salt:" + salt + "");
				if (showCookies) {
					System.out.println("Initial set of cookies:");
					List<Cookie> cookies = cookieStore.getCookies();
					if (cookies.isEmpty()) {
						System.out.println("None");
					} else {
						for (i = 0; i < cookies.size(); i++) {
							System.out.println("- " + cookies.get(i).toString());
						}
					}
				}
				 */
			} finally {
				response1.close();
			}
			HttpPost httpost = new HttpPost(description.getURL() + s731ujsc);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("j_username", description.getUSER()));
			nvps.add(new BasicNameValuePair("j_password", description.getPASSWORD()));
			nvps.add(new BasicNameValuePair("j_salt", salt));
			httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
			CloseableHttpResponse response2 = httpclient.execute(httpost);
			boolean ok = false;
			String errorAuth = "(reason of error is undetected)";
			try {
				HttpEntity entity = response2.getEntity();
				//System.out.println("Post-login form get: " + response2.getStatusLine().getStatusCode() + "_" + response2.getStatusLine());
				ok = response2.getStatusLine().getStatusCode() == 302;
			    java.util.Scanner scaner = new java.util.Scanner(entity.getContent(), "UTF-8").useDelimiter("\\A");
			    String s = scaner.hasNext() ? scaner.next() : "";
				//String s = new String(readStream(entity.getContent(), true), "UTF-8");
				//new PrintStream(new File("login_rezult.html")).print(s);
				if (showCookies) {
					List<Cookie> cookies = cookieStore.getCookies();
					if (cookies.isEmpty()) {
						System.out.println("None");
					} else {
						for (int i = 0; i < cookies.size(); i++) {
							System.out.println("- " + cookies.get(i).toString());
						}
					}
				}
				if (!ok) {
					int i = s.indexOf(authAnswerB);
					if (i != -1)
						i += authAnswerB.length();
					int j = s.indexOf(authAnswerE, i);
					//System.out.println(s);
					errorAuth = s.substring(i, j + authAnswerE.length()).trim();
					return null;
				}
			} finally {
				response2.close();
			}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return httpclient;
	}
	public static HashMap<String, String> getTypeList(Server description, CloseableHttpClient client, String u) {
		HashMap<String, String> typeList = new HashMap<String, String>();
		HttpGet httpget = new HttpGet(description.getURL() + u);
		CloseableHttpResponse response1 = null;
		try {
			response1 = client.execute(httpget);
			HttpEntity entity = response1.getEntity();
		    java.util.Scanner scaner;
			scaner = new java.util.Scanner(entity.getContent(), "UTF-8").useDelimiter("\\A");
		    String s = scaner.hasNext() ? scaner.next() : "";
			String s1 = "<select name=\"types\" size=10 multiple>";
			int i = s.indexOf(s1)+s1.length();
			int j = s.indexOf("</select>", i);
			String[] st = s.substring(i,j).split("<option value=\"");
			for (String q: st) if (q.length()>5) {
				String key = q.split("\"")[0].trim();
				String s2 = "\">";
				int ii = q.indexOf(s2)+s2.length();
				int jj = q.indexOf("</option>", ii);
				String name = q.substring(ii,jj);
				typeList.put(key, name);
			}
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				response1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return typeList;
	}
	public static List<String> getRepository(Server description, CloseableHttpClient client) {
		HttpGet httpget = new HttpGet(description.getURL() + s731repsup);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		List<String> repent = null;
		// httpget.setHeader("User-Agent", sUA);
		CloseableHttpResponse response1 = null;
		try {
			response1 = client.execute(httpget);
			HttpEntity entity = response1.getEntity();
		    java.util.Scanner scaner;
			scaner = new java.util.Scanner(entity.getContent(), "UTF-8").useDelimiter("\\A");
		    String s = scaner.hasNext() ? scaner.next() : "";
			//System.out.println(s);
			// Attempt to get all entities for REP and DIR
			if (true) {
				//new File("rep").mkdir();
				String s1 = "<select name=\"types\" size=10 multiple>";
				int i = s.indexOf(s1)+s1.length();
				int j = s.indexOf("</select>", i);
				String[] st = s.substring(i,j).split("<option value=\"");
				repent = new ArrayList<String>(st.length);
				for (String q: st) if (q.length()>5) repent.add(q.split("\"")[0].trim());
				//for (String q:repent) System.out.println("="+q+"=");
			}
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				response1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return repent;
	}
	public static List<String> getDirectory(Server description, CloseableHttpClient client) {
		HttpGet httpget = new HttpGet(description.getURL() + s731dirsup);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		List<String> dirent = null;
		// httpget.setHeader("User-Agent", sUA);
		CloseableHttpResponse response1 = null;
		try {
			response1 = client.execute(httpget);
			HttpEntity entity = response1.getEntity();
		    java.util.Scanner scaner;
			scaner = new java.util.Scanner(entity.getContent(), "UTF-8").useDelimiter("\\A");
		    String s = scaner.hasNext() ? scaner.next() : "";
				String s1 = "<select name=\"types\" size=10 multiple>";
				int i = s.indexOf(s1)+s1.length();
				int j = s.indexOf("</select>", i);
				String[] st = s.substring(i,j).split("<option value=\"");
				dirent = new ArrayList<String>(st.length);
				for (String q: st) if (q.length()>5) dirent.add(q.split("\"")[0].trim());
				//for (String q:repent) System.out.println("="+q+"=");
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				response1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dirent;
	}
	static String sUA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.48 Safari/537.36";
	static String s731uaa = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/UmeAdminApp";
	static String s731ujsc = "/webdynpro/dispatcher/sap.com/tc~sec~ume~wd~umeadmin/j_security_check";
	static String saltB = "<input type=\"hidden\" name=\"j_salt\" value=\"", saltE = "\"";
	static String authAnswerB = "<!--	Federation Error Message				-->", authAnswerE = "</div>";
	static String s731repsup = "/rep/support/SimpleQuery";
	static String s731dirsup = "/dir/support/SimpleQuery";	
}
