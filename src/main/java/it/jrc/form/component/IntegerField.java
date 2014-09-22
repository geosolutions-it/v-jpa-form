package it.jrc.form.component;

import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.ui.TextField;

public class IntegerField extends TextField {

  public IntegerField(String caption) {
    super();
    setCaption(caption);
    setConverter(new StringToIntegerConverter());
  }
  
  
  @Override
  public boolean isValid() {
    if (isEmpty()) {
      return false;
    }
    return super.isValid();
  }
}
