package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.string.StringValue;

/**
 * @author simetrias
 */
public class TableDisplayNavigationPanel extends Panel {

    private final TableDisplayVisualizer tableDisplayVisualizer;

    public TableDisplayNavigationPanel(String id, TableDisplayVisualizer tableDisplayVisualizer) {
        super(id);
        setOutputMarkupId(true);
        this.tableDisplayVisualizer = tableDisplayVisualizer;
        addNavigationControls();
    }

    private void addNavigationControls() {
        add(new AjaxLink("first") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                tableDisplayVisualizer.setCurrentPage(0);
                ajaxRequestTarget.appendJavaScript("fitTableDisplayGrid('" + TableDisplayNavigationPanel.this.getParent().getMarkupId() + "');");
            }
        });

        add(new AjaxLink("last") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                tableDisplayVisualizer.setCurrentPage(tableDisplayVisualizer.getPageCount() - 1);
                ajaxRequestTarget.appendJavaScript("fitTableDisplayGrid('" + TableDisplayNavigationPanel.this.getParent().getMarkupId() + "');");
            }
        });

        add(new AjaxPagingNavigation("navigation", tableDisplayVisualizer) {

            @Override
            public void onEvent(IEvent<?> event) {
                super.onEvent(event);
                AjaxRequestTarget ajaxRequestTarget = getRequestCycle().find(AjaxRequestTarget.class);
                StringValue executionStatusTimer = getRequest().getQueryParameters().getParameterValue("executionStatusTimer");
                boolean proceed = executionStatusTimer == null ? true : ! "true".equals(executionStatusTimer.toString("false"));
                if (proceed) {
                    ajaxRequestTarget.add(TableDisplayNavigationPanel.this);
                    ajaxRequestTarget.add(tableDisplayVisualizer);
                    ajaxRequestTarget.appendJavaScript("fitTableDisplayGrid('" + TableDisplayNavigationPanel.this.getParent().getMarkupId() + "');");
                }
            }
        });
    }
}

