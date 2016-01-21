package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import portal.PopupContainerProvider;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class CellTitleBarPanel extends Panel {

    private final CellModel cellModel;
    private final CallbackHandler callbackHandler;
    private AjaxLink openPopupLink;
    private CellPopupPanel cellPopupPanel;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public CellTitleBarPanel(String id, CellModel cellModel, CallbackHandler callbackHandler) {
        super(id);
        this.cellModel = cellModel;
        this.callbackHandler = callbackHandler;
        addPopup();
        addActions();
        createCellPopupPanel();
    }

    private void addActions() {
        add(new Label("cellName", cellModel.getName().toLowerCase()));

        add(new AjaxLink("remove") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCallbackHandler().onRemove(cellModel);
            }
        });

        AjaxLink bindingsAction = new AjaxLink("bindings") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCallbackHandler().onEditBindings(cellModel);
            }
        };
        add(bindingsAction);
        if (cellModel.getBindingModelMap().isEmpty()) {
            bindingsAction.setVisible(false);
        }

        IndicatingAjaxSubmitLink submit = new IndicatingAjaxSubmitLink("submit", callbackHandler.getExecuteFormComponent()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                callbackHandler.onExecute();
            }
        };
        submit.setOutputMarkupId(true);
        add(submit);
    }

    public CellModel getCellModel() {
        return cellModel;
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

        void onRemove(CellModel cellModel);

        void onEditBindings(CellModel cellModel);

        Form getExecuteFormComponent();

        void onExecute();

    }
}
