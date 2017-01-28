package portal.notebook.webapp.cell;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;
import portal.notebook.api.CellInstance;
import portal.notebook.webapp.CellTitleBarPanel;
import portal.notebook.webapp.NotebookCanvasPage;
import toolkit.wicket.semantic.SemanticModalPanel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 27/08/2016.
 */
public class CellDescriptionEditorPanel extends SemanticModalPanel {

    private static final Logger LOG = Logger.getLogger(CellDescriptionEditorPanel.class.getName());

    private Long cellId;
    private WebMarkupContainer refresh;

    private Form form;
    private TextArea editor;

    public CellDescriptionEditorPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    public void configure(Long cellId, WebMarkupContainer refresh) {
        this.cellId = cellId;
        this.refresh = refresh;
    }

    public void reset() {
        cellId = null;
        refresh = null;
    }

    private CellInstance findCellInstance() {
        NotebookCanvasPage page = (NotebookCanvasPage)getPage();
        return page.getNotebookSession().getCurrentNotebookInstance().findCellInstanceById(cellId);
    }


    private void addForm() {

        getModalRootComponent().add(new Label("cellName", new PropertyModel(this, "name")));

        form = new Form<>("form");
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);

        editor = new TextArea<>("description", new PropertyModel<>(this, "description"));
        form.add(editor);

        AjaxSubmitLink submitAction = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                System.out.println("Description submitted: " + getDescription());


                NotebookCanvasPage page = (NotebookCanvasPage)getPage();
                try {
                    page.getNotebookSession().storeCurrentEditable();
                    System.out.println("Notebook saved");
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to save description", e);
                    page.notifyMessage("Error", "Failed to save description: " + e.getLocalizedMessage());
                }

                if (refresh != null) {
                    System.out.println("Ajax target added: " + refresh);
                    target.add(refresh);
                }

                hideModal();
                reset();
            }
        };
        submitAction.setOutputMarkupId(true);
        form.add(submitAction);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                System.out.println("Description cancelled");
                hideModal();
                reset();
            }
        };
        form.add(cancelAction);
    }

    public String getName() {
        if (cellId == null) {
            return "";
        } else {
            CellInstance cellInstance = findCellInstance();
            if (cellInstance == null) {
                return "";
            } else {
                String name = findCellInstance().getName();
                return name == null ? "" : name;
            }
        }
    }

    public String getDescription() {
        if (cellId == null) {
            return "";
        } else {
            CellInstance cellInstance = findCellInstance();
            if (cellInstance == null) {
                return "";
            } else {
                String desc = (String) cellInstance.getSettings().get(CellTitleBarPanel.SETTING_DESCRIPTION);
                if (desc == null) {
                    desc = cellInstance.getCellDefinition().getDescription();
                }
                return desc == null ? "" : desc;
            }
        }
    }

    public void setDescription(String description) {
        if (cellId == null) {
            return;
        } else {
            findCellInstance().getSettings().put(CellTitleBarPanel.SETTING_DESCRIPTION, description);
        }
    }
}
