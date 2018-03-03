package com.differencer.pi.editors;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.differencer.pi.Activator;
public class ServerTreeContentProvider implements IContentProvider, ITreeContentProvider {
	@Override
	public void dispose() {
	}
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	public Object[] getChildren(Object parentElement) {
		return getChildrenByReflect(parentElement);
	}
	public Object getParent(Object element) {
		Method method = null;
		try {
			method = element.getClass().getMethod("getParent");
			return method.invoke(element);
		} catch (IllegalArgumentException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (IllegalAccessException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (InvocationTargetException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (SecurityException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (NoSuchMethodException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		return null;
	}
	public boolean hasChildren(Object element) {
		return getChildrenByReflect(element).length > 0;
	}
	public Object[] getElements(Object inputElement) {
		return getChildrenByReflect(inputElement);
	}
	private Object[] getChildrenByReflect(Object element) {
		Method method = null;
		try {
			method = element.getClass().getMethod("getChildren");
			return (Object[]) method.invoke(element);
		} catch (IllegalArgumentException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (IllegalAccessException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (InvocationTargetException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (SecurityException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (NoSuchMethodException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		}
		return null;
	}
}
