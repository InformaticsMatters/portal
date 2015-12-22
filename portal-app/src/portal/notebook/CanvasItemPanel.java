
package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;

public abstract class CanvasItemPanel extends Panel implements CellTitleBarPanel.CallbackHandler {

    private final CellModel cellModel;
    @Inject
    private NotebookSession notebookSession;

    public CanvasItemPanel(String id, CellModel cellModel) {
        super(id);
        this.cellModel = cellModel;
    }

    protected void addTitleBar() {
        CellTitleBarPanel cellTitleBarPanel = new CellTitleBarPanel("titleBar", getCellModel(), this);
        add(cellTitleBarPanel);
    }

    public CellModel getCellModel() {
        return cellModel;
    }

    public void fireContentChanged() {
        notebookSession.reloadCurrentNotebook();
        getRequestCycle().find(AjaxRequestTarget.class).add(getPage());
    }

    @Override
    public void onRemove(CellModel cellModel) {
        notebookSession.removeCell(cellModel);
        fireContentChanged();
    }

    @Override
    public void onEditBindings(CellModel cellModel) {
        /*
        connectionPanel.configure(null, cellModel);
        connectionPanel.setCanAddBindings(false);
        connectionPanel.showModal();
        */
    }
}
