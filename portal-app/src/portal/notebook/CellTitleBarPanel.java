package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class CellTitleBarPanel extends Panel {

    private final CellModel cellModel;
    private final CellCallbackHandler callbackHandler;
    private AjaxLink openPopupLink;
    private CellPopupPanel cellPopupPanel;

    public CellTitleBarPanel(String id, CellModel cellModel, CellCallbackHandler callbackHandler) {
        super(id);
        this.cellModel = cellModel;
        this.callbackHandler = callbackHandler;
        addPopup();
        addActions();
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
    }

    public CellModel getCellModel() {
        return cellModel;
    }

    public CellCallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    private void addPopup() {
        cellPopupPanel = new CellPopupPanel("content");
        openPopupLink = new AjaxLink("openPopup") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                // popupContainerProvider.setPopupContentForPage(getPage(), popupPanel);
                // popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
                String js = "$('#:link')" +
                        ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.cellPopup.popup'), on : 'click'})" +
                        ".popup('toggle').popup('destroy')";
                js = js.replace(":link", openPopupLink.getMarkupId()).replace(":content", cellPopupPanel.getMarkupId());
                System.out.println(js);
                ajaxRequestTarget.appendJavaScript(js);
            }
        };
        add(openPopupLink);
    }

}
