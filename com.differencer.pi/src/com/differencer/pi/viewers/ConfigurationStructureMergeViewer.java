package com.differencer.pi.viewers;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.compare.structuremergeviewer.StructureDiffViewer;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.differencer.pi.Activator;
import com.differencer.pi.actions.ExportToClientTransportAction;
import com.differencer.pi.actions.ExportToServerTransportAction;
import com.differencer.pi.editors.Server;
import com.differencer.pi.nodes.ConfigurationNode;
import com.differencer.pi.preferences.PreferenceConstants;
public class ConfigurationStructureMergeViewer extends StructureDiffViewer {
	public ConfigurationStructureMergeViewer(Composite parent, CompareConfiguration configuration) {
		super(parent, configuration);
	}
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		boolean enable = false;
		ISelection selection = getSelection();
		HashMap<ConfigurationNode, ConfigurationNode> nodes = new HashMap<ConfigurationNode, ConfigurationNode>();
		if (selection instanceof IStructuredSelection) {
			Iterator<IStructuredSelection> elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object element = elements.next();
				if (element instanceof DiffNode) {
					DiffNode container = (DiffNode) element;
					ITypedElement typed = container.getLeft();
					if (typed instanceof ConfigurationNode) {
						enable = true;
						ConfigurationNode leaf = (ConfigurationNode) typed;
						if (leaf.getChildren().length != 0) {
							enable = true;
							Object[] children = leaf.getChildren();
							for (int i = 0; i < children.length;i++){
								ConfigurationNode child = (ConfigurationNode) children[i];
								nodes.put(child, child);
							}
						}
						nodes.put(leaf, leaf);
					}
				}
			}
		}
		IPreferencesService service = Platform.getPreferencesService();
		String transportDirectory = service.getString(Activator.PLUGIN_ID, PreferenceConstants.P_TRANSPORT_PATH, "not found transport directory preference!", null);
		String transportArchiveDirectory = service.getString(Activator.PLUGIN_ID, PreferenceConstants.P_TRANSPORT_ARCHIVE_PATH, "not found transport archive directory preference!", null);
		ExportToClientTransportAction exportToClientTransportAction = new ExportToClientTransportAction(nodes, transportDirectory, transportArchiveDirectory);
		ActionContributionItem actionToClientContributionItem = new ActionContributionItem(exportToClientTransportAction);
		actionToClientContributionItem.setVisible(true);
		exportToClientTransportAction.setEnabled(enable);
		Server left = null;
		if (getRoot() instanceof DiffNode) {
			DiffNode container = (DiffNode) getRoot();
			ITypedElement right = container.getRight();
			if (right instanceof ConfigurationNode) {
				left = ((ConfigurationNode) right).getServer();
			}
		}
		ExportToServerTransportAction exportToServerTransportAction = new ExportToServerTransportAction(nodes, transportDirectory, transportArchiveDirectory, left);
		ActionContributionItem actionToServerContributionItem = new ActionContributionItem(exportToServerTransportAction);
		actionToServerContributionItem.setVisible(true);
		exportToServerTransportAction.setEnabled(enable);
		manager.add(actionToClientContributionItem);
		manager.add(actionToServerContributionItem);
		super.fillContextMenu(manager);
	}
	@Override
	protected void createToolItems(ToolBarManager tbm) {
		Action a = new Action() {
			public void run() {
				exportDifference();
			}
		};
		a.setText("Export");
		a.setToolTipText("Export difference");
		a.setDescription("Export difference to file");
//		ImageDescriptor id = Activator.getImageDescriptor("icons/sample.gif");
//		if (id != null) a.setDisabledImageDescriptor(id);
//		id = Activator.getImageDescriptor("icons/sample.gif");
//		if (id != null) {
//			a.setImageDescriptor(id);
//			a.setHoverImageDescriptor(id);
//		}
		ActionContributionItem exportAction = new ActionContributionItem(a);
		exportAction.setVisible(true);
		tbm.add(exportAction);
		super.createToolItems(tbm);
	}
	private void exportDifference() {
		FileDialog fd = new FileDialog(getControl().getShell(), SWT.SAVE);
		fd.setText("Save");
		fd.setFilterPath("C:/tmp");
		String[] filterExt = { "*.txt", "*.xml", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if (selected != null) exportTree(selected);
	}
	private void exportTree(String path) {
		ConfigurationNode confnodeLeft = null;
		ConfigurationNode confnodeRight = null;
		DiffNode rootnode = (DiffNode) getRoot();
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();
			if (rootnode.getLeft() != null) confnodeLeft = (ConfigurationNode) rootnode.getLeft();
			if (confnodeLeft == null) return;
			Element root = document.createElement(confnodeLeft.getElementName());
			root.setAttribute("name", confnodeLeft.getName());
			root.setAttribute("side", "left");
			document.appendChild(root);
			addElements(document, root, rootnode);
			if (rootnode.getRight() != null) confnodeRight = (ConfigurationNode) rootnode.getRight();
			if (confnodeRight == null) return;
			root = document.createElement(confnodeRight.getElementName());
			root.setAttribute("name", confnodeRight.getName());
			root.setAttribute("side", "right");
			document.appendChild(root);
			addElements(document, root, rootnode);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	public static Document addElements(Document document, Element element, DiffNode diffnode) {
		IDiffElement[] nodes = diffnode.getChildren();
		for (int i = 0; i < nodes.length; i++) {
			DiffNode subdiffnode = (DiffNode) nodes[i];
			ConfigurationNode subconfnodeLeft = null;
			ConfigurationNode subconfnodeRight = null;
			if (diffnode.getLeft() != null) subconfnodeLeft = (ConfigurationNode) subdiffnode.getLeft();
			if (subconfnodeLeft == null) continue;
			Element subelement = document.createElement(subconfnodeLeft.getElementName());
			subelement.setAttribute("name", subconfnodeLeft.getName());
			subelement.setAttribute("side", "left");
			element.appendChild(subelement);
			addElements(document, subelement, subdiffnode);
			if (diffnode.getRight() != null) subconfnodeRight = (ConfigurationNode) subdiffnode.getRight();
			if (subconfnodeRight == null) continue;
			subelement = document.createElement(subconfnodeRight.getElementName());
			subelement.setAttribute("name", subconfnodeRight.getName());
			subelement.setAttribute("side", "right");
			element.appendChild(subelement);
			addElements(document, subelement, subdiffnode);
		}
		return document;
	}
}
