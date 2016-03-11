package portal.notebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.DatasetMetadata;
import portal.notebook.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasetFieldsPicklistFieldEditorPanel extends FieldEditorPanel {

    private final CellInstance cellInstance;
    private final OptionInstance optionInstance;
    private List<String> picklistItems;

    public DatasetFieldsPicklistFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, CellInstance cellInstance, OptionInstance optionInstance) {
        super(id, fieldEditorModel);
        this.cellInstance = cellInstance;
        this.optionInstance = optionInstance;
        loadPicklist();
        addComponents();
    }

    private void loadPicklist() {
        picklistItems = new ArrayList<>();
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
}
