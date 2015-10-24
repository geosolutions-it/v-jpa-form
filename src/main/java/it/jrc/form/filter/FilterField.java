package it.jrc.form.filter;

import it.jrc.form.AdminStringUtil;
import it.jrc.form.component.IntegerField;
import it.jrc.persist.Dao;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.SingularAttribute;

import com.google.common.base.Strings;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Like;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * 
 * Uses field type information to generate a filter field - e.g. a
 * {@link ComboBox} for foreign key fields.
 * 
 * @author will
 * 
 * @param <T>
 */
public class FilterField<T, X> {

    private final SingularAttribute<T, X> prop;

    private final Field<?> field;

    private FilterPanel<T> filterPanel;

    public void setCaption(String caption) {
        if (field != null) {
            field.setCaption(caption);
        }
    }

    public FilterField(SingularAttribute<T, X> prop, Dao dao, String query) {
        this.prop = prop;
        String name = AdminStringUtil.splitCamelCase(prop.getName());
        Class<X> javaType = prop.getJavaType();

        if (javaType.equals(String.class)) {

            final TextField f = new TextField(name);
            f.setImmediate(true);

            f.addTextChangeListener(new FieldEvents.TextChangeListener() {
                public void textChange(TextChangeEvent event) {
                    f.setValue(event.getText());
                    filterPanel.doFiltering();
                }
            });
            this.field = f;

        } else if (prop.getJavaType().equals(Long.class)) {
            final IntegerField f = new IntegerField(name);
            f.setImmediate(true);
            f.addTextChangeListener(new FieldEvents.TextChangeListener() {
                public void textChange(TextChangeEvent event) {
                    f.setValue(event.getText());
                    filterPanel.doFiltering();
                }
            });
            this.field = f;

        } else if (prop.getJavaType().getAnnotation(Entity.class) != null) {
            
            TypedQuery<X> jpaQuery = dao.getEntityManager().createQuery(query, prop.getJavaType());
            List<X> beans = jpaQuery.getResultList();
            
            BeanItemContainer<X> bic = new BeanItemContainer<X>(
                    prop.getJavaType(), beans);

            final ComboBox cb = new ComboBox(name, bic);
            cb.setImmediate(true);

            cb.addValueChangeListener(new ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {
                    Property<X> val = event.getProperty();
                    cb.setValue(val);
                    filterPanel.doFiltering();
                }
            });
            this.field = cb;

        } else {

            throw new RuntimeException("Can't create field for type: "
                    + prop.getJavaType());
        }

    }

    public Filter getFilter() {
        Class<X> clazz = prop.getJavaType();
        String propertyName = prop.getName();
        Object value = field.getValue();

        if (value == null) {
            return null;
        }

        if (clazz.equals(String.class)) {

            String stringVal = (String) value;

            if (!Strings.isNullOrEmpty(stringVal)) {
                
                Like like = new Like(propertyName, "%" + stringVal + "%");
                like.setCaseSensitive(false);
                return like;
            }
            return null;

        } else if (clazz.equals(Long.class) || clazz.equals(Integer.class)) {

            if (field.isValid()) {
                return new Compare.Equal(propertyName, value);
            }
            return null;

        } else if (prop.getJavaType().getAnnotation(Entity.class) != null) {

            return new Compare.Equal(propertyName, value);

        } else {
            throw new RuntimeException(
                    "Can't create filter for object of type: " + clazz);
        }
    }

    public Field getField() {
        return field;
    }

    public void addListener(FilterPanel<T> filterPanel) {
        this.filterPanel = filterPanel;
    }

    public void reset() {
        field.setValue(null);
    }
    

}
