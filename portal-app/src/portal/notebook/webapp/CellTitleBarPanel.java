package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import portal.PopupContainerProvider;
import portal.notebook.api.CellInstance;
import portal.notebook.service.Execution;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class CellTitleBarPanel extends Panel {
    private static final Logger LOGGER = Logger.getLogger(CellTitleBarPanel.class.getName());
    private final CellInstance cellInstance;
    private final CallbackHandler callbackHandler;
    private BindingsPopupPanel bindingsPopupPanel;
    private AdvancedPopupPanel advancedPopupPanel;
    private IndicatingAjaxSubmitLink submitLink;
    private AjaxLink waitLink;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;

    public CellTitleBarPanel(String id, CellInstance cellInstance, CallbackHandler callbackHandler) {
        super(id);
        setOutputMarkupId(true);
        this.cellInstance = cellInstance;
        this.callbackHandler = callbackHandler;
        addTitleBarCssToggler();
        addToolbarControls();
        initExecutionStatus();
    }

    private void initExecutionStatus() {
        try {
            Execution lastExecution = notebookSession.findExecution(getCellInstance().getId());
            applyExecutionStatus(lastExecution);
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Error findingExecution", t);
            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
        }
    }

    private void addTitleBarCssToggler() {
        this.add(new ToggleCssAttributeModifier("failed-class", new ToggleCssAttributeModifier.Toggler() {

            @Override
            public boolean cssActiveIf() {
                try {
                    return isFailed();
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Error checking status", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                    return true;
                }
            }
        }));
    }

    private void addToolbarControls() {
        add(new Label("cellName", cellInstance.getName().toLowerCase()));

        bindingsPopupPanel = new BindingsPopupPanel("content", cellInstance);
        AjaxLink bindingsLink = new AjaxLink("bindings") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                onBindingsLinkClicked(this, ajaxRequestTarget);
            }
        };
        bindingsLink.setOutputMarkupId(true);
        add(bindingsLink);

        AjaxLink advancedLink = new AjaxLink("advanced") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                onAdvancedLinkClicked(this, ajaxRequestTarget);
            }
        };
        advancedLink.setOutputMarkupId(true);
        add(advancedLink);
        Panel advancedOptionsPanel = callbackHandler.getAdvancedOptionsPanel();
        if (advancedOptionsPanel != null) {
            advancedPopupPanel = new AdvancedPopupPanel("content", cellInstance, advancedOptionsPanel);
            advancedLink.setVisible(true);
        } else {
            advancedLink.setVisible(false);
        }

        submitLink = new IndicatingAjaxSubmitLink("submit", callbackHandler.getExecuteFormComponent()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    callbackHandler.onExecute();
                    target.add(CellTitleBarPanel.this);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Error executing " + getCellInstance().getName(), t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        };
        submitLink.setOutputMarkupId(true);
        add(submitLink);

        waitLink = new AjaxLink("wait") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {

            }
        };
        waitLink.setEnabled(false);
        waitLink.setOutputMarkupId(true);
        add(waitLink);

        add(new AjaxLink("remove") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    getCallbackHandler().onRemove(cellInstance);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Error removing cell", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });

        add(new AjaxLink("expand") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    callbackHandler.onShowResults();
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Error showing dataset details panel", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });


        AjaxEventBehavior event = new AjaxEventBehavior("onload") {
            @Override
            protected void onEvent(final AjaxRequestTarget target) {
                target.appendJavaScript("console.log('setting up dropdown');");
                target.appendJavaScript("$('.ui.dropdown').dropdown();");
            }
        };
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        // enable the dropdown menu - maybe a better place to do this?
        response.render(OnDomReadyHeaderItem.forScript(
                "$('#" + getMarkupId() +
                " .ui.dropdown').dropdown({action: 'hide', onChange: function(value, text) {" +
                        "applyCellMenuAction('" + getParent().getMarkupId() + "', value);" +
                        "}})"));
    }

    private void onBindingsLinkClicked(AjaxLink link, AjaxRequestTarget ajaxRequestTarget) {
        popupContainerProvider.setPopupContentForPage(getPage(), bindingsPopupPanel);
        popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
        String js = "$('#:link')" +
                ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.bindingsPopup.popup'), on : 'click'})" +
                ".popup('toggle').popup('destroy')";
        js = js.replace(":link", link.getMarkupId()).replace(":content", bindingsPopupPanel.getMarkupId());
        ajaxRequestTarget.appendJavaScript(js);
    }

    private void onAdvancedLinkClicked(AjaxLink link, AjaxRequestTarget ajaxRequestTarget) {
        popupContainerProvider.setPopupContentForPage(getPage(), advancedPopupPanel);
        popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
        String js = "$('#:link')" +
                ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.cellPopup.popup'), on : 'click'})" +
                ".popup('toggle').popup('destroy')";
        js = js.replace(":link", link.getMarkupId()).replace(":content", advancedPopupPanel.getMarkupId());
        ajaxRequestTarget.appendJavaScript(js);
    }

    public void applyExecutionStatus(Execution execution) {
        boolean active = execution != null && execution.getJobActive();
        waitLink.setVisible(active);
        submitLink.setVisible(!active);
    }

    private boolean isFailed() {
        Execution lastExecution = notebookSession.findExecution(cellInstance.getId());
        return lastExecution != null && Boolean.FALSE.equals(lastExecution.getJobSuccessful());
    }

    public CellInstance getCellInstance() {
        return cellInstance;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public interface CallbackHandler extends Serializable {

        void onRemove(CellInstance cellModel) throws Exception;

        Form getExecuteFormComponent();

        void onExecute() throws Exception;

        default Panel getAdvancedOptionsPanel() {
            return null;
        }

        void onShowResults() throws Exception;
    }
}
