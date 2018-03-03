package com.differencer.pi.viewers;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.swt.widgets.Composite;
public class ConfigurationMergeViewer extends TextMergeViewer {
	public ConfigurationMergeViewer(Composite parent, CompareConfiguration configuration) {
		super(parent, configuration);
	}
	public String getTitle() {
		return "PI Compare";
	}
	@Override
	public Object getInput() {
		return super.getInput();
	}
	@Override
	public void setInput(Object input) {
		super.setInput(input);
	}
}