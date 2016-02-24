package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.OptionInstance;


public abstract class FieldEditorPanel extends Panel {

    private final FieldEditorModel fieldEditorModel;

    public FieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id);
        this.fieldEditorModel = fieldEditorModel;
    }

    public FieldEditorModel getFieldEditorModel() {
        return fieldEditorModel;
    }

}
