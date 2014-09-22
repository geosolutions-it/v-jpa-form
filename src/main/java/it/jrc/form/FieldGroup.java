package it.jrc.form;

import java.util.Collection;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.ui.Field;

/**
 * A simple wrapper for a {@link BeanFieldGroup}
 * 
 * @author Will Temperley
 *
 * @param <T>
 */
public class FieldGroup<T> {
    
    private BeanFieldGroup<T> fieldGroup;
    
    public FieldGroup(BeanFieldGroup<T> fieldGroup, String name, String description) {
        this.fieldGroup = fieldGroup;
        this.label = name;
        this.description = description;
    }

    public BeanFieldGroup<T> getFieldGroup() {
        return fieldGroup;
    }

    public void setFieldGroup(BeanFieldGroup<T> fieldGroup) {
        this.fieldGroup = fieldGroup;
    }

    private String label;

    public String getLabel() {
        return label;
    }

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setVisible(boolean active) {
        Collection<Field<?>> fields = fieldGroup.getFields();
        for (Field<?> field : fields) {
            field.setVisible(active);
        }
    }

}
