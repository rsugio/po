package com.differencer.pi.editors;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.eclipse.core.runtime.IProgressMonitor;

import com.differencer.pi.nodes.ConfigurationNode;
import com.differencer.pi.nodes.DiffItemEx;
import com.differencer.pi.nodes.DifferencerNodeEx;

class HTMLTableParser extends HTMLEditorKit.ParserCallback {
    private ConfigurationNode parent;
    private boolean encounteredATableRow = false;
    private boolean encounteredAHeaderTableRow = false;
    private int column = 0;
    private int headercolumn = 0;
    private Map<Integer, String> header = new HashMap<Integer, String>();
    private Map<String, String> item = null;
    private String name = null;
    private String value = null;
    private String url = null;
	private Server description;
	private boolean deleted = false;
	private IProgressMonitor monitor;

    public HTMLTableParser(Server d, ConfigurationNode c, boolean e, IProgressMonitor m) {
    	description = d;
    	parent = c;
    	deleted  = e;
    	monitor = m;
    }
    
    public void handleText(char[] data, int pos) {
        if(encounteredAHeaderTableRow) {
        	header.put(new Integer(headercolumn), new String(data));
        }
        if(encounteredATableRow) {
        	if (header.get(new Integer(column)) == null) return;
        	if (header.get(new Integer(column)).equals("Raw"))
                item.put(header.get(new Integer(column)), url);
        	else 
        		item.put(header.get(new Integer(column)), new String(data));
        }
    }

    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if(t == HTML.Tag.TH) {
        	encounteredAHeaderTableRow = true;
        	headercolumn++;
        	item = null;
        }
        if(t == HTML.Tag.TR) {
        	encounteredATableRow = true;
        	item = new HashMap<String, String>();
        }
        if(t == HTML.Tag.A) url = (String)a.getAttribute(HTML.Attribute.HREF);
        if(t == HTML.Tag.TD) column++;
    }

    public void handleEndTag(HTML.Tag t, int pos) {
        if(t == HTML.Tag.TH) {
        	encounteredAHeaderTableRow = false;
        }
        if(t == HTML.Tag.TR) {
        	monitor.worked(1);
           	if (item != null) {
           		String SWCV = item.get("Software Component Version");
           		String KEY = item.get("Key");
           		String OBJECTID = item.get("OBJECTID");
           		String VERSIONID = item.get("VERSIONID");
           		String FOLDERREF = item.get("FOLDERREF");
           		String TEXT = item.get("Description");
           		String RA_XILINK = item.get("Raw");
           		String NAME = item.get("Name");
           		if (NAME == null) NAME = KEY;
           		if (NAME == null) NAME = "NAME";
           		String NAMESPACE = item.get("Namespace");
           		if (NAMESPACE == null) NAMESPACE = KEY;
           		if (NAMESPACE == null) NAMESPACE = "NAMESPACE";
           		String OWNER = item.get("Person Responsible");
           		if (!NAMESPACE.contains("http://sap.com/xi/") 
//           				&& NAME.equals("SalesOrderStatusHistory")
           				) {//TODO nonSAPerst
           			DifferencerNodeEx inst = new DifferencerNodeEx(new DiffItemEx(description, KEY, (SWCV != null) ? NAMESPACE+"/"+NAME : NAME, OBJECTID.getBytes(), VERSIONID.getBytes(), 0, deleted, RA_XILINK));
           			parent.node.addChild(inst);
           			ConfigurationNode inst_conf = new ConfigurationNode(description, inst); 
           			parent.addChild(inst_conf);
           		}
           	}
            encounteredATableRow = false;
        	item = null;
        	column = 0;
        	headercolumn = 0;
        }
        if(t == HTML.Tag.A) url = null;
    }
}