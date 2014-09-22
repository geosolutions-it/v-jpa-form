package it.jrc.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;

public class SimplePanel extends CssLayout {
    
    public SimplePanel() {
        addStyleName("display-panel");
        addStyleName("layout-panel");
    }
    
    public enum ButtonIcon {
        icon_cog, icon_close
    }

    public Button addActionButton(ButtonIcon icon) {
        /*
         * Close button is invisible until it is retrieved. 
         * Then it's assumed it's wanted so becomes visible.
         */
        Button closeButton = new Button();
        closeButton.addStyleName("configure");
        
        closeButton.addStyleName(icon.toString().replace('_', '-'));
        
        closeButton.addStyleName("icon-only");
        closeButton.addStyleName("borderless");
        closeButton.setDescription("Edit");
        closeButton.addStyleName("small");
//        closeButton.setVisible(true);
        addComponent(closeButton); 
        
        return closeButton;
    }
}
