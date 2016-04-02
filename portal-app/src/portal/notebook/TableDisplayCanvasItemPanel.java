package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.dataset.IDatasetDescriptor;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author simetrias
 */
public class TableDisplayCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    private TableDisplayVisualizer tableDisplayVisualizer;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;
    private Long datasetDescriptorId;
    @Inject
    private CellChangeManager cellChangeManager;

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
        BindingInstance binding = cellInstance.getBindingInstanceMap().get("input");
        if (binding.getVariableInstance() != null && binding.getVariableInstance().getCellId().equals(changedCellId)) {
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
        BindingInstance bindingModel = findCellInstance().getBindingInstanceMap().get("input");
        VariableInstance variableInstance = bindingModel == null ? null : bindingModel.getVariableInstance();
        String value = variableInstance == null ? null : notebookSession.readTextValue(variableInstance);
        boolean assigned = value != null;
        IDatasetDescriptor descriptor = assigned ? loadDescriptor() : null;
        if (descriptor == null) {
            datasetDescriptorId = null;
            descriptor = new TableDisplayDatasetDescriptor(0L, "", 0);
        } else {
            datasetDescriptorId = descriptor.getId();
        }
        addOrReplaceTreeGridVisualizer(descriptor);
    }

    private IDatasetDescriptor loadDescriptor() {
        CellInstance cellInstance = findCellInstance();
        VariableInstance variableInstance = cellInstance.getBindingInstanceMap().get("input").getVariableInstance();
        return notebookSession.loadDatasetFromVariable(variableInstance);
    }

    private void addOrReplaceTreeGridVisualizer(IDatasetDescriptor datasetDescriptor) {
        tableDisplayVisualizer = new TableDisplayVisualizer("visualizer", datasetDescriptor);
        addOrReplace(tableDisplayVisualizer);
        TableDisplayNavigationPanel treeGridNavigation = new TableDisplayNavigationPanel("navigation", tableDisplayVisualizer);
        addOrReplace(treeGridNavigation);
    }

    private void storeCurrentSelection(UUID uuid, AjaxRequestTarget ajaxRequestTarget) {
        VariableInstance variableInstance = findCellInstance().getVariableInstanceMap().get("selection");
        MoleculeObject moleculeObject = notebookSession.findMoleculeObjectByRow(datasetDescriptorId, uuid);
        notebookSession.writeMoleculeValue(variableInstance, moleculeObject);
        notebookSession.storeCurrentNotebook();
        cellChangeManager.notifyVariableChanged(getCellId(), variableInstance.getVariableDefinition().getName(), ajaxRequestTarget);

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
