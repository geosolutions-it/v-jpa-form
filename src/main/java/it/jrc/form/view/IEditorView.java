package it.jrc.form.view;

import it.jrc.form.FieldGroup;
import it.jrc.form.editor.SubmitPanel;

import java.util.List;

import com.vaadin.ui.Component;

/**
 * Form views should implement this.  This allows views to be "throwaway" components.
 * 
 * @author Will Temperley
 * 
 */
public interface IEditorView<T> extends Component {

    /**
     * Acting like a factory here, the view provides the panel. Event management
     * is up to the controller.
     * 
     * @return
     */
    public SubmitPanel getTopSubmitPanel();
    
    /**
     * Acting like a factory here, the view provides the panel. Event management
     * is up to the controller.
     * 
     * @return
     */
    public SubmitPanel getBottomSubmitPanel();

    /**
     * Add fields to form. Implementations are expected to only manage the
     * display, not manipulate form values.
     * 
     * @param fields
     */
    public void buildForm(List<FieldGroup<T>> fields);

}
