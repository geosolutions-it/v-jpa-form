package it.jrc.form;

import it.jrc.form.component.ColorField;
import it.jrc.form.component.SelectOrCreateField;
import it.jrc.form.component.SelectionTable;
import it.jrc.persist.Dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import org.vaadin.tokenfield.TokenField;
import org.vaadin.tokenfield.TokenField.InsertPosition;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Link;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;

/**
 * Generates field groups based on {@link StaticMetamodel} properties. This
 * provides a type-safe method of creating forms.
 * 
 * Most fields can be created with the addField(prop) method, however extra
 * methods have been provided for differentiating between various types of text
 * areas.
 * 
 * Arbitrary fields can be added using the addField method.
 * 
 * @author Will Temperley
 * 
 * @param <T>
 * 
 */
public class JpaFieldFactory<T> {

    private static final String NULL_REPRESENTATION = "";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_FIELD_WIDTH = "300px";

    private List<String> order = new ArrayList<String>();
//    private List<String> disabledFields = new ArrayList<String>();

    private Map<String, Field<?>> fieldBuffer = new HashMap<String, Field<?>>();
    private Dao dao;
    private Class<T> clazz;

    // private ContainerManager<T> containerManager;

    public JpaFieldFactory(Dao dao, Class<T> clazz) {
        this.dao = dao;
        this.clazz = clazz;
    }

//    public void setDisabledFields(Attribute<T, ?>... prop) {
//        for (int i = 0; i < prop.length; i++) {
//            disabledFields.add(prop[i].getName());
//        }
//    }

    /**
     * Creates a field using type information obtained from static metamodel
     * properties.
     * 
     * @param prop
     *            the property from the {@link StaticMetamodel}
     */
    public <X> Component addField(Attribute<? extends T, X> prop) {

        Class<X> clazz = prop.getJavaType();

        if (Enum.class.isAssignableFrom(clazz)) {
            return addEnumField(prop);
        } else if (clazz.equals(String.class)) {
            return addTextField(prop);
        } else if (Number.class.isAssignableFrom(clazz)) {
            return addTextField(prop);
        } else if (clazz.equals(Boolean.class)) {
            return addBooleanField(prop);
        } else if (clazz.equals(Date.class)) {
            return addDateField(prop);
        } else if (clazz.getAnnotation(Entity.class) != null) {
            return addFkField(prop);
        } else if (clazz.equals(Set.class)) {
            return addM2MField(prop);
        }
        return null;
    }
    /**
     * Creates a field using type information obtained from static metamodel
     * properties.
     * 
     * @param prop
     *            the property from the {@link StaticMetamodel}
     */
    public <X> Component addField(Attribute<? extends T, X> prop, SingularAttribute<?, ?> orderBy) {

        Class<X> clazz = prop.getJavaType();

        if (Enum.class.isAssignableFrom(clazz)) {
            return addEnumField(prop);
        } else if (clazz.equals(String.class)) {
            return addTextField(prop);
        } else if (Number.class.isAssignableFrom(clazz)) {
            return addTextField(prop);
        } else if (clazz.equals(Boolean.class)) {
            return addBooleanField(prop);
        } else if (clazz.equals(Date.class)) {
            return addDateField(prop);
        } else if (clazz.getAnnotation(Entity.class) != null) {
            return addFkField(prop, orderBy);
        } else if (clazz.equals(Set.class)) {
            return addM2MField2(prop, orderBy);
        }
        return null;
    }
     

    public TextArea addTextArea(Attribute<? extends T, ?> prop) {
        TextArea f = new TextArea();
        f.setNullRepresentation(NULL_REPRESENTATION);
        f.setWidth(DEFAULT_FIELD_WIDTH);
        addField(prop, f);
        return f;
    }

    public void addRichTextArea(Attribute<? extends T, ?> prop) {
        RichTextArea f = new RichTextArea();
        f.setNullRepresentation(NULL_REPRESENTATION);
        f.setWidth(DEFAULT_FIELD_WIDTH);
        addField(prop, f);
    }

    public <X> void addTokenField(SetAttribute<? extends T, X> prop) {
        TokenField f = new TokenField("", InsertPosition.AFTER);
        Class<X> javaType = prop.getElementType().getJavaType();
        BeanItemContainer<X> c = new BeanItemContainer<X>(javaType);
        c.addAll(dao.all(javaType));
        f.setContainerDataSource(c);
        addField(prop, f);
    }

    public TextField addTextField(Attribute<? extends T, ?> prop) {
        TextField f = new TextField();
        f.setNullRepresentation(NULL_REPRESENTATION);
        f.setWidth(DEFAULT_FIELD_WIDTH);
        addField(prop, f);
        return f;
    }

    public void addColorField(String propName) {
        ColorField f = new ColorField();

        f.setCaption(AdminStringUtil.splitCamelCase(propName));

        fieldBuffer.put(propName, f);
        order.add(propName);
    }

    private ComboBox addFkField(Attribute<? extends T, ?> prop) {
        ComboBox cb = new ComboBox();
        cb.setWidth(DEFAULT_FIELD_WIDTH);

        List<?> list = dao.all(prop.getJavaType());

        for (Object x : list) {
            cb.addItem(x);
        }
        addField(prop, cb);
        return cb;
    }
    
    private ComboBox addFkField(Attribute<? extends T, ?> prop, SingularAttribute<?, ?> orderBy) {
        ComboBox cb = new ComboBox();
        cb.setWidth(DEFAULT_FIELD_WIDTH);

        List<?> list = dao.all(prop.getJavaType(), orderBy);

        for (Object x : list) {
            cb.addItem(x);
        }
        addField(prop, cb);
        return cb;
    }

    private ComboBox addEnumField(Attribute<? extends T, ?> prop) {
        ComboBox cb = new ComboBox();

        Object[] consts = prop.getJavaType().getEnumConstants();

        for (int i = 0; i < consts.length; i++) {
            cb.addItem(consts[i]);
        }
        addField(prop, cb);
        return cb;
    }

    private CheckBox addBooleanField(Attribute<? extends T, ?> prop) {
        CheckBox checkBox = new CheckBox();
        addField(prop, checkBox);
        return checkBox;
    }

    public <X> SelectionTable<X> addSelectionTable(
            Attribute<? extends T, X> prop) {
        SelectionTable<X> selectionTable = new SelectionTable<X>(
                prop.getJavaType(), dao);
        // selectionTable.addColumns(attributes);
        addField(prop, selectionTable);
        return selectionTable;
    }

    public <X> SelectOrCreateField<X> addSelectAndCreateField(
            Attribute<? extends T, X> prop,
            SingularAttribute<X, ?>... childProps) {

        SelectOrCreateField<X> socf = new SelectOrCreateField<X>(
                prop.getJavaType(), dao);

        for (int i = 0; i < childProps.length; i++) {
            socf.addField(childProps[i]);
        }
        addField(prop, socf);
        socf.init();
        return socf;

    }

    private TwinColSelect addM2MField(Attribute<? extends T, ?> prop) {

        TwinColSelect l = new TwinColSelect(AdminStringUtil.splitCamelCase(prop
                .getName()));
        l.setWidth("600px");

        /*
         * Get the type the attribute contains
         */
        Class<?> clazz = PluralAttribute.class.cast(prop).getElementType()
                .getJavaType();

        List<?> objects = dao.all(clazz);
        for (Object object : objects) {
            l.addItem(object);
        }

        l.setMultiSelect(true);

        addField(prop, l);
        return l;
    }
    
    private TwinColSelect addM2MField2(Attribute<? extends T, ?> prop, SingularAttribute<?, ?> orderBy) {

        TwinColSelect l = new TwinColSelect(AdminStringUtil.splitCamelCase(prop
                .getName()));
        l.setWidth("600px");

        /*
         * Get the type the attribute contains
         */
        Class<?> clazz = PluralAttribute.class.cast(prop).getElementType()
                .getJavaType();

        List<?> objects = dao.all(clazz, orderBy);
        for (Object object : objects) {
            l.addItem(object);
        }

        l.setMultiSelect(true);

        addField(prop, l);
        return l;
    }

    // FIXME copy (overload of above) for quick fix
    // Lost info
    public <X> TwinColSelect addM2MField(Attribute<? extends T, ?> prop,
            SingularAttribute<?, ?> orderBy) {

        TwinColSelect l = new TwinColSelect(AdminStringUtil.splitCamelCase(prop.getName()));
        l.setWidth("600px");

        /*
         * Get the type the attribute contains
         */
        Class<T> clazz = PluralAttribute.class.cast(prop).getElementType().getJavaType();

        List<T> objects = dao.all(clazz, orderBy);

        for (Object object : objects) {
            l.addItem(object);
        }

        l.setMultiSelect(true);

        addField(prop, l);
        return l;
    }

    private DateField addDateField(Attribute<? extends T, ?> prop) {
        DateField dateField = new DateField();
        dateField.setDateFormat(DEFAULT_DATE_FORMAT);
        addField(prop, dateField);
        return dateField;
    }

    /**
     * @return the order in which the fields should be displayed on the form.
     *         This is the same as the order they were added.
     */
    public List<String> getOrder() {
        return order;
    }

    /**
     * Adds a field to the temporary cache. Ensures order is also set.
     * 
     * @param prop
     *            the metamodel property
     * @param f
     *            the field
     */
    public void addField(Attribute<? extends T, ?> prop, Field<?> f) {

        String propName = prop.getName();

        String fieldDescription = dao.getFieldDescription(prop);

        /**
         * setDescription removed from Field interface in 7, therefore we have
         * to do this separately for every field type, or more simply up-cast
         * like here.
         */
        if (f instanceof AbstractField<?> && fieldDescription != null) {
            ((AbstractField<?>) f).setDescription(fieldDescription);
        }

        f.setCaption(AdminStringUtil.splitCamelCase(propName));

        fieldBuffer.put(propName, f);
        order.add(propName);
    }
    
//    public <X> void setFieldValue(Attribute<? extends T, X> prop, X value) {
//        fields.g
//    }

    /**
     * Using a string for the property is useful for transient fields, where the
     * {@link StaticMetamodel} doesn't work.
     * 
     * @param propName
     * @param f
     */
    public void addField(String propName, Field<?> f) {
        fieldBuffer.put(propName, f);
        order.add(propName);
    }

    /**
     * This works but is very hacky
     * 
     * @return
     */
    @Deprecated
    public FieldGroup<T> getFieldGroup(String name) {
        

        BeanFieldGroup<T> bfg = new BeanFieldGroup<T>(clazz);

        for (String propName : order) {
            Field<?> f = fieldBuffer.get(propName);
            if (f != null) {
                bfg.bind(f, propName);
            }
        }

        //FIXME Description from database??
        FieldGroup<T> fg = new FieldGroup<T>(bfg, name, null);

        fieldBuffer.clear();
        return fg;
    }

}