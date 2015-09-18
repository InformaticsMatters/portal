package portal.webapp.notebook;

import org.apache.wicket.markup.html.WebPage;

/**
 * Created by mariapaz on 9/18/15.
 */
public class TestCanvasCellPage extends WebPage {

    private FileUploadCanvasItemPanel fileUploadCanvasItemPanel;
    private GroovyScriptCanvasItemPanel groovyScriptCanvasItemPanel;
    private TableViewCanvasItemPanel tableViewCanvasItemPanel;

    public TestCanvasCellPage() {
        addPanels();
    }

    private void addPanels() {
        fileUploadCanvasItemPanel = new FileUploadCanvasItemPanel("fileUploadItem");
        add(fileUploadCanvasItemPanel);
        fileUploadCanvasItemPanel.setOutputMarkupPlaceholderTag(true);

        groovyScriptCanvasItemPanel = new GroovyScriptCanvasItemPanel("groovyScriptItem");
        add(groovyScriptCanvasItemPanel);
        groovyScriptCanvasItemPanel.setOutputMarkupPlaceholderTag(true);

        tableViewCanvasItemPanel = new TableViewCanvasItemPanel("tableViewItem");
        add(tableViewCanvasItemPanel);
        tableViewCanvasItemPanel.setOutputMarkupPlaceholderTag(true);
    }
}
