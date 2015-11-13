package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class Sample2CanvasItemPanel extends CanvasItemPanel<Sample2CellModel> {
    private static final Logger logger = LoggerFactory.getLogger(Sample2CanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;

    public Sample2CanvasItemPanel(String id, Sample2CellModel cell) {
        super(id, cell);
        addHeader();
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