package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class FieldEditorPanel extends Panel {

    private final FieldEditorModel fieldEditorModel;

    public FieldEditorPanel(String id, FieldEditorModel fieldEditorModel) {
        super(id);
        this.fieldEditorModel = fieldEditorModel;
        setOutputMarkupId(true);
    }

    public FieldEditorModel getFieldEditorModel() {
        return fieldEditorModel;
    }

    public boolean processCellChanged(Long cellId, AjaxRequestTarget ajaxRequestTarget) {
        return false;
    }
}
