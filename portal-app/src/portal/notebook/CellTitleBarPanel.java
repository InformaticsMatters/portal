package portal.notebook;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
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
    private IndicatingAjaxSubmitLink submit;

    public CellTitleBarPanel(String id, CellInstance cellInstance, CallbackHandler callbackHandler) {
        super(id);
        this.cellInstance = cellInstance;
        this.callbackHandler = callbackHandler;
        addPopup();
        addActions();
        addBehaviors();
        createCellPopupPanel();
    }

    private void addBehaviors() {
        add(new AbstractAjaxTimerBehavior(Duration.seconds(2)){
            @Override
            protected void onTimer(AjaxRequestTarget ajaxRequestTarget) {
                Execution execution = notebookSession.findExecution(cellInstance.getId());
                if (execution != null && execution.getJobActive()) {
                   submit.setEnabled(false);
                } else {
                    submit.setEnabled(true);
                }
                ajaxRequestTarget.add(submit);
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

        submit = new IndicatingAjaxSubmitLink("submit", callbackHandler.getExecuteFormComponent()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                submit.setEnabled(false);
                target.add(submit);
                callbackHandler.onExecute();
            }
        };
        submit.setEnabled(false);
        submit.setOutputMarkupId(true);
        add(submit);
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
                popupContainerProvider.setPopupContentForPage(getPage(), cellPopupPanel);
                popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
                String js = "$('#:link')" +
                        ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.cellPopup.popup'), on : 'click'})" +
                        ".popup('toggle').popup('destroy')";
                js = js.replace(":link", openPopupLink.getMarkupId()).replace(":content", cellPopupPanel.getMarkupId());
                ajaxRequestTarget.appendJavaScript(js);
            }
        };
        add(openPopupLink);
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
