package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;

public class DummyFieldEditorPanel extends FieldEditorPanel {

    public DummyFieldEditorPanel(String id, FieldEditorModel editorModel) {
        super(id, editorModel); addComponents();
    }

    @Override
    public void enableEditor(boolean editable) {

    }

    private void addComponents() {
        add(new Label("label", getFieldEditorModel().getDisplayName()));
    }
}
