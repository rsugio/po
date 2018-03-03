package com.differencer.pi.wizards;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.xml.sax.SAXException;
import com.differencer.pi.Activator;
import com.differencer.pi.Differencer;
import com.differencer.pi.editors.Server;
public class PayloadExporter {
	public void createFolder(IPath destinationPath) {
		new File(destinationPath.toOSString()).mkdir();
	}
	public void write(IResource resource, IPath destinationPath) throws CoreException, IOException {
		if (resource.getType() == IResource.FILE) {
			writeFile((IFile) resource, destinationPath);
		} else {
			writeChildren((IContainer) resource, destinationPath);
		}
	}
	protected void writeChildren(IContainer folder, IPath destinationPath) throws CoreException, IOException {
		if (folder.isAccessible()) {
			IResource[] children = folder.members();
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				writeResource(child, destinationPath.append(child.getName()));
			}
		}
	}
	protected void writeFile(IFile file, IPath destinationPath) throws IOException, CoreException {
		OutputStream output = null;
		InputStream contentStream = null;
		try {
			contentStream = new BufferedInputStream(file.getContents(false));
			output = new BufferedOutputStream(new FileOutputStream(destinationPath.toOSString() + ".piload.xml"));
			Server description = new Server(contentStream);
			Differencer.exportXMLWithPayload(description, output);
		} catch (ParserConfigurationException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (SAXException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (IOException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} catch (CoreException e) {
			Activator.log(Status.ERROR, getClass().getName(), e);
		} finally {
			if (contentStream != null) {
				try {
					contentStream.close();
				} catch (IOException e) {
					Activator.log(Status.ERROR, getClass().getName(), e);
				}
			}
			if (output != null) {
				output.close();
			}
		}
	}
	protected void writeResource(IResource resource, IPath destinationPath) throws CoreException, IOException {
		if (resource.getType() == IResource.FILE) {
			writeFile((IFile) resource, destinationPath);
		} else {
			createFolder(destinationPath);
			writeChildren((IContainer) resource, destinationPath);
		}
	}
}
