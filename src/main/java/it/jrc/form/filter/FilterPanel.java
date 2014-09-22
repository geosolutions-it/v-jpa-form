package it.jrc.form.filter;

import it.jrc.persist.Dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.FormLayout;

public class FilterPanel<T> extends FormLayout {

  private JPAContainer<T> container;

  private List<FilterField<T, ?>> fields = new ArrayList<FilterField<T, ?>>();
  
  private static String DEFAULT_WIDTH = "300px";

  private Dao dao;


  /**
   * Remains hidden until a field is added.
   * 
   * @param container
   * @param dao
   */
  public FilterPanel(JPAContainer<T> container, Dao dao) {
    
    setVisible(false);
    this.container = container;
    this.dao = dao;
  }

  /**
   * Adds a filter field
   * 
   * @param prop
   */
  public <X> FilterField<T, X> addFilterField(SingularAttribute<T, X> prop) {
    FilterField<T, X> field = new FilterField<T, X>(prop, dao);
    field.getField().setWidth(DEFAULT_WIDTH);
    if (!isVisible()) {
      setVisible(true);
    }
    field.addListener(this);
    fields.add(field);
    this.addComponent(field.getField());
    return field;
  }
  
  public void resetFilters() {
    for (FilterField<T, ?> field : fields) {
        field.reset();
    }
  }

  public void doFiltering() {

    container.removeAllContainerFilters();

    for (FilterField<T, ?> field : fields) {

      Filter filter = field.getFilter();
      if (filter != null) {
	      container.addContainerFilter(filter);
      }
      
    }

  }

}
