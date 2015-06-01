package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import toolkit.wicket.marvin4js.MarvinSketcher;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPopupPanel extends Panel {

    private ServiceCanvasItemPanel.Callbacks callbacks;
    private MarvinSketcher marvinSketcherPanel;

    public ServiceCanvasItemPopupPanel(String id, ServiceCanvasItemPanel.Callbacks callbacks) {
        super(id);
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);

        add(new AjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onDelete();
            }
        });

        add(new AjaxLink("sketcher") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                marvinSketcherPanel.showModal();
            }
        });

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", "modalElement");
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                marvinSketcherPanel.hideModal();
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);

    }


}
