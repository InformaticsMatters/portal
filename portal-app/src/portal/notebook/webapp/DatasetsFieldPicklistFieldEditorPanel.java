package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.api.VariableType;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatasetsFieldPicklistFieldEditorPanel extends FieldEditorPanel<String> {
    private static final Logger LOGGER = Logger.getLogger(DatasetsFieldPicklistFieldEditorPanel.class.getName());
    private final Long cellId;
    private List<String> picklistItems;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;
    private DropDownChoice<String> picklistChoice;

    public DatasetsFieldPicklistFieldEditorPanel(String id, FieldEditorModel<String> fieldEditorModel, Long cellId) {
        super(id, fieldEditorModel);
        this.cellId = cellId;
        try {
            loadPicklist();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Error loading picklist", t);
            // TODO
        }
        addComponents();
    }

    private void loadPicklist() throws Exception {
        picklistItems = new ArrayList<>();
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        boolean first = true;
        for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null && variableInstance.getVariableDefinition().getVariableType().equals(VariableType.DATASET_MOLS)) {
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


    private Set<String> extractFieldNames(VariableInstance variableInstance) throws Exception {
        String string = notebookSession.readTextValue(variableInstance);
        if (string != null) {
            DatasetMetadata<? extends BasicObject> datasetMetadata = new ObjectMapper().readValue(string, DatasetMetadata.class);
            return datasetMetadata.getValueClassMappings().keySet();
        } else {
            return new HashSet<>();
        }
    }

    private void addComponents() {
        Model<String> model = new Model<String>() {
            @Override
            public String getObject() {
                return getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        picklistChoice = new DropDownChoice<>("picklist", model, picklistItems);
        add(picklistChoice);
    }

    @Override
    public boolean processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        CellInstance thisCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(this.cellId);
        boolean refresh = false;
        for (BindingInstance bindingInstance : thisCellInstance.getBindingInstanceMap().values()) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null && variableInstance.getCellId().equals(changedCellId)) {
                try {
                    loadPicklist();
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Error loading picklist for " + bindingInstance.getName(), t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
                refresh = true;
            }
        }
        return refresh;
    }

    @Override
    public void enableEditor(boolean editable) {
        picklistChoice.setEnabled(editable);
    }

}
