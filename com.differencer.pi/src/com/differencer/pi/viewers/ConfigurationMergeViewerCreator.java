package com.differencer.pi.viewers;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

/**
 * A factory object for the <code>TextMergeViewer</code>.
 * This indirection is necessary because only objects with a default
 * constructor can be created via an extension point
 * (this precludes Viewers).
 */
public class ConfigurationMergeViewerCreator implements IViewerCreator {

	public Viewer createViewer(Composite parent, CompareConfiguration mp) {
		mp.setRightEditable(false);
		mp.setLeftEditable(false);
		return new ConfigurationMergeViewer(parent, mp);
	}
}
