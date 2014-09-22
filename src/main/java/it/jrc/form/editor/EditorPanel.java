package it.jrc.form.editor;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class EditorPanel extends CssLayout {

    private EditorPanelHeading editorPanelHeading;
    private Button closeButton;

    /**
     * Width setting function overridden with a no-op to prevent it always being set to 100%
     * 
     * @author Will Temperley
     *
     */

    public EditorPanel() {

        addStyleName("layout-panel");
        setSizeFull();
        
        /*
         * Close button is invisible until it is retrieved. 
         * Then it's assumed it's wanted so becomes visible.
         */
        closeButton = new Button();
        closeButton.addStyleName("configure");
        closeButton.addStyleName("icon-cancel");
        closeButton.addStyleName("icon-only");
        closeButton.addStyleName("borderless");
        closeButton.setDescription("Close");
        closeButton.addStyleName("small");
        closeButton.setVisible(false);
        addComponent(closeButton);

    }

    public EditorPanelHeading addHeading(String title) {
        
        editorPanelHeading = new EditorPanelHeading(title);
        this.addComponent(editorPanelHeading);
        return editorPanelHeading;
        
    }
    
    public Button getCloseButton() {
        closeButton.setVisible(true);
        return closeButton;
    }

    public void addCreateButton(Button button) {
        editorPanelHeading.addComponent(button);
    }

    public void setContent(Component content) {
        addComponent(content);
    }

    public void addDescription(String description) {
        Label label = new Label(description);
        label.addStyleName("panel-description");
        this.addComponent(label);
    }

}
