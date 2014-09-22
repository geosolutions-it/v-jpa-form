package it.jrc.form.editor;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

/**
 * Designed to support the actions of an editor with a selection and edit panel
 * 
 * 1. Have a selection panel
 * 
 * 2. Have a editor panel
 * 
 * 3. Know what to do when the edit state changes
 * 
 */
public interface SelectAndEditUI extends Component, EditFormUI {

    void setTable(Component table);
    
    void addSelectionComponent(Component selectionComponent);
    
    public Button getCreateButton();
}
