package portal.notebook;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.squonk.notebook.api.OptionDefinition;
import org.squonk.notebook.api.OptionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends CanvasItemPanel {

    private Form form;
    private Map<OptionDefinition, String> optionValueMap;

    public ServiceCanvasItemPanel(String id, CellModel cellModel) {
        super(id, cellModel);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
    }

    private void addForm() {
        form = new Form("form");
        add(form);

        List<OptionDefinition> optionDefinitionList = getCellModel().getCellType().getOptionDefinitionList();
        optionValueMap = new HashMap<>();
        for (OptionDefinition optionDefinition : optionDefinitionList) {
            optionValueMap.put(optionDefinition, null);
        }

        ListView<OptionDefinition> listView = new ListView<OptionDefinition>("option", optionDefinitionList) {

            @Override
            protected void populateItem(ListItem<OptionDefinition> listItem) {
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
    }

    private void addOptionEditor(ListItem<OptionDefinition> listItem) {
        OptionDefinition optionDefinition = listItem.getModelObject();
        OptionModel optionModel = new OptionModel(optionDefinition);
        if (OptionType.SIMPLE == optionDefinition.getOptionType()) {
            listItem.add(new StringOptionEditorPanel("editor", optionDefinition, optionModel));
        }
    }

    private class OptionModel implements IModel<String> {

        private final OptionDefinition optionDefinition;

        public OptionModel(OptionDefinition optionDefinition) {
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
