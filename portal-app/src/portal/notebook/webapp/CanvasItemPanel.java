package portal.notebook.webapp;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.io.IODescriptor;
import org.squonk.jobdef.JobStatus.Status;
import org.squonk.types.BasicObject;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class CanvasItemPanel extends Panel implements CellTitleBarPanel.CallbackHandler {

    public static final String OPTION_CONFIG = "configuration";
    public static final String OPTION_SELECTED_IDS = "selectionSelected";
    public static final String OPTION_MARKED_IDS = "selectionMarked";
    public static final String OPTION_FILTER_IDS = "filteredIDs";
    private static final Logger LOG = Logger.getLogger(CanvasItemPanel.class.getName());
    private final Long cellId;
    protected ResultsHandler resultsHandler;
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

    public CanvasItemPanel(String id, Long cellId) {
        super(id);
        this.cellId = cellId;

        createResultsHandlers();

        try {
            updateStatusInfo();
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error refreshing status", t);
        }
    }


    protected void createResultsHandlers() {
        CellInstance cellInstance = findCellInstance();
        CellDefinition cellDefinition = cellInstance.getCellDefinition();
        List<IODescriptor> iods = cellDefinition.getVariableDefinitionList();
        // TODO - handle multiple outputs (resultsHandler should become an array and results viewer should handle multiple types)
        // TODO - handle other result types
        if (iods != null && iods.size() > 0) {
            for (IODescriptor iod : iods) {
                String name = iod.getName();
                if (iod.getPrimaryType() == Dataset.class) {
                    LOG.fine("Creating results handler for variable " + name + " in cell " + cellInstance.getName());
                    AbstractCellDatasetProvider datasetProvider = new OutputVariableCellDatasetProvider(notebookSession, getCellId(), name);
                    resultsHandler = new DatasetResultsHandler(name, notebookSession, this, datasetProvider);
                    return;
                } else {
                    // TODO this needs changing. It relates to the PDB Upload cell that has an output variable that is
                    // not a dataset. We need a PDBResultsHandler that allows the raw PDB content to be viewed and for it
                    // to be shown in Alex Rose's NGL viewer
                    resultsHandler = new DatasetResultsHandler(name, notebookSession, this, null);
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

        LOG.finer(js);

        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(js));
    }

    protected void makeCanvasItemResizable(HtmlHeaderContainer container, String fitCallbackFunction, int minWidth, int minHeight) {
        String js = "makeCanvasItemResizable(:minWidth, :minHeight, ':id', :fitCallback)";
        js = js.replace(":id", getMarkupId()).replace(":minWidth", Integer.toString(minWidth)).replace(":minHeight", Integer.toString(minHeight));
        js = js.replace(":fitCallback", "function(id) {" + fitCallbackFunction + "(id)}");
        container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(js));
    }

    public CellInstance findCellInstance() {
        return findCellInstance(cellId);
    }

    public CellInstance findCellInstance(Long cellId) {
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
            LOG.log(Level.WARNING, "Error removing cell", t);
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
                        updateAndNotifyCellStatus(ajaxRequestTarget);
                    }
                    oldExecution = lastExecution;
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Error refreshing status", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });
    }

    protected void notifyExecutionChanged(AjaxRequestTarget ajaxRequestTarget, Execution execution) {
        cellTitleBarPanel.applyExecutionStatus(execution);
        ajaxRequestTarget.add(cellTitleBarPanel);
        CellInstance cell = findCellInstance();
        if (cell != null) {
            CellChangeEvent.DataValues evt = new CellChangeEvent.DataValues(cellId, CellChangeEvent.SOURCE_ALL_DATA, execution.getJobStatus());
            cellChangeManager.notifyDataValuesChanged(evt, ajaxRequestTarget);
        } else {
            LOG.log(Level.WARNING, "Could not find cell" + cellId);
        }
    }

    protected void notifyOptionValuesChanged(String name, AjaxRequestTarget ajaxRequestTarget) {
        CellInstance cell = findCellInstance();
        if (cell != null) {
            CellChangeEvent.OptionValues evt = new CellChangeEvent.OptionValues(cell.getId(), name);
            cellChangeManager.notifyOptionValuesChanged(evt, ajaxRequestTarget);
        } else {
            LOG.warning("Could not find cell" + cellId);
        }
    }

    protected void updateAndNotifyCellStatus(AjaxRequestTarget ajaxRequestTarget) {
        updateStatusInfo();
        if (statusLabel != null) {
            ajaxRequestTarget.add(statusLabel);
        }
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

    public void processCellChanged(CellChangeEvent evt, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        if (evt.getSourceCellId().equals(getCellId())) {
            updateAndNotifyCellStatus(ajaxRequestTarget);
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

    protected boolean saveNotebook() {
        try {
            notebookSession.storeCurrentEditable();
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to save notebook", e);
            notifyMessage("Error", "Failed to save notebook: " + e.getLocalizedMessage());
            return false;
        }
    }

    protected void notifyMessage(String title, String message) {
        try {
            notifierProvider.getNotifier(getPage()).notify(title, message);
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Failed to notify message. Title: " + title + " Message: " + message, ex);
        }
    }

    /** Allows cells to specify what jobs statuses cause data to be refreshed.
     * Default is to update when jobStatus is null, COMPLETED or ERROR.
     *
     * @param jobStatus
     * @return
     */
    protected boolean doesJobStatusRequireRefresh(Status jobStatus) {
         return jobStatus == null || Status.COMPLETED == jobStatus || Status.ERROR == jobStatus;
    }

    protected <T> T getOptionValue(CellInstance cellInstance, String name, Class<T> type) {
        OptionInstance instance = cellInstance.getOptionInstanceMap().get(name);
        return  instance == null ? null : (T) instance.getValue();
    }

    protected boolean doesCellChangeRequireRefresh(CellChangeEvent evt) {

        CellInstance cellInstance = findCellInstance();
        if (cellInstance == null) {
            LOG.warning("Cannot find cell instance. This is not expected");
            return false;
        }
        LOG.fine("CellChangeEvent received by cell " + cellInstance.getName() + " [" + getCellId() + "]" + ": " + evt);
        CellInstance changedCell = findCellInstance(evt.getSourceCellId());
        if (cellInstance == null || changedCell == null) {
            return false;
        }

        // variable values have changed due to cell execution
        if (evt instanceof CellChangeEvent.DataValues) {
            LOG.fine("  DATA VALUES EVENT");
            CellChangeEvent.DataValues dvevt = (CellChangeEvent.DataValues) evt;
            if (doesJobStatusRequireRefresh(dvevt.getJobStatus())) {
                for (BindingInstance bindingInstance : cellInstance.getBindingInstanceMap().values()) {
                    VariableInstance variableInstance = bindingInstance.getVariableInstance();
                    if (variableInstance != null
                            && dvevt.getSourceCellId().equals(variableInstance.getCellId())
                            && (dvevt.getSourceName() == null
                            || CellChangeEvent.SOURCE_ALL_DATA.equals(dvevt.getSourceName())
                            || dvevt.getSourceName().equals(variableInstance.getVariableDefinition().getName()))
                            ) {
                        LOG.fine("  NEEDS DATA VALUES REFRESH");
                        return true;
                    }
                }
            }
        }

        // variable bindings have changed
        if (evt instanceof CellChangeEvent.DataBinding) {
            LOG.fine("  DATA BINDING EVENT");
            CellChangeEvent.DataBinding dbevt = (CellChangeEvent.DataBinding) evt;
            // check if we were the target
            if (dbevt.getTargetCellId().equals(cellInstance.getId())) {
                BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(dbevt.getTargetName());
                LOG.fine("  Checking " + bindingInstance);
                if (bindingInstance != null) {
                    VariableInstance variableInstance = bindingInstance.getVariableInstance();
                    if (dbevt.getType() == CellChangeEvent.BindingChangeType.Bind && variableInstance != null
                            && dbevt.getSourceCellId().equals(variableInstance.getCellId())
                            && dbevt.getSourceName().equals(variableInstance.getVariableDefinition().getName())) {
                        LOG.fine("  NEEDS DATA BINDING REFRESH");
                        return true;
                    } else if (dbevt.getType() == CellChangeEvent.BindingChangeType.Unbind) {
                        LOG.fine("  NEEDS DATA BINDING REFRESH");
                        return true;
                    }
                }
            }
        }

        // option values have changed
        if (evt instanceof CellChangeEvent.OptionValues) {
            LOG.fine("  OPTION VALUES EVENT");
            CellChangeEvent.OptionValues ovevt = (CellChangeEvent.OptionValues) evt;
            for (OptionBindingInstance optionBindingInstance : cellInstance.getOptionBindingInstanceMap().values()) {
                OptionInstance optionInstance = optionBindingInstance.getOptionInstance();
                if (optionInstance != null
                        && ovevt.getSourceCellId().equals(optionInstance.getCellId())
                        && ovevt.getSourceName().equals(optionInstance.getOptionDescriptor().getKey())) {
                    LOG.fine("  NEEDS OPTION REFRESH");
                    return true;
                }
            }
        }

        // option binding has changed
        if (evt instanceof CellChangeEvent.OptionBinding) {
            LOG.fine("  OPTION BINDING EVENT");
            CellChangeEvent.OptionBinding obevt = (CellChangeEvent.OptionBinding) evt;
            // check if we were the target
            if (obevt.getTargetCellId().equals(cellInstance.getId())) {
                OptionBindingInstance optionBindingInstance = cellInstance.getOptionBindingInstanceMap().get(obevt.getTargetName());
                OptionInstance optionInstance = optionBindingInstance.getOptionInstance();
                if (obevt.getType() == CellChangeEvent.BindingChangeType.Bind
                        && optionInstance != null
                        && obevt.getSourceCellId().equals(optionInstance.getCellId())
                        && obevt.getSourceName().equals(optionInstance.getOptionDescriptor().getKey())) {
                    LOG.fine("  NEEDS OPTION REFRESH");
                    return true;
                } else if (obevt.getType() == CellChangeEvent.BindingChangeType.Unbind) {
                    LOG.fine("  NEEDS OPTION REFRESH");
                    return true;
                }
            }
        }

        LOG.fine("  NO REFRESH");
        return false;
    }

    /** Read the dataset fo the variable and apply a filter based on the UUIDs that might be present in the option named filterOption
     *
     * @param variableInstance The variable instance for the dataset
     * @param filterOption The name of the option containing the filter
     * @return
     * @throws Exception
     */
    protected Dataset<? extends BasicObject> generateFilteredData(VariableInstance variableInstance, String filterOption) throws Exception {

        Dataset<? extends BasicObject> dataset = notebookSession.squonkDataset(variableInstance);
        if (dataset == null) {
            return null;
        }
        if (filterOption == null) {
            return dataset;
        }

        // apply the selection filter
        Set<UUID> selectionFilter = findCellInstance().readOptionBindingFilter(filterOption);
        if (selectionFilter == null || selectionFilter.size() == 0) {
            return dataset;
        } else {
            DatasetMetadata meta = dataset.getMetadata().clone();
            meta.setSize(0);
            final AtomicInteger counter = new AtomicInteger(0);
            Stream<? extends BasicObject> filtered = dataset.getStream()
                    .filter((o) -> selectionFilter.contains(o.getUUID()))
                    .peek((o) -> counter.incrementAndGet())
                    .onClose(() -> meta.setSize(counter.get()));

            Dataset<? extends BasicObject> result = new Dataset(meta.getType(), filtered, meta);
            return result;
        }
    }

    public List<Panel> collectExpandedPanels(List<Panel> panels) {
        return panels;
    }

}
