package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import portal.notebook.api.OptionInstance;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class StringFieldEditorPanel extends FieldEditorPanel {

    private Model<String> model;

    public StringFieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id, fieldEditorModel);
        addComponents();
    }

    private void addComponents() {
        model = new Model<String>(){
            @Override
            public String getObject() {
                return (String)getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        TextField<String> stringTextField = new TextField<>("value", model);
        add(stringTextField);
    }

}
