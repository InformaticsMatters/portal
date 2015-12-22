
package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import portal.PopupContainerProvider;

import javax.inject.Inject;

public abstract class CanvasItemPanel extends Panel implements CellTitleBarPanel.CallbackHandler {

    private final CellModel cellModel;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    private ConnectionPanel connectionPanel;

    public CanvasItemPanel(String id, CellModel cellModel) {
        super(id);
        this.cellModel = cellModel;
        addBindingsPanel();
    }

    private void addBindingsPanel() {
        connectionPanel = new ConnectionPanel("content", "modalElement");
        connectionPanel.setCallbacks(new ConnectionPanel.Callbacks() {

            @Override
            public void onSubmit() {
                notebookSession.storeCurrentNotebook();
                if (connectionPanel.getSourceCellModel() != null) {
                    AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
                    ajaxRequestTarget.add(getPage());
                    String sourceMarkupId = NotebookCanvasPage.CANVAS_ITEM_PREFIX + connectionPanel.getSourceCellModel().getId();
                    String targetMarkupId = NotebookCanvasPage.CANVAS_ITEM_PREFIX + connectionPanel.getTargetCellModel().getId();
                    String js = "addConnection('" + sourceMarkupId + "', '" + targetMarkupId + "');";
                    ajaxRequestTarget.appendJavaScript(js);
                }
            }

            @Override
            public void onClose() {
                if (connectionPanel.isDirty()) {
                    notebookSession.reloadCurrentNotebook();
                    RequestCycle.get().find(AjaxRequestTarget.class).add(getPage());
                }
            }
        });
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
        editBindings(null, getCellModel(), false);
    }

    public void editBindings(CellModel sourceCellModel, CellModel targetCellModel, boolean canAddBindings) {
        connectionPanel.configure(sourceCellModel, targetCellModel, canAddBindings);
        popupContainerProvider.setPopupContentForPage(getPage(), connectionPanel);
        popupContainerProvider.refreshContainer(getPage(), getRequestCycle().find(AjaxRequestTarget.class));
        connectionPanel.showModal();
    }
}
