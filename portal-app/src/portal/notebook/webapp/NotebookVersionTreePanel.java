package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.io.ByteArrayOutputStream;
import portal.PortalWebApplication;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class NotebookVersionTreePanel extends Panel {

    @Inject
    private NotebookSession notebookSession;

    public NotebookVersionTreePanel(String id) {
        super(id);
        add(new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget ajaxRequestTarget) {
                ajaxRequestTarget.appendJavaScript("createTree(:json)".replace(":json", buildHistoryTreeAsJson()));
            }
        });
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/d3.min.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/versiontree.js")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(PortalWebApplication.class, "resources/versiontree.css")));
    }

    private String buildHistoryTreeAsJson() {
        try {
            String json = "";
            if (notebookSession.getCurrentNotebookInfo() != null) {
                HistoryTree history = notebookSession.buildCurrentNotebookHistoryTree();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(outputStream, history);
                outputStream.flush();
                json = outputStream.toString();
            }
            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
