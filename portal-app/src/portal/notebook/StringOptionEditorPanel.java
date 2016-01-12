package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.squonk.notebook.api.OptionDefinition;

/**
 * @author simetrias
 */
public class StringOptionEditorPanel extends Panel {

    public StringOptionEditorPanel(String id, OptionDefinition optionDefinition, IModel<String> optionModel) {
        super(id);
        addComponents(optionDefinition, optionModel);
    }

    private void addComponents(OptionDefinition optionDefinition, IModel<String> servicePropertyModel) {
        add(new Label("label", optionDefinition.getDisplayName()));
        TextField<String> stringTextField = new TextField<>("value", servicePropertyModel);
        add(stringTextField);
    }
}
