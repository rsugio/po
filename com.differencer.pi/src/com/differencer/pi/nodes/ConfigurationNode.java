package com.differencer.pi.nodes;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
import com.differencer.pi.UserHandler;
import com.differencer.pi.editors.Server;
import com.pinternals.diffo.api.IDifferencerNode;
public class ConfigurationNode implements IStructureComparator, ITypedElement, IStreamContentAccessor, IStorage {
	public IDifferencerNode node;
	private Object parent;
	private HashMap<ConfigurationNode, ConfigurationNode> children = new HashMap<ConfigurationNode, ConfigurationNode>(10);
	private Server server;
	public ConfigurationNode(Server d, IDifferencerNode n) {
		node = n;
		server = d;
		Object[] nodes = n.getChildren();
		for (int i = 0; i < nodes.length; i++) {
			addChild(new ConfigurationNode(server, (IDifferencerNode) nodes[i]));
		}
	}
	public String getVersion() {
		return node.getVersion();
	}
	@Override
	public String getName() {
		return node.getName();
	}
	public String getType() {
		return "piconf";
	}
	@Override
	public Image getImage() {
		return CompareUI.getImage(getType());
	}
	@Override
	public InputStream getContents() {
		try {
			InputStream payload = node.getPayload();
			if (payload == null) return new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><EmptyPayload></EmptyPayload>".getBytes("UTF-8"));
			BufferedReader reader = new BufferedReader(new InputStreamReader(node.getPayload(), "UTF-8"));
			Source xmlInput = new StreamSource(reader);
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", 2);
			try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new ByteArrayInputStream(xmlOutput.getWriter().toString().getBytes());
} catch (UnsupportedEncodingException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		return null;
	}
	@Override
	public IPath getFullPath() {
		return null;
	}
	@Override
	public boolean isReadOnly() {
		return true;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	@Override
	public Object[] getChildren() {
		Object[] c = new Object[children.size()];
		Iterator<ConfigurationNode> iter = children.values().iterator();
		for (int i = 0; iter.hasNext(); i++)
			c[i] = iter.next();
		return c;
	}
	public void addChild(ConfigurationNode c) {
		children.put(c, c);
		c.setParent(this);
		// node.addChild(c.getNode());
	}
	public IDifferencerNode getNode() {
		return node;
	}
	@Override
	public boolean equals(Object other) {
		if (other instanceof ConfigurationNode) return (getName().equals(((ConfigurationNode) other).getName()));
		return super.equals(other);
	}
	@Override
	public int hashCode() {
		return getName().hashCode();
	}
	public void setParent(Object p) {
		parent = p;
	}
	public Object getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}
	public Server getServer() {
		return server;
	}
	public String getSWCVID() {
		return node.getObjectSWCV();
	}
	public String getOBJECTID() {
		return node.getObjectID();
	}
	public String getOBJECTNAME() {
		return node.getObjectName();
	}
	public String getOBJECTTYPE() {
		return node.getObjectType();
	}
	public String getOBJECTNAMESPACE() {
		return node.getObjectNamespace();
	}
	public String getElementName() {
		return node.getElementName();
	}
	public String getTransportSuffix() {
		return node.getTransportSuffix();
	}
}
