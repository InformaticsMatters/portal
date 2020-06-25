package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.types.io.JsonHandler;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DatasetFieldMultiSelectFieldEditorPanel extends FieldEditorPanel {
    private static final Logger LOG = Logger.getLogger(DatasetsFieldPicklistFieldEditorPanel.class.getName());
    private final Long cellId;
    private final List<String> picklistItems = new ArrayList<>();
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;
    private ListMultipleChoice<String> picklistChoice;
    private final DatasetFieldTypeDescriptor typeDescriptor;

    public DatasetFieldMultiSelectFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, Long cellId) {
        super(id, fieldEditorModel);
        this.cellId = cellId;
        if (getFieldEditorModel().getTypeDescriptor() != null && getFieldEditorModel().getTypeDescriptor() instanceof DatasetFieldTypeDescriptor) {
            typeDescriptor = (DatasetFieldTypeDescriptor) getFieldEditorModel().getTypeDescriptor();
        } else {
            throw new IllegalStateException("Expected to get a DatasetFieldTypeDescriptor");
        }
        if (!typeDescriptor.isMultiple()) {
            throw new IllegalStateException("DatasetFieldTypeDescriptor must be of type multiple");
        }
        try {
            loadPicklist();
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error loading picklist", t);
        }
        addComponents();
    }

    private void loadPicklist() throws Exception {
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        if (cellInstance != null) {
            loadFieldNames(cellInstance);
        }
    }

    private void loadFieldNames(CellInstance cellInstance) throws Exception {

        picklistItems.clear();

        BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(typeDescriptor.getInputName());
        if (bindingInstance != null) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                String json = notebookSession.readTextValue(variableInstance);
                if (json != null) {
                    DatasetMetadata<?> datasetMetadata = JsonHandler.getInstance().objectFromJson(json, DatasetMetadata.class);
                    for (Map.Entry<String, Class> e : datasetMetadata.getValueClassMappings().entrySet()) {
                        if (typeDescriptor.filter(e.getKey(), e.getValue())) {
                            LOG.finer("Adding field: " + e.getKey());
                            picklistItems.add(e.getKey());
                        }
                    }
                }
            }
        }
        LOG.fine("Final items: " + picklistItems.stream().collect(Collectors.joining(",")));
    }

    private void addComponents() {
        ArrayList<String> selectedItems = (ArrayList<String>) getFieldEditorModel().getValue();
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }
        Model model = new Model<ArrayList<String>>(selectedItems) {

            @Override
            public ArrayList<String> getObject() {
                return  (ArrayList<String>) getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(ArrayList<String> values) {
                getFieldEditorModel().setValue(values);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        picklistChoice = new ListMultipleChoice<>("multiselect", model, picklistItems);
        picklistChoice.setMaxRows(5);
        add(picklistChoice);
    }

    @Override
    public boolean processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        CellInstance thisCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(this.cellId);
        if (thisCellInstance == null) {
            LOG.warning("Can't find cell instance for cell " + this.cellId);
            return false;
        }
        BindingInstance bindingInstance = thisCellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        if (bindingInstance != null) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null && variableInstance.getCellId().equals(changedCellId)) {
                loadPicklist();
                return true;
            }
        }
        return false;
    }

    @Override
    public void enableEditor(boolean editable) {
        picklistChoice.setEnabled(editable);
    }
}
