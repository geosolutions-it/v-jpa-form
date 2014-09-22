package it.jrc.form.component;

import it.jrc.form.editor.EntityTable;
import it.jrc.form.filter.FilterField;
import it.jrc.form.filter.FilterPanel;
import it.jrc.persist.ContainerManager;
import it.jrc.persist.Dao;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;

public class SelectionTable<T> extends CustomField<T> {
    
    private static final int WIDTH_300 = 300;
    private Class<T> clazz;
    private ContainerManager<T> containerManager;
    private EntityTable<T> t;
    private FilterPanel<T> fp;

    public SelectionTable(final Class<T> clazz, Dao dao) {
        this.clazz = clazz;
        this.containerManager = new ContainerManager<T>(dao, clazz);
        JPAContainer<T> container = containerManager.getContainer();
        t = new EntityTable<T>(container);
        t.setWidth(FormConstants.FIELD_DEFAULT_WIDTH);
        
        t.addValueChangeListener(new Property.ValueChangeListener() {
            
            public void valueChange(
                    Property.ValueChangeEvent event) {
                
//                Item entity = t.getItem(event.getProperty().getValue());
                setValue(containerManager.findEntity(event.getProperty().getValue()));
            }
        });
        t.setPageLength(5);
        
        fp = new FilterPanel<T>(container, dao);
    }
    
    public void addColumn(SingularAttribute<T, ?> attr, String header) {
        t.setVisibleColumns(new Object[]{attr.getName()});
        t.setColumnHeader(attr.getName(), header);
        t.setColumnWidth(attr.getName(), WIDTH_300);
    }
    
    public void addFilterField(SingularAttribute<T, ?> attr){//, String header) {
        fp.addFilterField(attr);
    }
    
    public void addFilterField(SingularAttribute<T, ?> attr, String header) {
        FilterField<T, ?> f = fp.addFilterField(attr);
        f.setCaption(header);
    }
    
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        Object entity = newDataSource.getValue();
        
        if (entity != null) {
            t.setValue(containerManager.getId(entity));
            containerManager.getContainer().removeAllContainerFilters();
            containerManager.getContainer().refresh();
        }
        
        fp.resetFilters();
        
        super.setPropertyDataSource(newDataSource);
    }
    
    @Override
    public void setValue(T newFieldValue)
            throws Property.ReadOnlyException,
            ConversionException {
        
        
        if (newFieldValue != null) {
            t.setValue(containerManager.getId(newFieldValue));
        } else {
            t.setValue(null);
        }
        
        super.setValue(newFieldValue);
    }

    @Override
    protected Component initContent() {
        VerticalLayout vl = new VerticalLayout();
        vl.addComponent(fp);
        vl.addComponent(t);
        return vl;
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

}
