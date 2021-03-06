package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;

public class MultilineFieldEditorPanel extends FieldEditorPanel<String> {

    private TextArea<String> textArea;

    public MultilineFieldEditorPanel(String id, FieldEditorModel<String> fieldEditorModel) {
        super(id, fieldEditorModel);
        addComponents();
    }


    private void addComponents() {
        Model<String> model = new Model<String>() {
            @Override
            public String getObject() {
                return (String) getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        textArea = new TextArea<String>("value", model);
        add(textArea);
    }

    @Override
    public void enableEditor(boolean editable) {
        textArea.setEnabled(editable);
    }

}
