package it.jrc.form.component;

import it.jrc.form.AdminStringUtil;
import it.jrc.form.FormTableFieldFactory;
import it.jrc.persist.Dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.And;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * FIXME - bug with ordering means ordinal is not updated until drag-drop is
 * performed (so order table then save has no effect)
 * 
 * @author will
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class SetTable<T> extends CustomField<List<T>> {

	Logger logger = LoggerFactory.getLogger(SetTable.class);

	Table table = new Table();
	BeanItemContainer<T> beanContainer;

	private List<T> beanList;

	private VerticalLayout tableContainer;

	public SetTable(final Class<T> clazz, FormTableFieldFactory<T> ff,
			Dao dao) {

		beanContainer = new BeanItemContainer<T>(clazz);
		final EntityManager em = dao.getEntityManager();

		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {
			public void drop(DragAndDropEvent dropEvent) {
				DataBoundTransferable t = (DataBoundTransferable) dropEvent
						.getTransferable();
				@SuppressWarnings("unchecked")
				T sourceItemId = (T) t.getItemId(); // returns our Bean

				AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent
						.getTargetDetails());
				@SuppressWarnings("unchecked")
				T targetItemId = (T) dropData.getItemIdOver(); // returns our
																// Bean

				// No move if source and target are the same, or there is no
				// target
				if (sourceItemId == targetItemId || targetItemId == null) {
					return;
				}

				// Let's remove the source of the drag so we can add it back
				// where
				// requested...
				table.removeItem(sourceItemId);

				if (dropData.getDropLocation() == VerticalDropLocation.BOTTOM) {
					table.addItemAfter(targetItemId, sourceItemId);
				} else {
					Object prevItemId = table.prevItemId(targetItemId);
					table.addItemAfter(prevItemId, sourceItemId);
				}

			}

			public AcceptCriterion getAcceptCriterion() {
				return new And(new SourceIs(table), AcceptItem.ALL);
			}
		});

		table.setContainerDataSource(beanContainer);
		table.setTableFieldFactory(ff);

		/*
		 * Add delete column
		 */
		table.addGeneratedColumn("Delete", new Table.ColumnGenerator() {
			public Component generateCell(Table source, final Object itemId,
					Object columnId) {
				Button b = new Button();
				b.setStyleName(BaseTheme.BUTTON_LINK);
				b.setIcon(new ThemeResource("../runo/icons/16/cancel.png"));
				
				b.addClickListener(new Button.ClickListener() {
					public void buttonClick(ClickEvent event) {
						em.remove(beanContainer.getItem(itemId).getBean());
						beanContainer.removeItem(itemId);
						table.setPageLength(table.getPageLength() - 1);
					}
				});
				return b;
			}
		});
		ff.getOrder().add("Delete");

		table.setVisibleColumns(ff.getOrder().toArray());
		table.setEditable(!isReadOnly());

		VerticalLayout tableContainer = new VerticalLayout();
		tableContainer.addComponent(table);

		Button newButton = new Button("New", new Button.ClickListener() {
		    
			public void buttonClick(ClickEvent event) {

				T entity;
				try {
					entity = clazz.newInstance();
					beanContainer.addBean(entity);
					table.setPageLength(table.getPageLength() + 1);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});
		tableContainer.addComponent(newButton);

		this.tableContainer = tableContainer;
	}

	@Override
	protected Component initContent() {
		return tableContainer;
	}

	@Override
	public Class<? extends List<T>> getType() {
//		return (Class<? extends List<T>>) List.class;
	    return null;
	}

	@Override
	public void setPropertyDataSource(Property newDataSource) {
		Object value = newDataSource.getValue();

		/*
      * 
      */
		if (value instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<T> beans = (List<T>) value;
			this.beanList = beans;
			beanContainer.removeAllItems();
			beanContainer.addAll(beanList);
			table.setPageLength(beanList.size());
		} else if (value == null) {
			logger.debug("null value");
			table.setPageLength(1);
		}

		super.setPropertyDataSource(newDataSource);
	}

	@Override
	public List<T> getValue() {
		/*
		 * TODO: check if this causes the issue with losing the reference to
		 * collections. Could utilize a new
		 */
		if (beanList == null) {
			beanList = new ArrayList<T>();
		}
		beanList.clear();
		for (Object itemId : beanContainer.getItemIds()) {
			beanList.add(beanContainer.getItem(itemId).getBean());
		}
		return beanList;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		table.setEditable(!readOnly);
		super.setReadOnly(readOnly);
	}

	/**
	 * TODO:
	 * 
	 * @param <X>
	 * @param prop
	 */
	public <X> void addColumn(SingularAttribute<T, X> prop) {

		String propName = prop.getName();
		table.addContainerProperty(propName, prop.getClass(), null);
		table.setColumnHeader(propName,
				AdminStringUtil.splitCamelCase(propName));

	}

	public <X> void addColumn(SingularAttribute<T, X> prop, String colHead) {

		String propName = prop.getName();
		table.addContainerProperty(propName, prop.getClass(), null);
		table.setColumnHeader(propName, colHead);
	}

	@Override
	public void setBuffered(boolean buffered) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBuffered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAllValidators() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addValueChangeListener(ValueChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeValueChangeListener(ValueChangeListener listener) {
		// TODO Auto-generated method stub

	}

}