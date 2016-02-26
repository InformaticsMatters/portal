package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.notebook.api.OptionType;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.types.Structure;
import portal.notebook.api.OptionInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.api.VariableType;

import javax.inject.Inject;
import java.io.IOException;
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
    private Map<String, FieldEditorModel> variableEditorModelMap;
    private Form form;
    @Inject
    private NotebookSession notebookSession;
    private ListView<OptionInstance> optionListView;
    private ListView<VariableInstance> variableListView;

    public DefaultCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        optionEditorModelMap = new LinkedHashMap<>();
        variableEditorModelMap = new LinkedHashMap<>();
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
    }

    private void addForm() {
        form = new Form("form");
        add(form);

        List<VariableInstance> variableList = new ArrayList<>();
        for (VariableInstance variableInstance : getCellInstance().getOutputVariableMap().values()) {
            if (variableInstance.getVariableDefinition().isEditable()) {
                variableList.add(variableInstance);
            }
        }
        variableListView = new ListView<VariableInstance>("variable", variableList) {

            @Override
            protected void populateItem(ListItem<VariableInstance> listItem) {
                addVariableEditor(listItem);
            }
        };
        form.add(variableListView);

        List<OptionInstance> optionList = new ArrayList<>();
        for (OptionInstance optionInstance : getCellInstance().getOptionMap().values()) {
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
        storeVariables();
        storeOptions();
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellInstance().getId());
        fireContentChanged();
    }

    private void storeVariables() {
        for (String name : variableEditorModelMap.keySet()) {
            FieldEditorModel editorModel = variableEditorModelMap.get(name);
            VariableInstance variableInstance = getCellInstance().getOutputVariableMap().get(name);
            variableInstance.setValue(editorModel.getValue());
        }
    }

    private void storeOptions() {
        for (String name : optionEditorModelMap.keySet()) {
            FieldEditorModel editorModel = optionEditorModelMap.get(name);
            OptionInstance optionInstance = getCellInstance().getOptionMap().get(name);
            optionInstance.setValue(editorModel.getValue());
        }
    }

    private void addVariableEditor(ListItem<VariableInstance> listItem) {
        VariableInstance variableInstance = listItem.getModelObject();
        FieldEditorPanel fieldEditorPanel = createVariableEditor(variableInstance);
        variableEditorModelMap.put(variableInstance.getName(), fieldEditorPanel.getFieldEditorModel());
        listItem.add(fieldEditorPanel);
    }

    private FieldEditorPanel createVariableEditor(VariableInstance variableInstance) {
        if (variableInstance.getVariableDefinition().getVariableType().equals(VariableType.STRING)) {
            return new StringFieldEditorPanel("variableEditor", new FieldEditorModel(variableInstance.getValue(), variableInstance.getDisplayName()));
        } else if (variableInstance.getVariableDefinition().getVariableType().equals(VariableType.INTEGER)) {
            return new IntegerFieldEditorPanel("variableEditor", new FieldEditorModel(variableInstance.getValue(), variableInstance.getDisplayName()));
        } else if (variableInstance.getVariableDefinition().getVariableType().equals(VariableType.FLOAT)) {
            return new FloatFieldEditorPanel("variableEditor", new FieldEditorModel(variableInstance.getValue(), variableInstance.getDisplayName()));
        } else {
            return new DummyFieldEditorPanel("variableEditor", new FieldEditorModel(variableInstance.getValue(), variableInstance.getDisplayName()));
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
        if (OptionType.SIMPLE.equals(optionDefinition.getOptionType())) {
            if (optionDefinition.getTypeDescriptor().getType() == Structure.class) {
                return new StructureFieldEditorPanel("optionEditor", "canvasMarvinEditor", new FieldEditorModel(optionInstance.getValue(), optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == String.class) {
                return new StringFieldEditorPanel("optionEditor", new FieldEditorModel(optionInstance.getValue(), optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == Integer.class) {
                return new IntegerFieldEditorPanel("optionEditor", new FieldEditorModel(optionInstance.getValue(), optionDefinition.getDisplayName()));
            } else if (optionDefinition.getTypeDescriptor().getType() == Float.class) {
                return new FloatFieldEditorPanel("optionEditor", new FieldEditorModel(optionInstance.getValue(), optionDefinition.getDisplayName()));
            } else {
                return new DummyFieldEditorPanel("optionEditor", new FieldEditorModel(optionInstance.getValue(), optionDefinition.getDisplayName()));
            }
        } else {
            return new DummyFieldEditorPanel("optionEditor", new FieldEditorModel(optionInstance.getValue(), optionDefinition.getDisplayName()));
        }
    }


}
