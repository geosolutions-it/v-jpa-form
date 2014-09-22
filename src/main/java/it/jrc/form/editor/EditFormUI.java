package it.jrc.form.editor;

import com.vaadin.ui.Component;

/**
 * Designed to support the selection actions of an editor
 * 
 * 1. Have a selection panel
 * 
 * 2. Have a editor panel
 * 
 * 3. Know what to do when the edit state changes
 * 
 */
public interface EditFormUI extends Component {

    void addFormComponent(Component form, String label, String description);

    void setEditingState(boolean isEditing);

    void setSubmitPanel(Component submitPanel);
    
}
