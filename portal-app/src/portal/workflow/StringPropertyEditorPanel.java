package portal.workflow;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.squonk.options.OptionDescriptor;

/**
 * @author simetrias
 */
public class StringPropertyEditorPanel extends Panel {

    public StringPropertyEditorPanel(String id, OptionDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        super(id);
        addComponents(servicePropertyDescriptor, servicePropertyModel);
    }

    private void addComponents(OptionDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        add(new Label("label", servicePropertyDescriptor.getLabel()));
        TextField<String> stringTextField = new TextField<>("value", servicePropertyModel);
        add(stringTextField);
    }
}
