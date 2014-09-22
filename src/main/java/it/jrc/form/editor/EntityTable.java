package it.jrc.form.editor;

import it.jrc.form.AdminStringUtil;

import javax.persistence.metamodel.SingularAttribute;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;

public class EntityTable<T> extends Table {

    private static final int URL_COL_WIDTH = 100;
    private JPAContainer<T> container;

    /**
     * A table of entities
     * 
     * TODO: ordering column default??
     * 
     * @param container
     */
    public EntityTable(JPAContainer<T> container) {

        super();

        setImmediate(true);
        setEditable(false);
        setMultiSelect(false);
        setSelectable(true);

        setContainerDataSource(container);

        setVisibleColumns(new Object[] {});
        
        this.container = container;

    }
    
    public EntityTable(BeanItemContainer<T> beanContainer) { 
        
        setImmediate(true);
        setEditable(false);
        setMultiSelect(false);
        setSelectable(true);
        
        setContainerDataSource(beanContainer);
    }
    

    public void refresh() {
        
        if (container != null) {
            container.refresh();
        }
    }

    public void setDefaultSortAttribute(SingularAttribute<T, ?> prop) {
        setSortContainerPropertyId(prop.getName());
    }

    public <X> void setColumnWidth(SingularAttribute<T, X> prop, int width) {
        setColumnWidth(prop.getName(), width);
    }


    /**
     * Adds a column with links that open in a new window.
     * 
     * @param prop
     */
    public <X> void addUrlColumn(SingularAttribute<T, X> prop) {

        String propName = prop.getName();

        addGeneratedColumn(propName, new Table.ColumnGenerator() {
            public Object generateCell(Table source, Object itemId,
                    Object columnId) {
                Item item = source.getItem(itemId);
                Object colValue = item.getItemProperty(columnId).getValue();

                if (colValue == null) {
                    return null;
                }

                String columnValue = String.valueOf(colValue);
                Link link = new Link(columnValue, new ExternalResource(
                        columnValue));
                link.setTargetName("_blank");
                return link;
            }
        });

        setColumnHeader(propName, AdminStringUtil.splitCamelCase(propName));
        setColumnWidth(propName, URL_COL_WIDTH);
    }

    /**
     * For each of the provided {@link SingularAttribute}s adds a column to the table.
     * 
     * @param cols
     */
	public void addColumns(SingularAttribute<T, ?>... cols) {
		
		String[] colNames = new String[cols.length];
		for (int i = 0; i < cols.length; i++) {
			colNames[i] = cols[i].getName();
		}
		
		//FIXME HACK
		this.setVisibleColumns(colNames);
		for (String string : colNames) {
		    this.setColumnHeader(string, AdminStringUtil.splitCamelCase(string));
        }
	}
	

}
