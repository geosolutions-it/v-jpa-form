package it.jrc.form.component;

import java.util.Calendar;

import com.vaadin.ui.ComboBox;

public class YearField extends ComboBox {

    private static int year = Calendar.getInstance().get(Calendar.YEAR);
    
    public YearField() {
        
        Integer thisYear = year;
        for (int i = 0; i < 50; i++) {
            addItem(thisYear--);
        }
        
    }
    
    @Override
    public Class<?> getType() {
        return Integer.class;
    }

}
