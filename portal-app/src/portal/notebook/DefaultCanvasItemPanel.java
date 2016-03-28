package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.client.NotebookClient;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.types.Structure;
import portal.notebook.api.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class DefaultCanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCanvasItemPanel.class.getName());
    private Map<String, FieldEditorModel> optionEditorModelMap;
    private Form form;
    @Inject
    private NotebookSession notebookSession;
    private ListView<OptionInstance> optionListView;
    private boolean fileDirty;

    public DefaultCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        optionEditorModelMap = new LinkedHashMap<>();
        fileDirty = false;
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        if (findCellInstance().getCellDefinition().getExecutable()) {
            addExecutionStatusTimerBehavior();
        }
    }

    private void addForm() {
        form = new Form("form");
        add(form);

        List<OptionInstance> optionList = new ArrayList<>();
        CellInstance cellInstance = findCellInstance();
        for (OptionInstance optionInstance : cellInstance.getOptionInstanceMap().values()) {
            if (optionInstance.getOptionDescriptor().isEditable()) {
                optionList.add(optionInstance);
            }
        }
        optionListView = new ListView<OptionInstance>("option", optionList) {

            @Override
            protected void populateItem(ListItem<OptionInstance> listItem) {
                addOptionEditor(listItem);
            }
        };
        form.add(optionListView);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        try {
            execute();
            getRequestCycle().find(AjaxRequestTarget.class).add(DefaultCanvasItemPanel.this.form);
        } catch (Throwable t) {
            LOGGER.error("Failed to execute cell", t);
        }
    }

    private void execute() throws IOException {
        storeOptions();
        notebookSession.storeCurrentNotebook();
        if (fileDirty) {
            commitFiles();
        }
        notebookSession.executeCell(findCellInstance().getId());
        fireContentChanged();
    }

    private void storeOptions() {
        for (String name : optionEditorModelMap.keySet()) {
            FieldEditorModel editorModel = optionEditorModelMap.get(name);
            OptionInstance optionInstance = findCellInstance().getOptionInstanceMap().get(name);
            optionInstance.setValue(editorModel.getValue());
            if (optionInstance.getOptionDescriptor().getTypeDescriptor().getType().equals(File.class)) {
                VariableInstance variableInstance = findVariableInstanceForFileOption();
                notebookSession.writeTextValue(variableInstance, optionInstance.getValue());
            }
        }
    }


    private void commitFiles() {
        for (OptionInstance optionInstance : findCellInstance().getOptionInstanceMap().values()) {
            if (optionInstance.getOptionDescriptor().getTypeDescriptor().getType().equals(File.class)) {
                VariableInstance variableInstance = findVariableInstanceForFileOption();
                notebookSession.commitFileForVariable(variableInstance);
                fileDirty = false;
            }
        }
    }

    private void addOptionEditor(ListItem<OptionInstance> listItem) {
        OptionInstance optionInstance = listItem.getModelObject();
        FieldEditorPanel fieldEditorPanel = createOptionEditor(optionInstance);
        optionEditorModelMap.put(optionInstance.getOptionDescriptor().getName(), fieldEditorPanel.getFieldEditorModel());
        listItem.add(fieldEditorPanel);
    }

    private FieldEditorPanel createOptionEditor(OptionInstance optionInstance) {
        OptionDescriptor optionDescriptor = optionInstance.getOptionDescriptor();
        Object value = optionInstance.getValue() == null ? optionDescriptor.getDefaultValue() : optionInstance.getValue();
        if (optionDescriptor instanceof RestPicklistOptionDescriptor) {
            RestPicklistOptionDescriptor restPicklistOptionDescriptor = (RestPicklistOptionDescriptor) optionDescriptor;
            return new RestPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()), restPicklistOptionDescriptor.getQueryUri());
        } else if (optionDescriptor instanceof DatasetFieldOptionDescriptor) {
            return new DatasetFieldPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()), getCellId());
        } else if (optionDescriptor instanceof DatasetsFieldOptionDescriptor) {
            return new DatasetsFieldPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()), getCellId());
        } else if (optionDescriptor.getTypeDescriptor().getType() == Structure.class) {
            return new StructureFieldEditorPanel("optionEditor", "canvasMarvinEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()));
        } else if (optionDescriptor.getValues() != null && optionDescriptor.getValues().length > 0) {
            return new PicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()), optionInstance.getOptionDescriptor().getPicklistValueList());
        } else if (optionDescriptor.getTypeDescriptor() instanceof MultiLineTextTypeDescriptor) {
            return new MultilineFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == String.class) {
            return new StringFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == Integer.class) {
            return new IntegerFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == Float.class) {
            return new FloatFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == Boolean.class) {
            return new BooleanFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == File.class) {
            OptionUploadCallback callback = new OptionUploadCallback(optionInstance);
            return new FileFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()), callback);
        } else {
            return new DummyFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getDisplayName()));
        }
    }

    private VariableInstance findVariableInstanceForFileOption() {
        for (VariableInstance variableInstance : findCellInstance().getVariableInstanceMap().values()) {
            if (variableInstance.getVariableDefinition().getVariableType().equals(VariableType.FILE)) {
                return variableInstance;
            }
        }
        return null;
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        boolean refresh = false;
        for (int i = 0; i < optionListView.size(); i++) {
            ListItem listItem = (ListItem) optionListView.get(i);
            FieldEditorPanel fieldEditorPanel = (FieldEditorPanel) listItem.get(0);
            if (fieldEditorPanel.processCellChanged(changedCellId, ajaxRequestTarget)) {
                refresh = true;
            }
        }
        if (refresh) {
            ajaxRequestTarget.add(this);
        }

    }

    class OptionUploadCallback implements FileFieldEditorPanel.Callback {
        private final OptionInstance optionInstance;

        OptionUploadCallback(OptionInstance optionInstance) {
            this.optionInstance = optionInstance;
        }

        @Override
        public void onUpload(InputStream inputStream) {
            VariableInstance variableInstance = findVariableInstanceForFileOption();
            if (variableInstance == null) {
                throw new RuntimeException("Variable not found for option " + optionInstance.getOptionDescriptor().getName());
            }
            notebookSession.storeTemporaryFileForVariable(variableInstance, inputStream);
            fileDirty = true;
        }
    }

}
