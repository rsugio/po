package com.differencer.pi.editors;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
public class ServerStorageEditorInput implements IStorageEditorInput {
	private IStorage storage;
	public ServerStorageEditorInput(IStorage s) {
		storage = s;
	}
	@Override
	public boolean exists() {
		return true;
	}
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}
	@Override
	public String getName() {
		return storage.getName();
	}
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}
	@Override
	public String getToolTipText() {
		return "Read-Only " + storage.toString();
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
	public IStorage getStorage() {
		return storage;
	}
}
