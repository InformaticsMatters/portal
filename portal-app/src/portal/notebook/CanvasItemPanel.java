
package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import portal.PopupContainerProvider;

import javax.inject.Inject;

public abstract class CanvasItemPanel extends Panel implements CellTitleBarPanel.CallbackHandler {

    private CellModel cellModel;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    private BindingsModalPanel bindingsModalPanel;

    public CanvasItemPanel(String id, CellModel cellModel) {
        super(id);
        this.cellModel = cellModel;
        addBindingsPanel();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        String js = "initCellSizeAndPosition(':id', :top, :left, :width, :height)";
        CellModel model = getCellModel();
        js = js.replace(":id", getMarkupId());
        js = js.replace(":top", Integer.toString(model.getPositionTop()));
        js = js.replace(":left", Integer.toString(model.getPositionLeft()));
        js = js.replace(":width", Integer.toString(model.getSizeWidth()));
        js = js.replace(":height", Integer.toString(model.getSizeHeight()));
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(js));
    }

    private void addBindingsPanel() {
        bindingsModalPanel = new BindingsModalPanel("content", "modalElement");
        bindingsModalPanel.setCallbacks(new BindingsModalPanel.Callbacks() {

            @Override
            public void onSubmit() {
                notebookSession.storeCurrentNotebook();
                if (bindingsModalPanel.getSourceCellModel() != null) {
                    AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
                    ajaxRequestTarget.add(getPage());
                    String sourceMarkupId = NotebookCanvasPage.CANVAS_ITEM_PREFIX + bindingsModalPanel.getSourceCellModel().getId();
                    String targetMarkupId = NotebookCanvasPage.CANVAS_ITEM_PREFIX + bindingsModalPanel.getTargetCellModel().getId();
                    String js = "addConnection('" + sourceMarkupId + "', '" + targetMarkupId + "');";
                    ajaxRequestTarget.appendJavaScript(js);
                }
            }

            @Override
            public void onClose() {
                if (bindingsModalPanel.isDirty()) {
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

    protected void makeCanvasItemResizable(HtmlHeaderContainer container, String fitCallbackFunction, int minWidth, int minHeight) {
        String js = "makeCanvasItemResizable(:minWidth, :minHeight, ':id', :fitCallback)";
        js = js.replace(":id", getMarkupId()).replace(":minWidth", Integer.toString(minWidth)).replace(":minHeight", Integer.toString(minHeight));
        js = js.replace(":fitCallback", "function(id) {" + fitCallbackFunction + "(id)}");
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(js));
    }

    public void updateCellModel() {
        this.cellModel = notebookSession.getCurrentNotebookModel().findCellModel(cellModel.getName());
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
        bindingsModalPanel.configure(sourceCellModel, targetCellModel, canAddBindings);
        popupContainerProvider.setPopupContentForPage(getPage(), bindingsModalPanel);
        popupContainerProvider.refreshContainer(getPage(), getRequestCycle().find(AjaxRequestTarget.class));
        bindingsModalPanel.showModal();
    }
}
