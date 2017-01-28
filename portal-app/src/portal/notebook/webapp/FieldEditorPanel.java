package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;


public abstract class FieldEditorPanel<T> extends Panel {

    private final FieldEditorModel<T> fieldEditorModel;

    public FieldEditorPanel(String id, FieldEditorModel<T> fieldEditorModel) {
        super(id);
        this.fieldEditorModel = fieldEditorModel;
        setOutputMarkupId(true);
    }

    public FieldEditorModel<T> getFieldEditorModel() {
        return fieldEditorModel;
    }

    public boolean processCellChanged(Long cellId, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        return false;
    }

    public abstract void enableEditor(boolean editable);
}
