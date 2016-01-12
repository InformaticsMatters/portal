package portal.notebook;

import org.apache.wicket.markup.html.form.Form;
import org.squonk.notebook.api.OptionDefinition;

import java.util.List;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends CanvasItemPanel {

    private Form form;

    public ServiceCanvasItemPanel(String id, CellModel cellModel) {
        super(id, cellModel);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
    }

    private void addForm() {
        List<OptionDefinition> options = getCellModel().getCellType().getOptionDefinitionList();

        for (OptionDefinition optionDefinition : options) {
            System.out.println("Option definition: " + optionDefinition.getDisplayName());
        }

        form = new Form("form");
        add(form);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {


    }
}
