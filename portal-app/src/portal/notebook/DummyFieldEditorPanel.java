package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;

public class DummyFieldEditorPanel extends FieldEditorPanel {

    public DummyFieldEditorPanel(String id, FieldEditorModel editorModel) {
        super(id, editorModel); addComponents();
    }

    private void addComponents() {
        add(new Label("label", getFieldEditorModel().getDisplayName()));
    }
}
