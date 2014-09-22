package it.jrc.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;

/**
 * A container which simplifies access to multiple {@link FieldGroup}s.
 * 
 * @author Will Temperley
 *
 * @param <T>
 */
public class FieldGroupManager<T> {

    protected Class<T> clazz;
    protected T entity;
    protected List<FieldGroup<T>> fieldGroupList = new ArrayList<FieldGroup<T>>();
    
    public void add(FieldGroup<T> fgm) {
        fieldGroupList.add(fgm);
    }

    public void setEntity(T entity) {
        this.entity = entity;
        
        for (FieldGroup<T> fgr : fieldGroupList) {
            fgr.getFieldGroup().setItemDataSource(entity);
        }
    }

    public T getEntity() {
        return entity;
    }

    /**
     * Commit all {@link FieldGroup}s.
     * 
     * @return
     */
    public void commit() throws CommitException {
        
        for (FieldGroup<T> fgr : fieldGroupList) {
            
            BeanFieldGroup<T> fieldGroup = fgr.getFieldGroup();

            if (fieldGroup.isValid()) {

                try {
                    fieldGroup.commit();

                } catch (CommitException e) {
                    e.getCause().printStackTrace();
                    // e.printStackTrace();
                    Notification.show("Commit failed: " + e.getMessage());
                    // Notification.TYPE_WARNING_MESSAGE);
                    // mainWindow.showNotification("Could not validate form",
                    // "Please check all fields have been entered correctly.",
                    // Notification.TYPE_WARNING_MESSAGE);
                    return;
                }

            } else {
                Notification.show("Please ensure the form is valid");
                return;
            }
        }
        
    }
    
    public List<FieldGroup<T>> getFieldGroupReprs() {
        return fieldGroupList;
    }

    public boolean isValid() {
    
        boolean valid = true;
    
        for (FieldGroup<T> fgr : fieldGroupList) {
            BeanFieldGroup<T> bfg = fgr.getFieldGroup();
            if (!bfg.isValid()) {
                valid = false;
            }
            Collection<Field<?>> fields = bfg.getFields();
            for (Field<?> field : fields) {
                try {
                    field.validate();
                } catch (InvalidValueException e) {
                    System.out.println(field.getCaption() + " " + e.getMessage());
                }
            }
        }
        return valid;
    }

}