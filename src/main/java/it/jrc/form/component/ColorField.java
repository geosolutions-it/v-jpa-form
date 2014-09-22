package it.jrc.form.component;

import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.components.colorpicker.ColorChangeEvent;
import com.vaadin.ui.components.colorpicker.ColorChangeListener;

public class ColorField extends CustomField<Color> {

    private ColorPicker cp  = new ColorPicker();
    
    @Override
    protected Component initContent() {
        cp.addColorChangeListener(new ColorChangeListener() {
            
            public void colorChanged(ColorChangeEvent event) {
                ColorField.this.setValue(event.getColor());
            }
        });
        return cp;
    }

    @Override
    public Class<? extends Color> getType() {
        return Color.class;
    }
    
    @Override
    protected void setInternalValue(Color newValue) {
        cp.setColor(newValue);
        super.setInternalValue(newValue);
    }

//    @Override
//    public void setPropertyDataSource(@SuppressWarnings("rawtypes") Property newDataSource) {
//        
//        cp.setColor((Color) newDataSource.getValue());
//        
//        super.setPropertyDataSource(newDataSource);
//    }
    
    
    @Override
    public void setValue(Color newFieldValue)
            throws com.vaadin.data.Property.ReadOnlyException,
            ConversionException {
        cp.setColor(newFieldValue);
        super.setValue(newFieldValue);
    }
}
