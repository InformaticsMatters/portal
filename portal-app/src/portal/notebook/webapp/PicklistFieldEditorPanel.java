package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;

import java.util.List;

public class PicklistFieldEditorPanel  extends FieldEditorPanel {

    private final List picklistItems;
    private DropDownChoice picklistChoice;

    public PicklistFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, List picklistItems) {
        super(id, fieldEditorModel);
        this.picklistItems = picklistItems;
        addComponents();
    }

    private void addComponents() {
        Model model = new Model<String>() {
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
        picklistChoice = new DropDownChoice("picklist", model, picklistItems);
        add(picklistChoice);
    }

    @Override
    public void enableEditor(boolean editable) {
        picklistChoice.setEnabled(editable);
    }
}
