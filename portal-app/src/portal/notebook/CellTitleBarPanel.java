package portal.notebook;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;
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
    private AjaxLink openPopupLink;
    private CellPopupPanel cellPopupPanel;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    @Inject
    private NotebookSession notebookSession;
    private IndicatingAjaxSubmitLink submitLink;
    private AjaxLink waitLink;
    private AjaxLink warningLink;

    public CellTitleBarPanel(String id, CellInstance cellInstance, CallbackHandler callbackHandler) {
        super(id);
        this.cellInstance = cellInstance;
        this.callbackHandler = callbackHandler;
        addPopup();
        addActions();
        addBehaviors();
        createCellPopupPanel();
        refreshExecutionStatus();
        setOutputMarkupId(true);
    }

    private void addBehaviors() {
        add(new AbstractAjaxTimerBehavior(Duration.seconds(2)){
            @Override
            protected void onTimer(AjaxRequestTarget ajaxRequestTarget) {
                refreshExecutionStatus();
                ajaxRequestTarget.add(CellTitleBarPanel.this);
            }
        });
    }

    private void addActions() {
        add(new Label("cellName", cellInstance.getName().toLowerCase()));

        add(new AjaxLink("remove") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCallbackHandler().onRemove(cellInstance);
            }
        });

        AjaxLink bindingsAction = new AjaxLink("bindings") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCallbackHandler().onEditBindings(cellInstance);
            }
        };
        add(bindingsAction);
        if (cellInstance.getBindingMap().isEmpty()) {
            bindingsAction.setVisible(false);
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

    }

    public CellInstance getCellInstance() {
        return cellInstance;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    private void addPopup() {
        cellPopupPanel = new CellPopupPanel("content");
        openPopupLink = new AjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                decoratePopupLink(this, ajaxRequestTarget);
            }
        };
        openPopupLink.setOutputMarkupId(true);
        add(openPopupLink);

        this.add(new ToggleCssAttributeModifier("failed-class", new ToggleCssAttributeModifier.Toggler() {
            @Override
            public boolean cssActiveIf() {
                return isFailed();
            }
        }));

    }

    private void decoratePopupLink(AjaxLink link, AjaxRequestTarget ajaxRequestTarget) {
        popupContainerProvider.setPopupContentForPage(getPage(), cellPopupPanel);
        popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
        String js = "$('#:link')" +
                ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.cellPopup.popup'), on : 'click'})" +
                ".popup('toggle').popup('destroy')";
        js = js.replace(":link", link.getMarkupId()).replace(":content", cellPopupPanel.getMarkupId());
        ajaxRequestTarget.appendJavaScript(js);
    }

    private void refreshExecutionStatus() {
        Execution lastExecution = notebookSession.findExecution(cellInstance.getId());
        boolean active = lastExecution != null && lastExecution.getJobActive();
        waitLink.setVisible(active);
        submitLink.setVisible(!active);
        openPopupLink.setVisible(!isFailed());
    }

    private boolean isFailed() {
        Execution lastExecution = notebookSession.findExecution(cellInstance.getId());
        return lastExecution != null && Boolean.FALSE.equals(lastExecution.getJobSuccessful());
    }

    private void createCellPopupPanel() {
        cellPopupPanel = new CellPopupPanel("content");
    }

    public interface CallbackHandler extends Serializable {

        void onRemove(CellInstance cellModel);

        void onEditBindings(CellInstance cellModel);

        Form getExecuteFormComponent();

        void onExecute();

    }
}
