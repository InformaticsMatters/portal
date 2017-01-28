package portal.notebook.webapp;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.squonk.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class TableDisplayCanvasItemPanel extends CanvasItemPanel {
    private static final Logger LOG = Logger.getLogger(TableDisplayCanvasItemPanel.class.getName());
    private Form<ModelObject> form;
    private TableDisplayVisualizer tableDisplayVisualizer;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;
    private Long datasetDescriptorId;
    @Inject
    private CellChangeManager cellChangeManager;
    private Label statusLabel;

    public TableDisplayCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();
        if (cellInstance.getSizeWidth() == null || cellInstance.getSizeWidth() == 0) {
            cellInstance.setSizeWidth(500);
        }
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        addGrid();
        try {
            load();
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error loading data", t);
            // TODO
        }
        addStatus();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript("fitTableDisplayGrid('" + getMarkupId() + "')"));
        makeCanvasItemResizable(container, "fitTableDisplayGrid", 325, 270);
    }

    @Override
    public void processCellChanged(CellChangeEvent evt, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        LOG.fine("Table cell changed");
        super.processCellChanged(evt, ajaxRequestTarget);

        if (doesCellChangeRequireRefresh(evt)) {
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

    private void load() throws Exception {
        LOG.fine("Loading table data");
        BindingInstance bindingModel = findCellInstance().getBindingInstanceMap().get("input");
        VariableInstance variableInstance = bindingModel == null ? null : bindingModel.getVariableInstance();
        String metaJson = variableInstance == null ? null : notebookSession.readTextValue(variableInstance);
        LOG.fine("Metadata: " + metaJson);
        boolean assigned = metaJson != null;
        IDatasetDescriptor descriptor = assigned ? loadDescriptor() : null;
        if (descriptor == null) {
            datasetDescriptorId = null;
            descriptor = new TableDisplayDatasetDescriptor(0L, "", 0);
        } else {
            datasetDescriptorId = descriptor.getId();
        }
        addOrReplaceTreeGridVisualizer(descriptor);
    }

    private IDatasetDescriptor loadDescriptor() throws Exception {
        CellInstance cellInstance = findCellInstance();
        VariableInstance variableInstance = cellInstance.getBindingInstanceMap().get("input").getVariableInstance();
        return notebookSession.loadDatasetFromVariable(variableInstance);
    }

    private void addOrReplaceTreeGridVisualizer(IDatasetDescriptor datasetDescriptor) {
        tableDisplayVisualizer = new TableDisplayVisualizer("visualizer", datasetDescriptor) {

            @Override
            protected void onItemSelectionChanged(IModel<DefaultMutableTreeNode> item, boolean newValue) {
                try {
                    Row row = (Row) item.getObject().getUserObject();
                    if (newValue) {
                        storeCurrentSelection(row.getUuid());
                    }
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Error persisting selection", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        };
        addOrReplace(tableDisplayVisualizer);
        TableDisplayNavigationPanel treeGridNavigation = new TableDisplayNavigationPanel("navigation", tableDisplayVisualizer);
        addOrReplace(treeGridNavigation);
    }

    private void storeCurrentSelection(UUID uuid) throws Exception {
        // TODO - allow multiple selection
        VariableInstance variableInstance = findCellInstance().getVariableInstanceMap().get("selection");
        MoleculeObject moleculeObject = notebookSession.findMoleculeObjectByRow(datasetDescriptorId, uuid);
        notebookSession.writeMoleculeValue(variableInstance, moleculeObject);
        notebookSession.storeCurrentEditable();
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        CellChangeEvent.DataValues evt = new CellChangeEvent.DataValues(getCellId(), variableInstance.getVariableDefinition().getName(), null);
        cellChangeManager.notifyDataValuesChanged(evt, target);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        try {
            notebookSession.reloadCurrentVersion();
            load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addStatus() {
        statusLabel = createStatusLabel("cellStatus");
        add(statusLabel);
    }

    class ModelObject implements Serializable {

    }
}
