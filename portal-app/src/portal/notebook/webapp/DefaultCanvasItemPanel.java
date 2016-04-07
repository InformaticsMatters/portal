package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.types.Structure;
import portal.notebook.api.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author simetrias
 */
public class DefaultCanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCanvasItemPanel.class.getName());
    private Map<String, FieldEditorModel> optionEditorModelMap;
    private Form form;
    @Inject
    private NotebookSession notebookSession;
    private ListView<BindingsPanel.OptionInstance> optionListView;

    public DefaultCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        optionEditorModelMap = new LinkedHashMap<>();
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        Boolean executable = findCellInstance().getCellDefinition().getExecutable();
        // test for null shpuldn´t be necessary. means bug somewhere else
        if (executable != null && executable) {
            addExecutionStatusTimerBehavior();
        }
    }

    private void addForm() {
        form = new Form("form");
        add(form);

        List<BindingsPanel.OptionInstance> optionList = new ArrayList<>();
        BindingsPanel.CellInstance cellInstance = findCellInstance();
        for (BindingsPanel.OptionInstance optionInstance : cellInstance.getOptionInstanceMap().values()) {
            if (optionInstance.getOptionDescriptor().isEditable()) {
                optionList.add(optionInstance);
            }
        }
        optionListView = new ListView<BindingsPanel.OptionInstance>("option", optionList) {

            @Override
            protected void populateItem(ListItem<BindingsPanel.OptionInstance> listItem) {
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
        notebookSession.executeCell(findCellInstance().getId());
        fireContentChanged();
    }

    private void storeOptions() {
        for (String name : optionEditorModelMap.keySet()) {
            storeOption(name);
        }
    }

    private void storeOption(String name) {
        FieldEditorModel editorModel = optionEditorModelMap.get(name);
        BindingsPanel.OptionInstance optionInstance = findCellInstance().getOptionInstanceMap().get(name);
        optionInstance.setValue(editorModel.getValue());
    }

    private void addOptionEditor(ListItem<BindingsPanel.OptionInstance> listItem) {
        BindingsPanel.OptionInstance optionInstance = listItem.getModelObject();
        FieldEditorPanel fieldEditorPanel = createOptionEditor(optionInstance);
        optionEditorModelMap.put(optionInstance.getOptionDescriptor().getkey(), fieldEditorPanel.getFieldEditorModel());
        listItem.add(fieldEditorPanel);
    }

    private FieldEditorPanel createOptionEditor(BindingsPanel.OptionInstance optionInstance) {
        OptionDescriptor optionDescriptor = optionInstance.getOptionDescriptor();
        Object value = optionInstance.getValue() == null ? optionDescriptor.getDefaultValue() : optionInstance.getValue();
        if (optionDescriptor instanceof RestPicklistOptionDescriptor) {
            RestPicklistOptionDescriptor restPicklistOptionDescriptor = (RestPicklistOptionDescriptor) optionDescriptor;
            return new RestPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()), restPicklistOptionDescriptor.getQueryUri());
        } else if (optionDescriptor instanceof DatasetFieldOptionDescriptor) {
            return new DatasetFieldPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()), getCellId());
        } else if (optionDescriptor instanceof DatasetsFieldOptionDescriptor) {
            return new DatasetsFieldPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()), getCellId());
        } else if (optionDescriptor.getTypeDescriptor().getType() == Structure.class) {
            return new StructureFieldEditorPanel("optionEditor", "canvasMarvinEditor", new FieldEditorModel(value, optionDescriptor.getLabel()));
        } else if (optionDescriptor.getValues() != null && optionDescriptor.getValues().length > 0) {
            Object[] values = optionInstance.getOptionDescriptor().getValues();
            return new PicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()), values == null ? Collections.emptyList() : Arrays.asList(values));
        } else if (optionDescriptor.getTypeDescriptor() instanceof MultiLineTextTypeDescriptor) {
            return new MultilineFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == String.class) {
            return new StringFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == Integer.class) {
            return new IntegerFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == Float.class) {
            return new FloatFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == Boolean.class) {
            return new BooleanFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()));
        } else if (optionDescriptor.getTypeDescriptor().getType() == File.class) {
            OptionUploadCallback callback = new OptionUploadCallback(optionInstance);
            return new FileFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()), callback);
        } else {
            return new DummyFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor.getLabel()));
        }
    }

    private BindingsPanel.VariableInstance findVariableInstanceForFileOption() {
        for (BindingsPanel.VariableInstance variableInstance : findCellInstance().getVariableInstanceMap().values()) {
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
        private final BindingsPanel.OptionInstance optionInstance;

        OptionUploadCallback(BindingsPanel.OptionInstance optionInstance) {
            this.optionInstance = optionInstance;
        }

        @Override
        public void onUpload(String fileName, InputStream inputStream) {
            String optionName = optionInstance.getOptionDescriptor().getkey();
            BindingsPanel.OptionInstance liveOptionInstance = findCellInstance().getOptionInstanceMap().get(optionName);
            liveOptionInstance.setValue(fileName);
            notebookSession.storeCurrentNotebook();
            BindingsPanel.VariableInstance variableInstance = findVariableInstanceForFileOption();
            if (variableInstance == null) {
                throw new RuntimeException("Variable not found for option " + optionInstance.getOptionDescriptor().getkey());
            }
            notebookSession.writeTextValue(variableInstance, fileName);
            notebookSession.writeStreamValue(variableInstance, inputStream);
        }
    }

}
