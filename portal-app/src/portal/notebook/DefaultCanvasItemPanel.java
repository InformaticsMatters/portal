package portal.notebook;

import org.apache.wicket.Component;
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

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class DefaultCanvasItemPanel extends CanvasItemPanel {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCanvasItemPanel.class.getName());
    private Map<String, OptionFieldEditorModel> optionEditorModelMap;
    private Map<String, VariableFieldEditorModel> variableEditorModelMap;
    private Form form;
    @Inject
    private NotebookSession notebookSession;
    private ListView<OptionInstance> optionListView;
    private ListView<VariableInstance> variableListView;

    public DefaultCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        optionEditorModelMap = new HashMap<>();
        variableEditorModelMap = new HashMap<>();
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
        logger.info("Executing service cell " + getCellInstance().getCellDefinition().getDescription());
        try {
            execute();
            getRequestCycle().find(AjaxRequestTarget.class).add(DefaultCanvasItemPanel.this.form);
        } catch (Throwable t) {
            logger.error("Failed to execute service cell", t);
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
        for (VariableFieldEditorModel variableEditorModel : variableEditorModelMap.values()) {
            VariableInstance variableInstance = getCellInstance().getOutputVariableMap().get(variableEditorModel.getVariableInstance().getName());
            variableInstance.setValue(variableEditorModel.getValue());
        }
    }

    private void storeOptions() {
        for (OptionFieldEditorModel optionFieldEditorModel : optionEditorModelMap.values()) {
            OptionInstance optionInstance = getCellInstance().getOptionMap().get(optionFieldEditorModel.getOptionInstance().getOptionDescriptor().getName());
            optionInstance.setValue(optionFieldEditorModel.getValue());
        }
    }

    private void addVariableEditor(ListItem<VariableInstance> listItem) {
        VariableInstance variableInstance = listItem.getModelObject();
        FieldEditorPanel fieldEditorPanel = createVariableEditor(variableInstance);
        listItem.add((Component) fieldEditorPanel);
    }

    private FieldEditorPanel createVariableEditor(VariableInstance variableInstance) {
        VariableFieldEditorModel editorModel = new VariableFieldEditorModel(variableInstance);
        variableEditorModelMap.put(variableInstance.getName(), editorModel);
        return new DummyFieldEditorPanel("variable", editorModel);
    }


    private void addOptionEditor(ListItem<OptionInstance> listItem) {
        OptionInstance optionInstance = listItem.getModelObject();
        FieldEditorPanel fieldEditorPanel = createOptionEditor(optionInstance);
        listItem.add(fieldEditorPanel);
    }

    private FieldEditorPanel createOptionEditor(OptionInstance optionInstance) {
        OptionDescriptor optionDefinition = optionInstance.getOptionDescriptor();
        OptionFieldEditorModel optionFieldEditorModel = new OptionFieldEditorModel(optionInstance);
        optionEditorModelMap.put(optionInstance.getOptionDescriptor().getName(), optionFieldEditorModel);
        if (OptionType.SIMPLE.equals(optionDefinition.getOptionType())) {
            if (optionDefinition.getTypeDescriptor().getType() == String.class) {
                return new StringFieldEditorPanel("optionEditor", optionFieldEditorModel);
            } else if (optionDefinition.getTypeDescriptor().getType() == Structure.class) {
                return new StructureFieldEditorPanel("optionEditor", "canvasMarvinEditor", optionFieldEditorModel);
            } else {
                return new StringFieldEditorPanel("optionEditor", optionFieldEditorModel); // for now
            }
        } else {
            return new StringFieldEditorPanel("optionEditor", optionFieldEditorModel); // for now
        }
    }


}
