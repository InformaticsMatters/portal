package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.dataset.IDatasetDescriptor;
import portal.notebook.api.*;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class TableDisplayCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    private TableDisplayVisualizer tableDisplayVisualizer;
    @Inject
    private NotebookSession notebookSession;

    public TableDisplayCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(500);
        }
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        addGrid();
        load();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript("fitTableDisplayGrid('" + getMarkupId() + "')"));
        makeCanvasItemResizable(container, "fitTableDisplayGrid", 325, 270);
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        CellInstance cellInstance = findCellInstance();
        BindingInstance binding = cellInstance.getBindingMap().get("input");
        if (binding.getVariable() != null && binding.getVariable().getCellId().equals(changedCellId)) {
            load();
            ajaxRequestTarget.add(this);
        }

    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

    private void addGrid() {
        addOrReplaceTreeGridVisualizer(new TableDisplayDatasetDescriptor(0L, "", 0));
    }

    private void load() {
        BindingInstance bindingModel = findCellInstance().getBindingMap().get("input");
        VariableInstance variableModel = bindingModel == null ? null : bindingModel.getVariable();
        boolean assigned = variableModel != null && variableModel.getValue() != null;
        IDatasetDescriptor descriptor = assigned ? loadDescriptor() : null;
        if (descriptor == null) {
            descriptor = new TableDisplayDatasetDescriptor(0L, "", 0);
        }
        addOrReplaceTreeGridVisualizer(descriptor);
    }

    private IDatasetDescriptor loadDescriptor() {
        CellInstance cellInstance = findCellInstance();
        VariableInstance variableModel = cellInstance.getBindingMap().get("input").getVariable();
        VariableType variableType = variableModel.getVariableType();
        if (variableType.equals(VariableType.FILE)) {
            return notebookSession.loadDatasetFromFile(variableModel.getValue().toString());
        } else if (variableType.equals(VariableType.DATASET)) {
            return notebookSession.loadDatasetFromSquonkDataset(variableModel);
        } else if (variableModel.getValue() instanceof Strings) {
            return notebookSession.createDatasetFromStrings((Strings) variableModel.getValue(), variableModel.getName());
        } else if (variableModel.getValue() instanceof List) {
            return notebookSession.createDatasetFromMolecules((List<MoleculeObject>) variableModel.getValue(), variableModel.getName());
        } else {
            return null;
        }
    }

    private void addOrReplaceTreeGridVisualizer(IDatasetDescriptor datasetDescriptor) {
        tableDisplayVisualizer = new TableDisplayVisualizer("visualizer", datasetDescriptor);
        addOrReplace(tableDisplayVisualizer);
        TableDisplayNavigationPanel treeGridNavigation = new TableDisplayNavigationPanel("navigation", tableDisplayVisualizer);
        addOrReplace(treeGridNavigation);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        notebookSession.reloadCurrentNotebook();
        load();
    }

    class ModelObject implements Serializable {

    }
}
