package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.squonk.options.OptionDescriptor;

/**
 * @author simetrias
 */
public class StringOptionEditorPanel extends Panel {

    public StringOptionEditorPanel(String id, OptionDescriptor optionDefinition, IModel<String> optionModel) {
        super(id);
        addComponents(optionDefinition, optionModel);
    }

    private void addComponents(OptionDescriptor optionDefinition, IModel<String> servicePropertyModel) {
        add(new Label("label", optionDefinition.getDisplayName()));
        TextField<String> stringTextField = new TextField<>("value", servicePropertyModel);
        add(stringTextField);
    }
}
