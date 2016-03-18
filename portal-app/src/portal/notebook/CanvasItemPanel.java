
package portal.notebook;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.PopupContainerProvider;
import portal.notebook.api.CellInstance;
import portal.notebook.service.Execution;

import javax.inject.Inject;

public abstract class CanvasItemPanel extends Panel implements CellTitleBarPanel.CallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(CanvasItemPanel.class);
    private final Long cellId;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    @Inject
    private ExecutionStatusChangeManager executionStatusChangeManager;
    private CellTitleBarPanel cellTitleBarPanel;
    private Execution oldExecution;

    public CanvasItemPanel(String id, Long cellId) {
        super(id);
        this.cellId = cellId;
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        String js = "initCellSizeAndPosition(':id', :left, :top, :width, :height)";
        CellInstance model = findCellInstance();
        js = js.replace(":id", getMarkupId());
        js = js.replace(":left", Integer.toString(model.getPositionLeft()));
        js = js.replace(":top", Integer.toString(model.getPositionTop()));
        js = js.replace(":width", Integer.toString(model.getSizeWidth()));
        js = js.replace(":height", Integer.toString(model.getSizeHeight()));

        logger.info(js);

        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(js));
    }

    protected void addTitleBar() {
        cellTitleBarPanel = new CellTitleBarPanel("titleBar", findCellInstance(), this);
        add(cellTitleBarPanel);
    }

    protected void makeCanvasItemResizable(HtmlHeaderContainer container, String fitCallbackFunction, int minWidth, int minHeight) {
        String js = "makeCanvasItemResizable(:minWidth, :minHeight, ':id', :fitCallback)";
        js = js.replace(":id", getMarkupId()).replace(":minWidth", Integer.toString(minWidth)).replace(":minHeight", Integer.toString(minHeight));
        js = js.replace(":fitCallback", "function(id) {" + fitCallbackFunction + "(id)}");
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(js));
    }

    public CellInstance findCellInstance() {
        return notebookSession.getCurrentNotebookInstance().findCellById(cellId);
    }

    public void fireContentChanged() {
        notebookSession.reloadCurrentNotebook();
        getRequestCycle().find(AjaxRequestTarget.class).add(getPage());
    }

    @Override
    public void onRemove(CellInstance cellInstance) {
        notebookSession.getCurrentNotebookInstance().removeCell(cellInstance.getId());
        notebookSession.storeCurrentNotebook();
        fireContentChanged();
    }

    @Override
    public void onEditBindings(CellInstance cellModel) {
        editBindings();
    }

    public void editBindings() {
        popupContainerProvider.refreshContainer(getPage(), getRequestCycle().find(AjaxRequestTarget.class));
    }

    public void addExecutionStatusTimerBehavior() {
        add(new AbstractAjaxTimerBehavior(Duration.seconds(2)) {

            @Override
            protected void onTimer(AjaxRequestTarget ajaxRequestTarget) {
                refreshExecutionStatus(ajaxRequestTarget);
            }
        });
    }

    protected void refreshExecutionStatus(AjaxRequestTarget ajaxRequestTarget) {
        Execution lastExecution = notebookSession.findExecution(findCellInstance().getId());
        boolean changed = executionChanged(lastExecution);
        if (changed) {
            cellTitleBarPanel.applyExecutionStatus(lastExecution);
            ajaxRequestTarget.add(cellTitleBarPanel);
            executionStatusChangeManager.notifyExecutionStatusChanged(findCellInstance().getId(), ajaxRequestTarget);
        }
        oldExecution = lastExecution;
    }

    private boolean executionChanged(Execution lastExecution) {
        if (oldExecution == null) {
            return lastExecution != null;
        } else if (lastExecution == null) {
            return true;
        } else if (oldExecution.getJobId().equals(lastExecution.getJobId())) {
            return !oldExecution.getJobActive().equals(lastExecution.getJobActive())
            || !oldExecution.getJobStatus().equals(lastExecution.getJobStatus());
        } else if (!oldExecution.getJobId().equals(lastExecution.getJobId())) {
            return true;
        } else {
            return false;
        }
    }

    public abstract void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget);

    public Long getCellId() {
        return cellId;
    }
}
