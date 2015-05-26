package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPopupPanel extends Panel {

    private ServiceCanvasItemPanel.Callbacks callbacks;

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
    }


}
