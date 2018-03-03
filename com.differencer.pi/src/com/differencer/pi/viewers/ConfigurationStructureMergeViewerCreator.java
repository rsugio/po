package com.differencer.pi.viewers;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class ConfigurationStructureMergeViewerCreator implements IViewerCreator {
	public ConfigurationStructureMergeViewerCreator() {
	}
	@Override
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		ConfigurationStructureMergeViewer structureDiffViewer = new ConfigurationStructureMergeViewer(parent, config);
		structureDiffViewer.setStructureCreator(new ConfigurationStructureCreator());
		return structureDiffViewer;
	}
}
