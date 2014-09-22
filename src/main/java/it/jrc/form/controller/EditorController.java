package it.jrc.form.controller;


import it.jrc.form.ButtonFactory;
import it.jrc.form.FieldGroup;
import it.jrc.form.FieldGroupManager;
import it.jrc.form.JpaFieldFactory;
import it.jrc.form.editor.BaseEditor;
import it.jrc.form.editor.SubmitPanel;
import it.jrc.form.view.IEditorView;
import it.jrc.persist.ContainerManager;
import it.jrc.persist.Dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

/**
 * 
 * A simplified verion of {@link BaseEditor}
 * 
 * Probably do not require all the container stuff.
 * 
 */
public abstract class EditorController<T> extends Panel {

//    private static final String EDITING_FORMAT_STRING = "Editing: %s";

    private static final String SAVE_MESSAGE = "Saved successfully.";

    private Logger logger = LoggerFactory.getLogger(EditorController.class);

    protected Dao dao;

    private FieldGroupManager<T> fgm = new FieldGroupManager<T>();
    
    /*
     * The field factory
     */
    protected JpaFieldFactory<T> ff;

    protected ContainerManager<T> containerManager;

    private EditCompleteListener<T> editCompleteListener;
    
    public interface EditCompleteListener<T> {
        
        public void onEditComplete(T entity);
        
    }

    public EditorController(final Class<T> clazz, final Dao dao) {

        this.dao = dao;
        this.ff = new JpaFieldFactory<T>(dao, clazz);

        containerManager = new ContainerManager<T>(dao, clazz);

        /*
         * Filter panel
         */
//        filterPanel = new FilterPanel<T>(containerManager.getContainer(), dao);
    }

    /**
     * Designed to be overridden to allow customised form construction.
     * 
     * @param view
     */
    public void init(IEditorView<T> view) {

        this.setContent(view);

        view.buildForm(fgm.getFieldGroupReprs());

        buildSubmitPanel(view.getSubmitPanel());

    }

    protected FieldGroup<T> addFieldGroup(String name) {
        FieldGroup<T> fieldGroupMeta = ff.getFieldGroup(name);
        fgm.add(fieldGroupMeta);
        return fieldGroupMeta;
    }

    protected void buildSubmitPanel(SubmitPanel submitPanel) {
        /*
         * Submit panel
         */
        Button commit = ButtonFactory.getButton(
                ButtonFactory.SAVE_BUTTON_CAPTION, ButtonFactory.SAVE_ICON);
        commit.setEnabled(containerManager.canUpdate());

        commit.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                commitForm(true);
            }
        });

        Button delete = ButtonFactory.getButton(
                ButtonFactory.DELETE_BUTTON_CAPTION, ButtonFactory.DELETE_ICON);
        delete.setEnabled(containerManager.canDelete());

        delete.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {

                ConfirmDialog.show(UI.getCurrent(),
                        "Are you sure you wish to delete this record?",
                        new ConfirmDialog.Listener() {

                            public void onClose(ConfirmDialog dialog) {
                                if (dialog.isConfirmed()) {
                                    doDelete();

                                }
                            }

                        });
            }
        });

        submitPanel.addLeft(commit);
        submitPanel.addRight(delete);
    }

    /**
     * The field factory is a custom component for each editor. For polymorphic
     * entities, the field factory is passed the object to allow it to determine
     * the correct subclass.
     * 
     * @return the custom field factory
     */
    public JpaFieldFactory<T> getFieldFactory() {
        return ff;
    }

    protected boolean commitForm(boolean showNotification) {

        T entity = fgm.getEntity();

        boolean x = fgm.isValid();
        if (x == false) {
            Notification.show("Validation failed");
            return false;
        }

        try {
            fgm.commit();
        } catch (CommitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        /*
         * Subclasses may define tasks to perform pre commit.
         */
        doPreCommit(entity);

        Object id = containerManager.addEntity(entity);

        if (showNotification) {
            Notification.show(SAVE_MESSAGE);
        }

        entity = containerManager.findEntity(id);

        /*
         * Subclasses may define tasks to perform post-commit.
         */
        doPostCommit(entity);

        return true;
    }

    public void doCreate() {
        fgm.setEntity(containerManager.newEntity());
    }

    public void doUpdate(T entity) {
        fgm.setEntity(entity);
    }

    private void doDelete() {
        T entity = fgm.getEntity();
        if (entity == null) {
            logger.error("Delete attempted with null entity.");
            return;
        }
        containerManager.deleteEntity(entity);
        doPostDelete(entity);
    }

    /**
     * Called after deletion for any cleanup that may be required.
     * 
     * @param entity
     */
    protected void doPostDelete(T entity) {
        // containerManager.refresh();
    }

    /**
     * Can be overridden by classes that require parent child associations to be
     * fixed.
     */
    protected void doPreCommit(T entity) {

    }

    /**
     * Subclasses can use this method for obtaining notification of commit
     * actions.
     * 
     * @param entity
     */
    protected void doPostCommit(T entity) {
        if (entity == null) {
        System.out.println("ENTITY IS NULL");
        }
        fireEditComplete(entity);
    }

    public void refresh() {
        containerManager.refresh();
    }

    public T getEntity() {
        return fgm.getEntity();
    }

    protected List<FieldGroup<T>> getFieldGroups() {
        return fgm.getFieldGroupReprs();
    }

    public void addEditCompleteListener(EditCompleteListener<T> listener) {
        this.editCompleteListener = listener;
    }
    
    private void fireEditComplete(T entity) {
        if (editCompleteListener != null) {
            editCompleteListener.onEditComplete(entity);
        }
    }
}
