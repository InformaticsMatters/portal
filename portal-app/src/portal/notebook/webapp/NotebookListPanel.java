package portal.notebook.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookListPanel extends Panel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotebookListPanel.class);
    private final EditNotebookPanel editNotebookPanel;
    private ListView<NotebookInfo> listView;
    private String selectedMarkupId;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private SessionContext sessionContext;
    @Inject
    private NotifierProvider notifierProvider;

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
        try {
            return notebookSession.listNotebookInfo();
        } catch (Throwable t) {
            LOGGER.warn("Error listing notebooks", t);
            return new ArrayList<>();
        }
    }

    private void addNotebookList() {
        listView = new ListView<NotebookInfo>("notebook", new PropertyModel<List<NotebookInfo>>(this, "notebookInfoList")) {

            @Override
            protected void populateItem(ListItem<NotebookInfo> listItem) {
                NotebookInfo notebookInfo = listItem.getModelObject();
                boolean isOwner = sessionContext.getLoggedInUserDetails().getUserid().equals(notebookInfo.getOwner());
                listItem.add(new Label("name", notebookInfo.getName()));
                listItem.add(new Label("owner", notebookInfo.getOwner()));

                AjaxLink editLink = new AjaxLink("edit") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        try {
                            editNotebookPanel.configureForEdit(listItem.getModelObject().getId());
                            editNotebookPanel.showModal();
                        } catch (Throwable t) {
                            LOGGER.warn("Error configuring for edit", t);
                            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                        }
                    }
                };
                listItem.add(editLink);
                editLink.setVisible(isOwner);

                AjaxLink shareLink = new AjaxLink("share") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        try {
                            boolean share = !notebookInfo.getShared();
                            if (share && !notebookInfo.getShareable()) {
                                throw new Exception("At least one savepoint is required to share a notebook");
                            } else {
                                notebookSession.updateNotebook(notebookInfo.getId(), notebookInfo.getName(), notebookInfo.getDescription(), share);
                                refreshNotebookList();
                            }
                        } catch (Throwable t) {
                            LOGGER.warn("Error updating notebook", t);
                            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                        }
                    }
                };
                listItem.add(shareLink);
                shareLink.setVisible(isOwner && notebookInfo.getShareable());

                AjaxLink removeLink = new AjaxLink("remove") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        try {
                            editNotebookPanel.configureForRemove(listItem.getModelObject().getId());
                            editNotebookPanel.showModal();
                        } catch (Throwable t) {
                            LOGGER.warn("Error configuring for remove", t);
                            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                        }
                    }
                };
                listItem.add(removeLink);
                removeLink.setVisible(isOwner);

                listItem.add(new AjaxEventBehavior("onclick") {

                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        try {
                            notebookSession.loadCurrentNotebook(notebookInfo.getId());
                            selectedMarkupId = listItem.getMarkupId();
                            target.add(getPage());
                        } catch (Throwable t) {
                            LOGGER.warn("Error loading notebook", t);
                            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                        }
                    }
                });

                Long currentId = notebookSession.getCurrentNotebookInfo() == null ? null : notebookSession.getCurrentNotebookInfo().getId();
                if (listItem.getModelObject().getId().equals(currentId)) {
                    selectedMarkupId = listItem.getMarkupId();
                }
                Label shared = new Label("shared");
                if (notebookInfo.getShared()) {
                    shared.setDefaultModel(Model.of("public"));
                    shared.add(new AttributeModifier("class", "ui tiny blue label"));
                }
                listItem.add(shared);
                shared.setVisible(isOwner);
            }
        };
        add(listView);
    }

    public void refreshNotebookList() throws Exception {
        LOGGER.info("refresh...");
        listView.setList(notebookSession.listNotebookInfo());
        getRequestCycle().find(AjaxRequestTarget.class).add(this);
    }

}
