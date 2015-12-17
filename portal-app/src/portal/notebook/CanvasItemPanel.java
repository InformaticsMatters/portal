package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;

public abstract class CanvasItemPanel extends Panel {
    private final CellModel cellModel;
    private final CallbackHandler callbackHandler;
    private AjaxLink openPopupLink;
    private CellPopupPanel cellPopupPanel;

    public CanvasItemPanel(String id, CellModel cellModel, CallbackHandler callbackHandler) {
        super(id);
        this.cellModel = cellModel;
        this.callbackHandler = callbackHandler;
        addHeader();
        createCellPopupPanel();
    }

    private void addHeader() {
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

    private void createCellPopupPanel() {
        cellPopupPanel = new CellPopupPanel("content");
    }


    public CellModel getCellModel() {
        return cellModel;
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public interface CallbackHandler extends Serializable {
        void onRemove(CellModel cellModel);

        void onEditBindings(CellModel cellModel);

        void onContentChanged();
    }

}
