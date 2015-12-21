package portal.notebook;

import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.notebook.api.VariableType;
import portal.dataset.IDatasetDescriptor;
import portal.notebook.service.Strings;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

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

    public TableDisplayCanvasItemPanel(String id, CellModel cell, CellCallbackHandler callbackHandler) {
        super(id, cell);
        addForm();
        addGrid();
        load();
        setOutputMarkupId(true);
    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(new IndicatingAjaxSubmitLink("display", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                load();
            }
        });

        add(form);
    }

    private void addGrid() {
        addOrReplaceTreeGridVisualizer(new TableDisplayDatasetDescriptor(0l, "", 0));
    }

    private void load() {
        BindingModel bindingModel = getCellModel().getBindingModelMap().get("input");
        VariableModel variableModel = bindingModel == null ? null : bindingModel.getVariableModel();
        boolean assigned = variableModel != null && variableModel.getValue() != null;
        IDatasetDescriptor descriptor = assigned ? loadDescriptor() : null;
        if (descriptor == null) {
            descriptor = new TableDisplayDatasetDescriptor(0l, "", 0);
        }
        addOrReplaceTreeGridVisualizer(descriptor);
    }

    private IDatasetDescriptor loadDescriptor() {
        CellModel cellModel = getCellModel();
        VariableModel variableModel = cellModel.getBindingModelMap().get("input").getVariableModel();
        if (variableModel.getVariableType().equals(VariableType.FILE)) {
            return notebookSession.loadDatasetFromFile(variableModel.getValue().toString());
        } else if (variableModel.getVariableType().equals(VariableType.DATASET)) {
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

    }

    class ModelObject implements Serializable {

    }

}
