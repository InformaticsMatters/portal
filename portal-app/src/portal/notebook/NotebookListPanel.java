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
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookListPanel extends Panel {

    private static final Logger logger = LoggerFactory.getLogger(NotebookListPanel.class);
    private final EditNotebookPanel editNotebookPanel;
    private ListView<NotebookInfo> listView;
    private String selectedMarkupId;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private SessionContext sessionContext;

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

    public List<NotebookInfo> getNotebookInfoList() {
        return notebookSession.listNotebookInfo();
    }

    private void addNotebookList() {
        listView = new ListView<NotebookInfo>("notebook", new PropertyModel<List<NotebookInfo>>(this, "notebookInfoList")) {

            @Override
            protected void populateItem(ListItem<NotebookInfo> listItem) {
                NotebookInfo notebookDescriptor = listItem.getModelObject();
                boolean isOwner = sessionContext.getLoggedInUserDetails().getUserid().equals(notebookDescriptor.getOwner());
                listItem.add(new Label("name", notebookDescriptor.getName()));
                listItem.add(new Label("owner", notebookDescriptor.getOwner()));

                AjaxLink editLink = new AjaxLink("edit") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        editNotebookPanel.configureForEdit(listItem.getModelObject().getId());
                        editNotebookPanel.showModal();
                    }
                };
                listItem.add(editLink);
                editLink.setVisible(isOwner);

                AjaxLink shareLink = new AjaxLink("share") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        notebookSession.updateNotebook(notebookDescriptor.getId(), notebookDescriptor.getName(), notebookDescriptor.getDescription());
                        refreshNotebookList();
                    }
                };
                listItem.add(shareLink);
                shareLink.setVisible(isOwner);

                AjaxLink removeLink = new AjaxLink("remove") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        editNotebookPanel.configureForRemove(listItem.getModelObject().getId());
                        editNotebookPanel.showModal();
                    }
                };
                listItem.add(removeLink);
                removeLink.setVisible(isOwner);

                listItem.add(new AjaxEventBehavior("onclick") {

                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        notebookSession.loadCurrentNotebook(notebookDescriptor.getId());
                        target.add(getPage());
                    }
                });

                Long currentId = notebookSession.getCurrentNotebookInfo() == null ? null : notebookSession.getCurrentNotebookInfo().getId();
                if (listItem.getModelObject().getId().equals(currentId)) {
                    selectedMarkupId = listItem.getMarkupId();
                }
                Label shared = new Label("shared");
                /**if (notebookDescriptor.getShared()) {
                    shared.setDefaultModel(Model.of("public"));
                    shared.add(new AttributeModifier("class", "ui tiny blue label"));
                }**/
                listItem.add(shared);
                shared.setVisible(isOwner);
            }
        };
        add(listView);
    }

    public void refreshNotebookList() {
        listView.setList(notebookSession.listNotebookInfo());
        getRequestCycle().find(AjaxRequestTarget.class).add(this);
    }
}
