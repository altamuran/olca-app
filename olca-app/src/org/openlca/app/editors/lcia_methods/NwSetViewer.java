package org.openlca.app.editors.lcia_methods;

import java.util.UUID;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.openlca.app.M;
import org.openlca.app.util.tables.Tables;
import org.openlca.app.viewers.table.AbstractTableViewer;
import org.openlca.app.viewers.table.modify.field.StringModifier;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;

class NwSetViewer extends AbstractTableViewer<NwSet> {

	private static final String NAME = M.NormalizationAndWeightingSet;
	private static final String UNIT = M.ReferenceUnit;

	private ImpactMethodEditor editor;

	public NwSetViewer(Composite parent, ImpactMethodEditor editor) {
		super(parent);
		this.editor = editor;
		getModifySupport().bind(NAME, new StringModifier<>(editor, "name"));
		getModifySupport().bind(UNIT,
				new StringModifier<>(editor, "weightedScoreUnit"));
		Tables.onDoubleClick(getViewer(), (event) -> {
			TableItem item = Tables.getItem(getViewer(), event);
			if (item == null)
				onCreate();
		});
	}

	public void setInput(ImpactMethod method) {
		if (method == null)
			setInput(new NwSet[0]);
		else
			setInput(method.getNwSets());
	}

	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new SetLabelProvider();
	}

	@Override
	protected String[] getColumnHeaders() {
		return new String[] { NAME, UNIT };
	}

	@OnAdd
	protected void onCreate() {
		NwSet set = new NwSet();
		set.setName("Enter a name");
		set.setRefId(UUID.randomUUID().toString());
		ImpactMethod method = editor.getModel();
		method.getNwSets().add(set);
		setInput(method.getNwSets());
		select(set);
		editor.setDirty(true);
	}

	@OnRemove
	protected void onRemove() {
		ImpactMethod method = editor.getModel();
		for (NwSet set : getAllSelected())
			method.getNwSets().remove(set);
		setInput(method.getNwSets());
		editor.setDirty(true);
	}

	private class SetLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int column) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof NwSet))
				return null;
			NwSet set = (NwSet) element;
			switch (columnIndex) {
			case 0:
				return set.getName();
			case 1:
				return set.getWeightedScoreUnit();
			default:
				return null;
			}
		}

	}

}
