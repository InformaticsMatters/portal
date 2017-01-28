package portal.notebook.webapp;

import org.apache.wicket.markup.html.basic.Label;

public class DummyFieldEditorPanel extends FieldEditorPanel<String> {

    public DummyFieldEditorPanel(String id, FieldEditorModel<String> editorModel) {
        super(id, editorModel); addComponents();
    }

    @Override
    public void enableEditor(boolean editable) {

    }

    private void addComponents() {
        add(new Label("label", getFieldEditorModel().getDisplayName()));
    }
}
