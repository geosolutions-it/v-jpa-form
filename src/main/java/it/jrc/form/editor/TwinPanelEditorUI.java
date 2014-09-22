package it.jrc.form.editor;


import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class TwinPanelEditorUI extends HorizontalLayout implements
        SelectAndEditUI {


    EditorPanel leftPanel = new EditorPanel();
    EditorPanel rightPanel = new EditorPanel();
    
    VerticalLayout placeHolder = new VerticalLayout();
    
    private Button createButton;

    public TwinPanelEditorUI(String title) {

        this.setSizeFull();
        this.setSpacing(true);

        //Just to put something in the RHS as a placeholder
        addComponent(placeHolder);
        addComponent(leftPanel);
        leftPanel.addHeading(title);
        createButton = new Button("New");
        leftPanel.addCreateButton(createButton);
        
        /*
         * edit panel
         */
        rightPanel.getCloseButton().addClickListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
               setEditingState(false); 
            }
        });
        rightPanel.addHeading("Edit " + title);

        addComponent(placeHolder);
        placeHolder.addComponent(rightPanel);

        /*
         * Begin not in edit mode
         */
        setEditingState(false);
    }

    public void addFormComponent(Component form, String label, String description) {
        rightPanel.addComponent(form);
    }
    
    public void addSelectionComponent(Component filterPanel) {
        leftPanel.addComponent(filterPanel);
    }

    public void setTable(Component table) {
        leftPanel.addComponent(table);
    }

    public void setSubmitPanel(Component submitPanel) {
        rightPanel.addComponent(submitPanel);
    }

    public void setEditingState(boolean isEditing) {
        rightPanel.setVisible(isEditing);
        if (createButton != null) {
            createButton.setEnabled(!isEditing);
        }
    }

    public Button getCreateButton() {
        return createButton;
    }
}
