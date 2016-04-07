package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.DatasetMetadata;
import portal.notebook.api.CellDefinition;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasetFieldPicklistFieldEditorPanel extends FieldEditorPanel {

    private final Long cellId;
    private List<String> picklistItems;
    @Inject
    private NotebookSession notebookSession;

    public DatasetFieldPicklistFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, Long cellId) {
        super(id, fieldEditorModel);
        this.cellId = cellId;
        loadPicklist();
        addComponents();
    }

    private void loadPicklist() {
        picklistItems = new ArrayList<>();
        BindingsPanel.CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        BindingsPanel.BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        BindingsPanel.VariableInstance variableInstance = bindingInstance.getVariableInstance();
        if (variableInstance != null) {
            loadFieldNames(variableInstance);
        }
    }

    private void loadFieldNames(BindingsPanel.VariableInstance variableInstance) {
        try {
            String string = notebookSession.readTextValue(variableInstance);
            if (string != null) {
                DatasetMetadata datasetMetadata = new ObjectMapper().readValue(string, DatasetMetadata.class);
                picklistItems.addAll(datasetMetadata.getValueClassMappings().keySet());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        DropDownChoice picklistChoice = new DropDownChoice("picklist", model, picklistItems);
        add(picklistChoice);
    }

    @Override
    public boolean processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        BindingsPanel.CellInstance thisCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(this.cellId);
        BindingsPanel.BindingInstance bindingInstance = thisCellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        BindingsPanel.VariableInstance variableInstance = bindingInstance.getVariableInstance();
        if (variableInstance != null && variableInstance.getCellId().equals(changedCellId)) {
            loadPicklist();
            return true;
        } else {
            return false;
        }
    }
}
