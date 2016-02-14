package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squonk.notebook.api.OptionType;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.types.Structure;
import portal.notebook.cells.ServiceCellDefinition;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends CanvasItemPanel {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCanvasItemPanel.class.getName());

    private Form form;
    private Map<OptionDescriptor, String> optionValueMap;
    @Inject
    private NotebookSession notebookSession;

    public ServiceCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
    }

    private void addForm() {
        form = new Form("form");
        add(form);

        List<OptionDescriptor> optionDefinitionList = getCellInstance().getCellDefinition().getOptionDefinitionList();
        optionValueMap = new HashMap<>();
        for (OptionDescriptor optionDefinition : optionDefinitionList) {
            optionValueMap.put(optionDefinition, null);
        }

        ListView<OptionDescriptor> listView = new ListView<OptionDescriptor>("option", optionDefinitionList) {

            @Override
            protected void populateItem(ListItem<OptionDescriptor> listItem) {
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
        //form.getModelObject().store();
        notebookSession.executeCell(getCellInstance().getId());
        fireContentChanged();
    }


    private void addOptionEditor(ListItem<OptionDescriptor> listItem) {
        OptionDescriptor optionDefinition = listItem.getModelObject();
        OptionModel optionModel = new OptionModel(optionDefinition);
        if (OptionType.SIMPLE.equals(optionDefinition.getOptionType())) {
            if (optionDefinition.getTypeDescriptor().getType() == String.class) {
                listItem.add(new StringOptionEditorPanel("editor", optionDefinition, optionModel));
            } else if (optionDefinition.getTypeDescriptor().getType() == Structure.class) {
                listItem.add(new StructureOptionEditorPanel("editor", "canvasMarvinEditor", optionDefinition, optionModel));
            } else {
                listItem.add(new StringOptionEditorPanel("editor", optionDefinition, optionModel)); // for now
            }
        } else {
            listItem.add(new StringOptionEditorPanel("editor", optionDefinition, optionModel)); // for now
        }
    }

    private class OptionModel implements IModel<String> {

        private final OptionDescriptor optionDefinition;

        public OptionModel(OptionDescriptor optionDefinition) {
            this.optionDefinition = optionDefinition;
        }

        @Override
        public String getObject() {
            return optionValueMap.get(optionDefinition);
        }

        @Override
        public void setObject(String s) {
            optionValueMap.put(optionDefinition, s);
        }

        @Override
        public void detach() {
        }
    }
}
