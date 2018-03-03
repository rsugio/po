package com.differencer.pi.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import com.differencer.pi.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_DATABASE_PATH, ".");
		store.setDefault(PreferenceConstants.P_DATABASE_FILE, "diffo.db");
		store.setDefault(PreferenceConstants.P_TRANSPORT_PATH, ".");
		store.setDefault(PreferenceConstants.P_TRANSPORT_ARCHIVE_BOOLEAN, false);
		store.setDefault(PreferenceConstants.P_TRANSPORT_ARCHIVE_PATH, "." + System.getProperty("file.separator") + "archive");
		//store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
	}

}
