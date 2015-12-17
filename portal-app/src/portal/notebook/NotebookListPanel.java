package portal.notebook;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.notebook.service.NotebookInfo;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookListPanel extends Panel {

    private static final Logger logger = LoggerFactory.getLogger(NotebookListPanel.class);
    private final EditNotebookPanel editNotebookPanel;
    private ListView<NotebookInfo> listView;
    @Inject
    private NotebookSession notebooksSession;
    private String selectedMarkupId;

    public NotebookListPanel(String id, EditNotebookPanel editNotebookPanel) {
        super(id);
        this.editNotebookPanel = editNotebookPanel;
        addNotebookList();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (selectedMarkupId != null) {
            String js = "makeNbTrActive('" + selectedMarkupId + "');";
            response.render(OnDomReadyHeaderItem.forScript(js));
        }
    }

    private void addNotebookList() {
        List<NotebookInfo> notebookList = notebooksSession.listNotebookInfo();

        logger.info(notebookList.size() + " notebook/s found.");

        listView = new ListView<NotebookInfo>("notebook", notebookList) {

            @Override
            protected void populateItem(ListItem<NotebookInfo> listItem) {
                NotebookInfo notebookInfo = listItem.getModelObject();
                listItem.add(new Label("name", notebookInfo.getName()));
                AjaxLink editLink = new AjaxLink("edit") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        editNotebookPanel.configureForEdit(listItem.getModelObject().getId());
                        editNotebookPanel.showModal();
                    }
                };
                listItem.add(editLink);
                AjaxLink removeLink = new AjaxLink("remove") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        editNotebookPanel.configureForRemove(listItem.getModelObject().getId());
                        editNotebookPanel.showModal();
                    }
                };
                listItem.add(removeLink);
                listItem.add(new AjaxEventBehavior("onclick") {

                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        notebooksSession.loadCurrentNotebook(notebookInfo.getId());
                        target.add(getPage());
                    }
                });

                Long currentId = notebooksSession.getCurrentNotebookInfo() == null ? null : notebooksSession.getCurrentNotebookInfo().getId();
                if (listItem.getModelObject().getId().equals(currentId)) {
                    selectedMarkupId = listItem.getMarkupId();
                }
            }
        };
        add(listView);
    }

    public void refreshNotebookList() {
        listView.setList(notebooksSession.listNotebookInfo());
        getRequestCycle().find(AjaxRequestTarget.class).add(this);
    }
}
