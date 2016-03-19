package portal.notebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.DatasetMetadata;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

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
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellById(cellId);
        BindingInstance bindingInstance = cellInstance.getBindingMap().get(CellDefinition.VAR_NAME_INPUT);
        VariableInstance variableInstance = bindingInstance.getVariable();
        if (variableInstance != null) {
            loadFieldNames(variableInstance);
        }
    }

    private void loadFieldNames(VariableInstance variableInstance) {
        try {
            String string = (String) variableInstance.getValue();
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
        CellInstance thisCellInstance = notebookSession.getCurrentNotebookInstance().findCellById(this.cellId);
        BindingInstance bindingInstance = thisCellInstance.getBindingMap().get(CellDefinition.VAR_NAME_INPUT);
        VariableInstance variableInstance = bindingInstance.getVariable();
        if (variableInstance != null && variableInstance.getCellId().equals(changedCellId)) {
            loadPicklist();
            return true;
        } else {
            return false;
        }
    }
}
