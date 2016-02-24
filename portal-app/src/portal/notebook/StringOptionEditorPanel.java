package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import portal.notebook.api.OptionInstance;

/**
 * @author simetrias
 */
public class StringOptionEditorPanel extends  OptionEditorPanel {

    private Model<String> model;

    public StringOptionEditorPanel(String id, OptionInstance optionInstance) {
        super(id, optionInstance);
        addComponents();
    }

    private void addComponents() {
        model = new Model<>();
        model.setObject((String) getOptionInstance().getValue());
        add(new Label("label", getOptionInstance().getOptionDescriptor().getDisplayName()));
        TextField<String> stringTextField = new TextField<>("value", model);
        add(stringTextField);
    }

    public void storeModel() {
        getOptionInstance().setValue(model.getObject());
    }

}
