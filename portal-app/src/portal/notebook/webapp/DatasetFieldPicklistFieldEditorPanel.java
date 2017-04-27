package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.options.DatasetFieldTypeDescriptor;
import portal.notebook.api.*;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatasetFieldPicklistFieldEditorPanel extends FieldEditorPanel {
    private static final Logger LOG = Logger.getLogger(DatasetsFieldPicklistFieldEditorPanel.class.getName());
    private final Long cellId;
    private List<String> picklistItems;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;
    private DropDownChoice picklistChoice;

    public DatasetFieldPicklistFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, Long cellId) {
        super(id, fieldEditorModel);
        this.cellId = cellId;
        try {
            loadPicklist();
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error loading picklist", t);
        }
        addComponents();
    }

    private void loadPicklist() throws Exception {
        picklistItems = new ArrayList<>();
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        if (cellInstance != null) {
            loadFieldNames(cellInstance);
        }
    }

    private void loadFieldNames(CellInstance cellInstance) throws Exception {

        DatasetFieldTypeDescriptor dfod = null;
        BindingInstance bindingInstance = null;
        if (getFieldEditorModel().getTypeDescriptor() != null && getFieldEditorModel().getTypeDescriptor() instanceof DatasetFieldTypeDescriptor) {
            dfod = (DatasetFieldTypeDescriptor) getFieldEditorModel().getTypeDescriptor();
            bindingInstance = cellInstance.getBindingInstanceMap().get(dfod.getInputName());
        }
        if (bindingInstance == null) {
            LOG.warning("Using a DatasetFieldPicklistFieldEditorPanel without a properly configured DatasetFieldTypeDescriptor. Avoid doing this.");
            bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        }
        if (bindingInstance != null) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                String json = notebookSession.readTextValue(variableInstance);
                if (json != null) {
                    DatasetMetadata<?> datasetMetadata = new ObjectMapper().readValue(json, DatasetMetadata.class);
                    if (dfod != null) {
                        for (Map.Entry<String, Class> e : datasetMetadata.getValueClassMappings().entrySet()) {
                            if (dfod.filter(e.getKey(), e.getValue())) {
                                picklistItems.add(e.getKey());
                            }
                        }
                    } else {
                        picklistItems.addAll(datasetMetadata.getValueClassMappings().keySet());
                    }
                }
            }
        }
    }

    private void addComponents() {
        Model model = new Model<String>() {

            @Override
            public String getObject() {
                return (String) getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        picklistChoice = new DropDownChoice("picklist", model, picklistItems);
        add(picklistChoice);
    }

    @Override
    public boolean processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        CellInstance thisCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(this.cellId);
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
