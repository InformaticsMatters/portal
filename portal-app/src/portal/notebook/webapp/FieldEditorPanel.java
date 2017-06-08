package portal.notebook.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;


public abstract class FieldEditorPanel<T> extends Panel {

    @Inject
    private NotifierProvider notifierProvider;

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

    protected void notify(String title, String message) {
        Page page = getPage();
        if (page != null) {
            notifierProvider.getNotifier(page).notify(title, message);
        }
    }
}
