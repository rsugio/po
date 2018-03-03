package com.differencer.pi.editors;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.differencer.pi.Activator;
import com.differencer.pi.nodes.ConfigurationNode;
public class Server {
	private String SID;
	private String URL;
	private String USER;
	public ConfigurationNode root = null;
	private HashMap<ServerListener, ServerListener> listeners = new HashMap<ServerListener, ServerListener>();
	public Server(IFile f) throws ParserConfigurationException, SAXException, IOException, CoreException {
		setXML(f);
	}
	public Server(InputStream s) throws ParserConfigurationException, SAXException, IOException, CoreException {
		setXML(s);
	}
	public void setXML(InputStream s) throws ParserConfigurationException, SAXException, IOException, CoreException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(s);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("pisite");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				setSID(getTagValue("sid", eElement));
				setURL(getTagValue("url", eElement));
				setUSER(getTagValue("user", eElement));
				//setPASSWORD(getTagValue("password", eElement));
			}
		}
	}
	public String getXML() {
		return "<pisite>" + "\n" + "\t<sid>" + getSID() + "</sid>" + "\n" + "\t<url>" + getURL() + "</url>" + "\n" + "\t<user>" + getUSER() + "</user>" + "\n" + "\t<password>" + getPASSWORD().replaceAll(".", "*") + "</password>" + "\n" + "</pisite>";
	}
	public void setXML(String s) throws ParserConfigurationException, SAXException, IOException, CoreException {
		setXML(new ByteArrayInputStream(s.getBytes()));
	}
	public void setXML(IFile f) throws ParserConfigurationException, SAXException, IOException, CoreException {
		setXML(f.getContents());
	}
	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return (nValue == null) ? "" : nValue.getNodeValue();
	}
	public void setSID(String sID) {
		SID = sID;
	}
	public String getSID() {
		return SID;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	public String getURL() {
		return URL;
	}
	public void setUSER(String uSER) {
		USER = uSER;
	}
	public String getUSER() {
		return USER;
	}
	public void setPASSWORD(String pASSWORD) {
		ISecurePreferences root = SecurePreferencesFactory.getDefault();
		ISecurePreferences node = root.node(getSecureNode());
		try {
			node.put("password", pASSWORD, true /*encrypt*/);
		} catch (StorageException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
	}
	public String getPASSWORD() {
		if (getSID() == null) return ""; 
		if (getSID().equals("")) return ""; 
		ISecurePreferences root = SecurePreferencesFactory.getDefault();
		ISecurePreferences node = root.node(getSecureNode());
		String PASSWORD = null;
		try {
			PASSWORD = node.get("password", null /*default*/);
		} catch (StorageException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		return "" + PASSWORD;
	}
	public String toString() {
		return "Server PI " + SID + " " + URL;
	}
	private String getSecureNode() {
		return "/com/differencer/pi/"+ getSID() + "/" + getUSER();
	}
	@Override
	public boolean equals(Object other) {
		if (other instanceof Server) return 
		(getSID().equals(((Server) other).getSID())) &&
		(getURL().equals(((Server) other).getURL())) &&
		(getUSER().equals(((Server) other).getUSER())) &&
		(getPASSWORD().equals(((Server) other).getPASSWORD()));
		return super.equals(other);
	}
	public int hashCode() {
		return (getSID()+getURL()+getUSER()+getPASSWORD()).hashCode();
	}
	public void addListener(ServerListener l) {
		listeners.put(l,l);
	}
	public void removeListener(ServerListener l) {
		listeners.remove(l);
	}
	public void fireEvent() {
		for (ServerListener l: listeners.values()) l.serverChanged(this);
	}
}
