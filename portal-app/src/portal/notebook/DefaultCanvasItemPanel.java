package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.notebook.api.OptionType;
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
        if (retrieveCellInstance().getCellDefinition().getExecutable()) {
            addExecutionStatusTimerBehavior();
        }
    }

    private void addForm() {
        form = new Form("form");
        add(form);

        List<OptionInstance> optionList = new ArrayList<>();
        CellInstance cellInstance = retrieveCellInstance();
        for (OptionInstance optionInstance : cellInstance.getOptionMap().values()) {
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
        notebookSession.executeCell(retrieveCellInstance().getId());
        fireContentChanged();
    }

    private void storeOptions() {
        for (String name : optionEditorModelMap.keySet()) {
            FieldEditorModel editorModel = optionEditorModelMap.get(name);
            OptionInstance optionInstance = retrieveCellInstance().getOptionMap().get(name);
            optionInstance.setValue(editorModel.getValue());
            if (optionInstance.getOptionDescriptor().getTypeDescriptor().getType().equals(File.class)) {
                VariableInstance variableInstance = findVariableInstanceForFileOption();
                variableInstance.setValue(optionInstance.getValue());
            }
        }
    }


    private void commitFiles() {
        for (OptionInstance optionInstance : retrieveCellInstance().getOptionMap().values()) {
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
        OptionDescriptor optionDefinition = optionInstance.getOptionDescriptor();
        Object value = optionInstance.getValue() == null ? optionDefinition.getDefaultValue() : optionInstance.getValue();
        if (OptionType.SIMPLE.equals(optionDefinition.getOptionType())) {
            if (optionDefinition instanceof DatasetFieldOptionDescriptor) {
                return new DatasetFieldsPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()), getCellId());
            } else if (optionDefinition.getTypeDescriptor().getType() == Structure.class) {
                return new StructureFieldEditorPanel("optionEditor", "canvasMarvinEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
            } else if (optionDefinition.getValues() != null && optionDefinition.getValues().length > 0) {
                return new PicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()),optionInstance.getOptionDescriptor().getPicklistValueList());
            } else if (optionDefinition.getTypeDescriptor() instanceof MultiLineTextTypeDescriptor) {
                return new MultilineFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == String.class) {
                return new StringFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == Integer.class) {
                return new IntegerFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == Float.class) {
                return new FloatFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == Boolean.class) {
                return new BooleanFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == File.class) {
                OptionUploadCallback callback = new OptionUploadCallback(optionInstance);
                return new FileFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()), callback);
            } else {
                return new DummyFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
            }
        } else {
            return new DummyFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDefinition.getDisplayName()));
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

    private VariableInstance findVariableInstanceForFileOption() {
        for (VariableInstance variableInstance : retrieveCellInstance().getOutputVariableMap().values()) {
            if (variableInstance.getVariableType().equals(VariableType.FILE)) {
                return variableInstance;
            }
        }
        return null;
    }

    @Override
    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) {
        boolean refresh = false;
        for (int i = 0; i < optionListView.size(); i++) {
            ListItem listItem = (ListItem)optionListView.get(i);
            FieldEditorPanel fieldEditorPanel = (FieldEditorPanel)listItem.get(0);
            if (fieldEditorPanel.processCellChanged(changedCellId, ajaxRequestTarget)) {
                refresh = true;
            }
        }
        if (refresh) {
            ajaxRequestTarget.add(this);
        }

    }

}
