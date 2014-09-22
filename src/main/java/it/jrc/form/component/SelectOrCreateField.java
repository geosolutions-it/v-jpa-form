package it.jrc.form.component;

import it.jrc.persist.Dao;

import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

public class SelectOrCreateField<T> extends InlineEditorController<T> {

    ComboBox combo;
    private Button button;

    public SelectOrCreateField(final Class<T> clazz, Dao dao) {

        super(clazz, dao);

        /*
         * The selection widget
         */
        this.combo = new ComboBox();
        combo.setWidth(FormConstants.FIELD_DEFAULT_WIDTH);
        populateCombo(clazz, dao);
        combo.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                Object obj = event.getProperty().getValue();
                setValue((T) obj);

            }
        });

        this.button = new Button("Add");

        button.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                doOpenWindow();
            }

        });

    }

    private void populateCombo(final Class<T> clazz, Dao dao) {
        List<T> items = dao.all(clazz);
        for (T t : items) {
            combo.addItem(t);
        }
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {

        combo.setValue(newDataSource.getValue());
        super.setPropertyDataSource(newDataSource);
    }

    @Override
    public void setValue(T newFieldValue) throws Property.ReadOnlyException,
            ConversionException {

        // TODO figure out the difference between setvaule and
        // setpropertydatasource once and for all
        super.setValue(newFieldValue);
    }

    @Override
    public T getValue() {
        T obj = super.getValue();
        if (obj == null) {
            return null;
        }
        Object id = dao.getId(obj);
        return dao.getEntityManager().find(clazz, id);
//        return obj;
    }

    @Override
    protected Component initContent() {
        CssLayout l = new CssLayout();
        l.addStyleName("select-create-field");
        l.addComponent(combo);
        l.addComponent(button);
        return l;
    }

    @Override
    protected void doPostCommit(T entity) {
        combo.addItem(entity);
        combo.select(entity);
    }

}
