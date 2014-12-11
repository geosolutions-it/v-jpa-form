package it.jrc.form.view;

import it.jrc.form.FieldGroup;
import it.jrc.form.editor.SubmitPanel;

import java.util.List;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;

public class DefaultEditorView<T> extends CssLayout implements IEditorView<T>  {

    SubmitPanel submitPanel = new SubmitPanel();
    
    public DefaultEditorView() {
        addStyleName("layout-panel");
        setSizeFull();
    }


    @Override
    public SubmitPanel getTopSubmitPanel() {
        return submitPanel;
    }

    @Override
    public void buildForm(List<FieldGroup<T>> fields) {
        for (FieldGroup<T> fieldGroup : fields) {
            for (Field<?> field : fieldGroup.getFieldGroup().getFields()) {
                addComponent(field);
            }
        }
        
        addComponent(submitPanel);
    }


    @Override
    public SubmitPanel getBottomSubmitPanel() {
        return submitPanel;
    }

}
