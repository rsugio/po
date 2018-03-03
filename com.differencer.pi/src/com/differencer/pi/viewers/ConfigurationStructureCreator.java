package com.differencer.pi.viewers;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.xml.sax.SAXException;
import com.differencer.pi.Activator;
import com.differencer.pi.editors.Server;
import com.differencer.pi.nodes.ConfigurationNode;
public class ConfigurationStructureCreator implements IStructureCreator {
	public ConfigurationStructureCreator() {
		// nothing to do
	}
	/*
	 * This title will be shown in the title bar of the structure compare pane.
	 */
	public String getName() {
		return "PI Configuration difference";
	}
	/*
	 * Returns a node.
	 */
	public IStructureComparator getStructure(Object input) {
		if (!(input instanceof IStreamContentAccessor)) return null;
		IStreamContentAccessor sca = (IStreamContentAccessor) input;
		Server description = null;
		try {
			description = new Server(sca.getContents());
		} catch (ParserConfigurationException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (SAXException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (IOException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (CoreException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		return com.differencer.pi.Differencer.getStructure(description);
	}
	public void save(IStructureComparator structure, Object input) {
		Assert.isTrue(false); // Cannot update PI
	}
	public String getContents(Object node, boolean ignoreWhitespace) {
		if (node instanceof ConfigurationNode) return ((ConfigurationNode) node).getVersion();
		return "";
	}
	public IStructureComparator locate(Object path, Object source) {
		return null;
	}
	public boolean canRewriteTree() {
		return false;
	}
}
