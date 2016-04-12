package portal.notebook.webapp;

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
    private CellChangeManager cellChangeManager;
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
        js = js.replace(":left", Integer.toString(model.getPositionLeft() == null ? 1 : model.getPositionLeft()));
        js = js.replace(":top", Integer.toString(model.getPositionTop() == null ? 1 : model.getPositionTop()));
        js = js.replace(":width", Integer.toString(model.getSizeWidth() == null ? 265 : model.getSizeWidth()));
        js = js.replace(":height", Integer.toString(model.getSizeHeight() == null ? 200 : model.getSizeHeight()));

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
        return notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
    }

    public void fireContentChanged() {
        notebookSession.reloadCurrentNotebook();
        getRequestCycle().find(AjaxRequestTarget.class).add(getPage());
    }

    @Override
    public void onRemove(CellInstance cellInstance) {
        notebookSession.getCurrentNotebookInstance().removeCellInstance(cellInstance.getId());
        notebookSession.storeCurrentNotebook();
        fireContentChanged();
    }

    public void addExecutionStatusTimerBehavior() {
        add(new AbstractAjaxTimerBehavior(Duration.seconds(2)) {

            @Override
            public CharSequence getCallbackUrl() {
                return super.getCallbackUrl() + "&executionStatusTimer=true";
            }

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
            cellChangeManager.notifyExecutionStatusChanged(findCellInstance().getId(), lastExecution.getJobStatus(), ajaxRequestTarget);
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