package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import portal.PopupContainerProvider;
import portal.notebook.api.CellInstance;
import portal.notebook.service.Execution;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class CellTitleBarPanel extends Panel {

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
        Execution lastExecution = notebookSession.findExecution(getCellInstance().getId());
        applyExecutionStatus(lastExecution);
    }

    private void addTitleBarCssToggler() {
        this.add(new ToggleCssAttributeModifier("failed-class", new ToggleCssAttributeModifier.Toggler() {

            @Override
            public boolean cssActiveIf() {
                return isFailed();
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
                callbackHandler.onExecute();
                target.add(CellTitleBarPanel.this);
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
                getCallbackHandler().onRemove(cellInstance);
            }
        });
    }

    private void onBindingsLinkClicked(AjaxLink link, AjaxRequestTarget ajaxRequestTarget) {
        popupContainerProvider.setPopupContentForPage(getPage(), bindingsPopupPanel);
        popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
        String js = "$('#:link')" +
                ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.cellPopup.popup'), on : 'click'})" +
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

        void onRemove(CellInstance cellModel);

        Form getExecuteFormComponent();

        void onExecute();

        default Panel getAdvancedOptionsPanel() {
            return null;
        }
    }
}
