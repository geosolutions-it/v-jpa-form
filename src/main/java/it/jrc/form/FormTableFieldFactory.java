package it.jrc.form;

import it.jrc.persist.Dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.metamodel.Attribute;






import com.vaadin.data.Container;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;

/**
 * 
 * @author will
 *
 * @param <T>
 */
public class FormTableFieldFactory<T> implements TableFieldFactory {

  private List<String> order = new ArrayList<String>();
  private Dao dao;
  private Map<String, Attribute<T, ?>> propMap = new HashMap<String, Attribute<T, ?>>();

  public FormTableFieldFactory(Dao dao) {
    this.dao = dao;
  }

  public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {

    Attribute<T, ?> prop = propMap.get(propertyId);
    if (prop == null) {
      return null;
    }
    
    String propName = prop.getName();
    

    Class<?> clazz = prop.getJavaType();

    if (clazz.equals(String.class) || Number.class.isAssignableFrom(clazz)) {
      TextField f = new TextField(propName);
      return f;
    }

    if (clazz.equals(Boolean.class)) {
      CheckBox cb = new CheckBox(propName);
      return cb;
    }

    if (clazz.getAnnotation(Entity.class) != null) {
      ComboBox cb = new ComboBox(propName);
      List<?> l = dao.all(clazz);
      for (Object x : l) {
        cb.addItem(x);
      }
      return cb;
    }
    return null;
  }

  public void addField(Attribute<T, ?> prop) {
    propMap.put(prop.getName(), prop);
    order.add(prop.getName());
  }

  public List<String> getOrder() {
    return order;
  }

}
