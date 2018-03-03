package com.differencer.pi.nodes;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.SAXException;

import com.pinternals.diffo.DiffItem;
import com.pinternals.diffo.UUtil;
import com.pinternals.diffo.api.IDifferencerNode;
import com.pinternals.diffo.api.IDiffo;

public class DifferencerNodeEx implements IDifferencerNode {
	public DiffItemEx item = null;
	String name = null;
	private HashMap<IDifferencerNode, IDifferencerNode> children = new HashMap<IDifferencerNode, IDifferencerNode>(10);
	HashMap<String,String> hmTN = null;
	private Object parent;
	public DifferencerNodeEx(String s) {
		name = s;
	}
	public DifferencerNodeEx(DiffItemEx i) {
		item = i;
		name = item.name;
	}
	public String getName() {
		return item == null ? name : item.name;
	}
	public String getVersion() {
		return item == null ? name : new String(item.version_id);
	}
	public InputStream getPayload() {
		try {
			return item == null ? null : item.getPayload();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public boolean equals(Object other) {
		if (other instanceof DifferencerNodeEx) return (getName().equals(((IDifferencerNode) other).getName()));
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
	public Object[] getChildren() {
		Object[] c = new Object[children.size()];
		Iterator<IDifferencerNode> iter = children.values().iterator();
		for (int i = 0; iter.hasNext(); i++)
			c[i] = iter.next();
		return c;
	}
	public void addChild(IDifferencerNode c) {
		children.put(c, c);
		c.setParent(this);
	}
	// --------------------------
	
	public String getObjectSWCV() {
		return item.getTransportAttr("getObjectSWCV");
	}
	public String getObjectID() {
		return item.getTransportAttr("getObjectID");
	}
	public String getObjectName() {
		return item.getTransportAttr("getObjectName");
	}
	public String getObjectType() {
		// TODO Auto-generated method stub
		return item.getTransportAttr("getObjectType");
	}
	public String getObjectNamespace() {
		return item.getTransportAttr("getObjectNamespace");
	}
	@Override
	public String getElementName() {
		// TODO Auto-generated method stub
		return "foo";
	}
	@Override
	public String getTransportSuffix() {
		return item.getTransportAttr("getTransportSuffix");
	}
	@Override
	public boolean refresh(IDiffo idiffo, boolean withChildren)
	throws MalformedURLException, SQLException, IOException, SAXException, ParseException, InterruptedException {
		return true;
		// TODO Auto-generated method stub
//		if (withChildren)
//			return idiffo.refresh(item.p.sid, item.p.uroot.toExternalForm(), item.p.uname, item.p.passwd);
//		else {
//			// TODO: incremental farsch
//			// если item.oref неизвестный, то надо сделать частичный фарш
//			assert item.oref != 0 && item.oref != -1L : "object reference is not exist yet.";
//			return true;
//		}
	}
}
