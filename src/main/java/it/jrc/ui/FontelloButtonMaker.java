package it.jrc.ui;

import com.vaadin.ui.Button;

public class FontelloButtonMaker {
    
    
    
    public enum ButtonIcon {
        icon_cog, icon_close, icon_search, icon_up
    }

    public static Button getButton(ButtonIcon icon) {
        /*
         * Close button is invisible until it is retrieved. 
         * Then it's assumed it's wanted so becomes visible.
         */
        Button closeButton = new Button();
        
        closeButton.addStyleName(icon.toString().replace('_', '-'));
        
        closeButton.addStyleName("icon-only");
//        closeButton.setVisible(true);
        
        return closeButton;
    }
    

}
