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
import portal.notebook.cells.ServiceCellDefinition;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends CanvasItemPanel {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCanvasItemPanel.class.getName());
    private Map<String, OptionEditorPanel> editorMap;
    private Form form;
    @Inject
    private NotebookSession notebookSession;
    private ListView<OptionInstance> listView;

    public ServiceCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        editorMap = new HashMap<>();
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
    }

    private void addForm() {
        form = new Form("form");
        add(form);

        List<OptionInstance> optionList = new ArrayList<>(getCellInstance().getOptionMap().values());
        listView = new ListView<OptionInstance>("option", optionList) {

            @Override
            protected void populateItem(ListItem<OptionInstance> listItem) {
                addOptionEditor(listItem);
            }
        };
        form.add(listView);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        ServiceCellDefinition cellDefinition = (ServiceCellDefinition) getCellInstance().getCellDefinition();
        logger.info("Executing service cell " + cellDefinition.getDescription());
        try {
            execute();
            getRequestCycle().find(AjaxRequestTarget.class).add(ServiceCanvasItemPanel.this.form);
        } catch (Throwable t) {
            logger.error("Failed to execute service cell", t);
        }
    }

    private void execute() throws IOException {
        storeModel();
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellInstance().getId());
        fireContentChanged();
    }

    private void storeModel() {
        for (OptionEditorPanel optionEditorPanel : editorMap.values()) {
            optionEditorPanel.storeModel();
        }
    }


    private void addOptionEditor(ListItem<OptionInstance> listItem) {
        OptionInstance optionInstance = listItem.getModelObject();
        OptionEditorPanel optionEditorPanel = createOptionEditor(optionInstance);
        listItem.add((Component) optionEditorPanel);
        editorMap.put(optionInstance.getOptionDescriptor().getName(), optionEditorPanel);
    }

    private OptionEditorPanel createOptionEditor(OptionInstance optionInstance) {
        OptionDescriptor optionDefinition = optionInstance.getOptionDescriptor();
        if (OptionType.SIMPLE.equals(optionDefinition.getOptionType())) {
            if (optionDefinition.getTypeDescriptor().getType() == String.class) {
                return new StringOptionEditorPanel("editor", optionInstance);
            } else if (optionDefinition.getTypeDescriptor().getType() == Structure.class) {
                return new StructureOptionEditorPanel("editor", "canvasMarvinEditor", optionInstance);
            } else {
                return new StringOptionEditorPanel("editor", optionInstance); // for now
            }
        } else {
            return new StringOptionEditorPanel("editor", optionInstance); // for now
        }
    }

}
