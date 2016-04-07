package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.DatasetMetadata;
import portal.notebook.api.VariableType;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatasetsFieldPicklistFieldEditorPanel extends FieldEditorPanel {

    private final Long cellId;
    private List<String> picklistItems;
    @Inject
    private NotebookSession notebookSession;

    public DatasetsFieldPicklistFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, Long cellId) {
        super(id, fieldEditorModel);
        this.cellId = cellId;
        loadPicklist();
        addComponents();
    }

    private void loadPicklist() {
        picklistItems = new ArrayList<>();
        BindingsPanel.CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        boolean first = true;
        for (BindingsPanel.BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
            BindingsPanel.VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null && variableInstance.getVariableDefinition().getVariableType().equals(VariableType.DATASET)) {
                Set<String> fieldNames = extractFieldNames(variableInstance);
                if (first) {
                    picklistItems.addAll(fieldNames);
                    first = false;
                } else {
                    picklistItems.retainAll(fieldNames);
                }
            }
        }
    }


    private Set<String> extractFieldNames(BindingsPanel.VariableInstance variableInstance) {
        try {
            String string = notebookSession.readTextValue(variableInstance);
            if (string != null) {
                DatasetMetadata datasetMetadata = new ObjectMapper().readValue(string, DatasetMetadata.class);
                return datasetMetadata.getValueClassMappings().keySet();
            } else {
                return new HashSet<>();
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
        boolean refresh = false;
        for (BindingsPanel.BindingInstance bindingInstance : thisCellInstance.getBindingInstanceMap().values()) {
            BindingsPanel.VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null && variableInstance.getCellId().equals(changedCellId)) {
                loadPicklist();
                refresh = true;
            }
        }
        return refresh;
    }

}
