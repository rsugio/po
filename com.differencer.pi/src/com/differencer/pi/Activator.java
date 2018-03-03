package com.differencer.pi;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.LogManager;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import com.differencer.pi.preferences.PreferenceConstants;
/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.differencer.pi";
	// The shared instance
	private static Activator plugin;
	/**
	 * The constructor
	 */
	public Activator() {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		InputStream logging = getClass().getResourceAsStream("/logging.properties");
		if (logging != null) LogManager.getLogManager().readConfiguration(logging);
		Differencer.openDatabase();
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(PreferenceConstants.P_DATABASE_PATH) || event.getProperty().equals(PreferenceConstants.P_DATABASE_FILE)) {
					String value = event.getNewValue().toString();
					IJobManager manager = Job.getJobManager();
					while(!manager.isIdle())
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Activator.log(Status.ERROR, "Property change waiting interrupted", e);
						}
					Differencer.closeDatabase();
					Differencer.openDatabase();
				}
			}
		});
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		Differencer.closeDatabase();
	}
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	public static void log(int s, String m) {
		if (plugin != null) getDefault().getLog().log(new Status(s, plugin.getBundle().getSymbolicName(), Status.OK, m, null));
	}
	public static void log(int s, String m, Throwable e) {
		if (plugin != null) getDefault().getLog().log(new Status(s, plugin.getBundle().getSymbolicName(), Status.OK, m, e));
	}
	public static ImageDescriptor getImageDescriptor(String p) {
		if (plugin == null)
			return null;
		IPath path= new Path(p);	
		URL url= FileLocator.find(plugin.getBundle(), path, null);
		if (url == null)
			return null;
		return ImageDescriptor.createFromURL(url);
	}
}
