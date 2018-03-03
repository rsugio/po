package com.differencer.pi.actions;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.xml.sax.SAXException;
import com.differencer.pi.Activator;
import com.differencer.pi.editors.Server;
import com.differencer.pi.editors.ServerConfigurationJob;
import com.differencer.pi.editors.ServerConfigurationJobs;
public class ConfigurationCollectAction implements IObjectActionDelegate {
	private Vector<Server> descriptions = new Vector<Server>();
	public ConfigurationCollectAction() {
	}
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	@Override
	public void run(IAction action) {
		Iterator<Server> iterator = descriptions.iterator();
		while (iterator.hasNext()) {
			startCollectionJob(iterator.next());
		}
	}
	public static void startCollectionJob(Server description) {
		IProgressMonitor group = Job.getJobManager().createProgressGroup();
		Job job = new ServerConfigurationJobs("Collect PI configuration from host " + description.getURL(), description, group);
		job.setProgressGroup(group, 100);
		job.schedule();
	}
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		descriptions.clear();
		Iterator<IFile> iterator = ((StructuredSelection) selection).iterator();
		while (iterator.hasNext()) {
			try {
				descriptions.add(new Server(iterator.next()));
			} catch (ParserConfigurationException e) {
				Activator.log(Status.ERROR, getClass().getName(), e);
			} catch (SAXException e) {
				Activator.log(Status.ERROR, getClass().getName(), e);
			} catch (IOException e) {
				Activator.log(Status.ERROR, getClass().getName(), e);
			} catch (CoreException e) {
				Activator.log(Status.ERROR, getClass().getName(), e);
			}// no PI files are filtered out by Eclipse :)
		}
	}
}
