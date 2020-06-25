package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.io.IODescriptor;
import org.squonk.options.*;
import org.squonk.options.types.Structure;
import org.squonk.types.AbstractStreamType;
import org.squonk.types.NumberRange;
import portal.notebook.api.*;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * @author simetrias
 */
public class DefaultCanvasItemPanel extends CanvasItemPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCanvasItemPanel.class.getName());
    private Map<String, FieldEditorModel> optionEditorModelMap;
    private Form form;
    private ListView<OptionInstance> optionListView;
    private Label statusLabel;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;

    public DefaultCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        optionEditorModelMap = new LinkedHashMap<>();
        setOutputMarkupId(true);
        addForm();
        addTitleBarAndResultsViewer();
        CellInstance cellInstance = findCellInstance();
        CellDefinition cellDefinition = cellInstance.getCellDefinition();
        if (cellDefinition.getInitialWidth() != null) {
            cellInstance.setSizeWidth(cellDefinition.getInitialWidth());
        }
        if (cellDefinition.getInitialHeight() != null) {
            cellInstance.setSizeHeight(cellDefinition.getInitialHeight());
        }
        Boolean executable = cellDefinition.getExecutable();
        // test for null shouldn´t be necessary. means bug somewhere else
        if (executable != null && executable) {
            addExecutionStatusTimerBehavior();
        }
        addStatus();
    }

    private void addStatus() {
        statusLabel = createStatusLabel("cellStatus");
        add(statusLabel);
    }


    private void addForm() {
        form = new Form("form");
        add(form);

        List<OptionInstance> optionList = new ArrayList<>();
        CellInstance cellInstance = findCellInstance();
        for (OptionInstance optionInstance : cellInstance.getOptionInstanceMap().values()) {
            OptionDescriptor od = optionInstance.getOptionDescriptor();
            if (od.isVisible() && od.isMode(OptionDescriptor.Mode.User)) {
                optionList.add(optionInstance);
            }
        }
        optionListView = new ListView<OptionInstance>("option", optionList) {

            @Override
            protected void populateItem(ListItem<OptionInstance> listItem) {
                try {
                    addOptionEditor(listItem);
                } catch (Throwable t) {
                    LOGGER.warn("Error populating item", t);
                }
            }
        };
        form.add(optionListView);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public WebMarkupContainer getContentPanel() {
         return form;
    }

    @Override
    public void onExecute() {
        try {
            execute();
            getRequestCycle().find(AjaxRequestTarget.class).add(DefaultCanvasItemPanel.this.form);
        } catch (Throwable t) {
            LOGGER.error("Failed to execute cell", t);
            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
        }
    }

    private void execute() throws Exception {
        CellInstance cellInstance = findCellInstance();
        storeOptions(cellInstance);
        notebookSession.storeCurrentEditable();
        notebookSession.executeCell(cellInstance.getId());
        fireContentChanged();
    }

    private void storeOptions(CellInstance cellInstance) {
        for (String name : optionEditorModelMap.keySet()) {
            storeOption(cellInstance, name);
        }
    }

    private void storeOption(CellInstance cellInstance, String name) {
        FieldEditorModel editorModel = optionEditorModelMap.get(name);
        OptionInstance optionInstance = cellInstance.getOptionInstanceMap().get(name);
        optionInstance.setValue(editorModel.getValue());
    }

    private void addOptionEditor(ListItem<OptionInstance> listItem) {
        OptionInstance optionInstance = listItem.getModelObject();
        FieldEditorPanel fieldEditorPanel = createOptionEditor(optionInstance);
        fieldEditorPanel.enableEditor(optionInstance.getOptionDescriptor().isEditable());
        optionEditorModelMap.put(optionInstance.getOptionDescriptor().getKey(), fieldEditorPanel.getFieldEditorModel());
        listItem.add(fieldEditorPanel);
    }

    @SuppressWarnings("unchecked")
    private FieldEditorPanel createOptionEditor(OptionInstance optionInstance) {
        OptionDescriptor optionDescriptor = optionInstance.getOptionDescriptor();
        TypeDescriptor typeDescriptor = optionDescriptor.getTypeDescriptor();
        Object value = optionInstance.getValue();
        if (optionDescriptor instanceof RestPicklistOptionDescriptor) {
            RestPicklistOptionDescriptor restPicklistOptionDescriptor = (RestPicklistOptionDescriptor) optionDescriptor;
            return new RestPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor), restPicklistOptionDescriptor.getQueryUri());
        } else if (optionDescriptor instanceof DatasetsFieldOptionDescriptor) {
            return new DatasetsFieldPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor), getCellId());
        } else if (typeDescriptor.getType() == Structure.class) {
            String notebookId = notebookSession.getCurrentNotebookInfo().getId().toString();
            String cellInstanceId = findCellInstance().getId().toString();
            return new StructureFieldEditorPanel("optionEditor", "marvin" + notebookId + "_" + cellInstanceId, new FieldEditorModel(value, optionDescriptor), notebookSession.getStructureIOClient());
        } else if (optionDescriptor.getValues() != null && optionDescriptor.getValues().length > 0) {
            Object[] values = optionInstance.getOptionDescriptor().getValues();
            return new PicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor), values == null ? Collections.emptyList() : Arrays.asList(values));
        } else if (typeDescriptor instanceof FieldActionTypeDescriptor) {
            // TODO - this needs a new editor. For now we put the multi-line text there.
            return new MultilineFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        } else if (typeDescriptor instanceof DatasetFieldTypeDescriptor) {
            DatasetFieldTypeDescriptor dfTypeDescriptor = (DatasetFieldTypeDescriptor)typeDescriptor;
            if (dfTypeDescriptor.isMultiple()) {
                return new DatasetFieldMultiSelectFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor), getCellId());
            } else {
                return new DatasetFieldPicklistFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor), getCellId());
            }
        } else if (typeDescriptor instanceof MultiLineTextTypeDescriptor) {
            return new MultilineFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        } else if (typeDescriptor.getType() == String.class) {
            return new StringFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        } else if (typeDescriptor.getType() == Integer.class) {
            return new IntegerFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        } else if (typeDescriptor.getType() == Float.class) {
            return new DoubleFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        } else if (typeDescriptor.getType() == Boolean.class) {
            return new BooleanFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        } else if (NumberRange.class.isAssignableFrom(typeDescriptor.getType())) {
            return new NumberRangeFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        } else if (typeDescriptor.getType() == File.class) {
            OptionUploadCallback callback = new OptionUploadCallback(optionInstance);
            return new FileFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor), callback);
        } else {
            return new DummyFieldEditorPanel("optionEditor", new FieldEditorModel(value, optionDescriptor));
        }
    }

    private VariableInstance findVariableInstanceForFileOption() {
        // TODO - modify this so that it asks for a file of the required type
        for (VariableInstance variableInstance : findCellInstance().getVariableInstanceMap().values()) {
            IODescriptor iod = variableInstance.getVariableDefinition();
            if (AbstractStreamType.class.isAssignableFrom(iod.getPrimaryType())) {
                return variableInstance;
            }
        }
        return null;
    }

    @Override
    public void processCellChanged(CellChangeEvent evt, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        super.processCellChanged(evt, ajaxRequestTarget);
        boolean refresh = false;
        for (int i = 0; i < optionListView.size(); i++) {
            ListItem listItem = (ListItem) optionListView.get(i);
            FieldEditorPanel fieldEditorPanel = (FieldEditorPanel) listItem.get(0);
            try {
                if (fieldEditorPanel.processCellChanged(evt.getSourceCellId(), ajaxRequestTarget)) {
                    refresh = true;
                }
            } catch (Throwable t) {
                LOGGER.warn("Error processing change", t);
                notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                refresh = false;
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
        public void onUpload(String fileName, InputStream inputStream) {
            try {
                String optionName = optionInstance.getOptionDescriptor().getKey();
                OptionInstance liveOptionInstance = findCellInstance().getOptionInstanceMap().get(optionName);
                liveOptionInstance.setValue(fileName);
                notebookSession.storeCurrentEditable();
                VariableInstance variableInstance = findVariableInstanceForFileOption();
                if (variableInstance == null) {
                    throw new RuntimeException("Variable not found for option " + optionInstance.getOptionDescriptor().getKey());
                }
                notebookSession.writeTextValue(variableInstance, fileName);
                notebookSession.writeStreamValue(variableInstance, inputStream);
            } catch (Throwable t) {
                LOGGER.warn("Error uploading " + fileName, t);
                notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
            }
        }
    }

}
