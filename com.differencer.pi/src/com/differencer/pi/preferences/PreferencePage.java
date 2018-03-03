package com.differencer.pi.preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import com.differencer.pi.Activator;
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("PI difference machine preference");
	}
	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.P_DATABASE_PATH, "Difference &database directory:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_DATABASE_FILE, "Difference &database filename:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.P_TRANSPORT_PATH, "&Transport directory:", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_TRANSPORT_ARCHIVE_BOOLEAN, "&Archive transport", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.P_TRANSPORT_ARCHIVE_PATH, "A&rchive transport directory:", getFieldEditorParent()));
		// addField(new RadioGroupFieldEditor(
		// PreferenceConstants.P_CHOICE,
		// "An example of a multiple-choice preference",
		// 1,
		// new String[][] { { "&Choice 1", "choice1" }, {
		// "C&hoice 2", "choice2" }
		// }, getFieldEditorParent()));
	}
	public void init(IWorkbench workbench) {
	}
}