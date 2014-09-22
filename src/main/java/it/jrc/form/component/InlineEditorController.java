package it.jrc.form.component;

import it.jrc.form.JpaFieldFactory;
import it.jrc.form.editor.EditorPanel;
import it.jrc.form.editor.EditorPanelHeading;
import it.jrc.persist.ContainerManager;
import it.jrc.persist.Dao;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public abstract class InlineEditorController<T> extends CustomField<T> {

    protected Class<T> clazz;
    protected ContainerManager<T> containerManager;
    public JpaFieldFactory<T> ff;
    protected EditorPanel content;
    protected BeanFieldGroup<T> bfg;
    protected Window w;
    protected Dao dao;
    private EditorPanelHeading heading;
    

    public InlineEditorController(Class<T> clazz, Dao dao) {
        
        ff = new JpaFieldFactory<T>(dao, clazz);
        
        this.dao = dao;
        this.clazz = clazz;

        /*
         * The window
         */
        w = new Window();
        w.setModal(true);
        w.setWidth("728px");
        w.setHeight("450px");
        content = new EditorPanel();
        w.setContent(content);
        
        /*
         * Container
         */
        this.containerManager = new ContainerManager<T>(dao, clazz);
        
    }

    protected void doOpenWindow() {
        UI.getCurrent().addWindow(w);
        w.center();
        T entity = containerManager.newEntity();
        bfg.setItemDataSource(entity);
    }
    
    public Window getWindow() {
        return w;
    }
    
    //Repetition to avoid breaking esp code
    public void edit(T entity) {
        UI.getCurrent().addWindow(w);
        w.center();
        bfg.setItemDataSource(entity);
        
        heading.setCaption(entity.toString());
        
    }
    

    public void init() {
        bfg = ff.getFieldGroup("").getFieldGroup();
        FormLayout fl = new FormLayout();
        for (Field<?> field : bfg.getFields()) {
            fl.addComponent(field);
        }
    
        heading = content.addHeading("");
        content.addComponent(fl);
    
        Button saveButton = new Button("Save");
        content.addComponent(saveButton);
        saveButton.addClickListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
                doCommit();
            }
    
        });
        
    }

    private void doCommit() {
        if (bfg.isValid()) {
            try {
                bfg.commit();
                T entity = bfg.getItemDataSource().getBean();
                T committedEntity = commit(entity);
                setValue(committedEntity);
                doPostCommit(committedEntity);
                w.close();
                
                
            } catch (CommitException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Notification.show("Please ensure all required fields have been populated.");
        }
    }
    
    protected void doPostCommit(T entity) {
    }
    
    protected T commit(T entity) {
        return (T) dao.persist(entity);
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

    public void addField(SingularAttribute<T, ?> attr) {
        ff.addField(attr);
    }
    
    public JpaFieldFactory<T> getFf() {
        return ff;
    }

}