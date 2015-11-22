package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class Sample1CanvasItemPanel extends CanvasItemPanel<Sample1CellModel> {
    private static final Logger logger = LoggerFactory.getLogger(Sample1CanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;

    public Sample1CanvasItemPanel(String id, Sample1CellModel cell) {
        super(id, cell);
        addHeader();
        setOutputMarkupId(true);
    }

    private void addHeader() {
        add(new Label("cellName", getCellModel().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                notebookSession.getNotebookModel().removeCell(getCellModel());
                notebookSession.storeNotebook();
                ajaxRequestTarget.add(getParent());
            }
        });
        notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "number").setValue(1);
        notebookSession.storeNotebook();
    }


}