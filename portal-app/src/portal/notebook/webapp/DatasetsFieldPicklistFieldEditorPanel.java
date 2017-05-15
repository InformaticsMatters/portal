package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DatasetsFieldPicklistFieldEditorPanel extends FieldEditorPanel<String> {
    private static final Logger LOG = Logger.getLogger(DatasetsFieldPicklistFieldEditorPanel.class.getName());
    private final Long cellId;
    private final List<String> picklistItems = new ArrayList<>();
    private Class secondaryType;
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
            LOG.log(Level.WARNING, "Error loading picklist", t);
            // TODO
        }
        addComponents();
    }

    private void loadPicklist() throws Exception {
        picklistItems.clear();
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        boolean first = true;
        for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
            VariableInstance variableInstance = bindingInstance.getVariableInstance();
            if (variableInstance != null) {
                LOG.fine("variable: " + variableInstance.getCellId() + "/" + variableInstance.getVariableDefinition().getName());
                if (variableInstance.getVariableDefinition().getPrimaryType() == Dataset.class) {
                    Set<String> fieldNames = extractFieldNames(variableInstance);
                    if (first) {
                        LOG.finer("adding: " + fieldNames.stream().collect(Collectors.joining(",")));
                        secondaryType = variableInstance.getVariableDefinition().getSecondaryType();
                        picklistItems.addAll(fieldNames);
                        first = false;
                    } else if (variableInstance.getVariableDefinition().getSecondaryType() == secondaryType) {
                        LOG.finer("keeping: " + fieldNames.stream().collect(Collectors.joining(",")));
                        picklistItems.retainAll(fieldNames);
                    } else {
                        LOG.info("Skipping " + variableInstance.getVariableDefinition().getName() + " from cell " + variableInstance.getCellId() + " as not of type " + secondaryType);
                    }
                } else {
                    LOG.info("Skipping " + variableInstance.getVariableDefinition().getName() + " from cell " + variableInstance.getCellId() + " as not a Dataset");
                }
            }
        }
        LOG.fine("Final items: " + picklistItems.stream().collect(Collectors.joining(",")));
    }


    private Set<String> extractFieldNames(VariableInstance variableInstance) throws Exception {
        String string = notebookSession.readTextValue(variableInstance);
        if (string != null) {
            DatasetMetadata<? extends BasicObject> datasetMetadata = JsonHandler.getInstance().objectFromJson(string, DatasetMetadata.class);
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
                    LOG.log(Level.WARNING, "Error loading picklist for " + bindingInstance.getName(), t);
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
