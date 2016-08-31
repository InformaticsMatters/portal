package portal.notebook.webapp;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.PopupContainerProvider;
import portal.notebook.api.*;
import portal.notebook.service.Execution;
import portal.notebook.webapp.results.DatasetResultsHandler;
import portal.notebook.webapp.results.ResultsHandler;
import portal.notebook.webapp.results.ResultsViewerPanel;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticModalPanel;

import javax.inject.Inject;
import java.util.List;

public abstract class CanvasItemPanel extends Panel implements CellTitleBarPanel.CallbackHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanvasItemPanel.class);
    private final Long cellId;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    @Inject
    private CellChangeManager cellChangeManager;
    private CellTitleBarPanel cellTitleBarPanel;
    private Execution oldExecution;
    @Inject
    private NotifierProvider notifierProvider;
    private Label statusLabel;
    private CellStatusInfo cellStatusInfo;

    private SemanticModalPanel resultsPanel;
    protected ResultsHandler resultsHandler;

    public CanvasItemPanel(String id, Long cellId) {
        super(id);
        this.cellId = cellId;

        createResultsHandlers();

        try {
            updateStatusInfo();
        } catch (Throwable t) {
            LOGGER.warn("Error refreshing status", t);
        }
    }

    protected void createResultsHandlers() {
        CellInstance cellInstance = findCellInstance();
        CellDefinition cellDefinition = cellInstance.getCellDefinition();
        List<VariableDefinition> varDefs = cellDefinition.getVariableDefinitionList();
        // TODO - handle multiple outputs (resultsHandler should become an array and results viewer should handle multiple types)
        // TODO - handle other result types
        if (varDefs != null && varDefs.size() > 0) {
            for (VariableDefinition varDef : varDefs) {
                String name = varDef.getName();
                VariableType type = varDef.getVariableType();
                if (type == VariableType.DATASET) {
                    resultsHandler = new DatasetResultsHandler(name, notebookSession, cellInstance);
                    return;
                }
            }
        }
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
        js = js.replace(":height", Integer.toString(model.getSizeHeight() == null ? 0 : model.getSizeHeight()));

        LOGGER.info(js);

        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(js));
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

    public void fireContentChanged() throws Exception {
        notebookSession.reloadCurrentVersion();
        getRequestCycle().find(AjaxRequestTarget.class).add(getPage());
    }

    @Override
    public void onRemove(CellInstance cellInstance) {
        try {
            notebookSession.getCurrentNotebookInstance().removeCellInstance(cellInstance.getId());
            notebookSession.storeCurrentEditable();
            fireContentChanged();
        } catch (Throwable t) {
            LOGGER.warn("Error removing cell", t);
            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
        }
    }

    public void addExecutionStatusTimerBehavior() {
        add(new AbstractAjaxTimerBehavior(Duration.seconds(2)) {

            @Override
            public CharSequence getCallbackUrl() {
                return super.getCallbackUrl() + "&executionStatusTimer=true";
            }

            @Override
            protected void onTimer(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    Execution lastExecution = notebookSession.findExecution(findCellInstance().getId());
                    boolean executionChanged = executionChanged(lastExecution);
                    if (executionChanged) {
                        notifyExecutionChanged(ajaxRequestTarget, lastExecution);
                        notifyCellStatus(ajaxRequestTarget);
                    }
                    oldExecution = lastExecution;
                } catch (Throwable t) {
                    LOGGER.warn("Error refreshing status", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });
    }

    private void notifyExecutionChanged(AjaxRequestTarget ajaxRequestTarget, Execution execution) {
        cellTitleBarPanel.applyExecutionStatus(execution);
        ajaxRequestTarget.add(cellTitleBarPanel);
        CellInstance cell = findCellInstance();
        if (cell != null) {
            cellChangeManager.notifyExecutionStatusChanged(cell.getId(), execution.getJobStatus(), ajaxRequestTarget);
        } else {
            LOGGER.warn("Could not find cell" + cellId);
        }
    }

    private void notifyCellStatus(AjaxRequestTarget ajaxRequestTarget) {
        updateStatusInfo();
        cellStatusChanged(cellStatusInfo, ajaxRequestTarget);
    }

    protected void updateStatusInfo() {
        Execution execution = notebookSession.findExecution(findCellInstance().getId());
        CellInstance cellInstance = findCellInstance();
        cellStatusInfo = new CellStatusInfo();
        cellStatusInfo.setBindingsComplete(bindingsComplete(cellInstance));
        if (execution == null) {
            cellStatusInfo.setRunning(Boolean.FALSE);
        } else {
            cellStatusInfo.setRunning(execution.getJobActive());
            cellStatusInfo.setSucceed(execution.getJobSuccessful());
            cellStatusInfo.setMessage(execution.getLastEventMessage());
        }
    }


    private Boolean bindingsComplete(CellInstance cellInstance) {
        for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
            if (bindingInstance.getVariableInstance() == null) {
                return false;
            }
        }
        return true;
    }

    protected void cellStatusChanged(CellStatusInfo cellStatusInfo, AjaxRequestTarget ajaxRequestTarget) {
        if (statusLabel != null) {
            ajaxRequestTarget.add(statusLabel);
        }
    }

    public void processCellChanged(Long changedCellId, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        if (changedCellId.equals(getCellId())) {
            notifyCellStatus(ajaxRequestTarget);
        }
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

    public Long getCellId() {
        return cellId;
    }

    protected Label createStatusLabel(String id) {
        statusLabel = new Label(id, new PropertyModel<String>(this, "statusString"));
        statusLabel.setOutputMarkupId(true);
        return statusLabel;
    }

    public String getStatusString() {
        if (cellStatusInfo == null) {
            return "";
        } else if (cellStatusInfo.getMessage() == null) {
            return cellStatusInfo.toString() + ".";
        } else {
            return cellStatusInfo.toString() + ": " + cellStatusInfo.getMessage();
        }
    }

    @Override
    public void onShowResults() throws Exception {
        if (resultsHandler != null) {
            boolean hasResults = resultsHandler.preparePanelForDisplay();
            if (hasResults) {
                resultsPanel.showModal(resultsHandler.getExtraJavascriptForResultsViewer());
                return;
            }
        }
        NotebookCanvasPage page = (NotebookCanvasPage) getPage();
        page.getNoResultsPanel().showModal();
    }

    /**
     * Adds a title bar as the Wicket ID titleBar.
     * This method is defined in this base class but must be called at the right moment from a subclass
     * if you want a title bar to appear which you probably do.
     */
    protected void addTitleBar() {
        cellTitleBarPanel = new CellTitleBarPanel("titleBar", findCellInstance(), this);
        add(cellTitleBarPanel);
    }

    /**
     * Adds a results viewer as the Wicket ID resultsViewer.
     * This appears in the modal popup when the expand button in the title bar is clicked.
     * This method is defined in this base class but must be called at the right moment from a subclass
     * for cells that have output variables that have a supported viewer.
     * Which results viewer(s) appear depends on the resultHandlers that are defined. If none are defined then
     * you get a generic popup saying viewing results is not supported.
     */
    protected void addResultsViewer() {
        if (resultsHandler != null) {
            resultsPanel = new ResultsViewerPanel("resultsViewer", "modalViewer", resultsHandler);
            add(resultsPanel);
        }
    }

    /**
     * Add title bar and results viewer.
     */
    protected void addTitleBarAndResultsViewer() {
        addTitleBar();
        addResultsViewer();
    }
}
