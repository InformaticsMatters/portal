package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;

public abstract class CanvasItemPanel extends Panel {

    private final CellModel cellModel;
    @Inject
    private NotebookSession notebookSession;

    public CanvasItemPanel(String id, CellModel cellModel) {
        super(id);
        this.cellModel = cellModel;
    }

    public CellModel getCellModel() {
        return cellModel;
    }

    public void fireContentChanged() {
        notebookSession.reloadCurrentNotebook();
        getRequestCycle().find(AjaxRequestTarget.class).add(getPage());
    }
}
