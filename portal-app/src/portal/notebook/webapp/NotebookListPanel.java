package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.SessionContext;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
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

        final IModel<List<NotebookInfo>> model = new IModel<List<NotebookInfo>>() {

            List<NotebookInfo> items;

            @Override
            public void detach() {
                items = null;
            }

            @Override
            public List<NotebookInfo> getObject() {
                if (items == null) {
                    return Collections.emptyList();
                } else {
                    return items;
                }
            }

            @Override
            public void setObject(List<NotebookInfo> items) {
                this.items = items;
            }
        };

        listView = new ListView<NotebookInfo>("notebook", model) {


            @Override
            protected void onBeforeRender() {
                model.setObject(getNotebookInfoList());
                super.onBeforeRender();
            }

            @Override
            protected void populateItem(ListItem<NotebookInfo> listItem) {

                NotebookInfo currentNotebookInfo = notebookSession.getCurrentNotebookInfo();
                Long currentId = currentNotebookInfo == null ? null : currentNotebookInfo.getId();

                NotebookInfo notebookInfo = listItem.getModelObject();
                boolean isOwner = sessionContext.getLoggedInUserDetails().getUserid().equals(notebookInfo.getOwner());
                boolean isShared = notebookInfo.getShared();
                boolean isCurrentNotebook = notebookInfo.getId().equals(currentId);
                listItem.add(new Label("name", notebookInfo.getName()));
                listItem.add(new Label("owner", notebookInfo.getOwner()));

                AjaxLink editLink = new AjaxLink("edit") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        try {
                            editNotebookPanel.configureForEdit(notebookInfo.getId());
                            editNotebookPanel.showModal();
                        } catch (Throwable t) {
                            LOGGER.warn("Error configuring for edit", t);
                            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                        }
                    }
                };
                listItem.add(editLink);
                editLink.setVisible(isOwner && isCurrentNotebook);

                AjaxLink changeStatusLink = new AjaxLink("changeStatus") {

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
                listItem.add(changeStatusLink);
                changeStatusLink.setVisible(isOwner && notebookInfo.getShareable() && isCurrentNotebook);

                WebMarkupContainer publicNb = new WebMarkupContainer("publicNb");
                changeStatusLink.add(publicNb);
                publicNb.setVisible(isShared && isCurrentNotebook);

                WebMarkupContainer privateNb = new WebMarkupContainer("privateNb");
                changeStatusLink.add(privateNb);
                privateNb.setVisible(!isShared && isCurrentNotebook);

                WebMarkupContainer publicNbLabel = new WebMarkupContainer("publicNbLabel");
                listItem.add(publicNbLabel);
                publicNbLabel.setVisible(isShared && (!isOwner || !notebookInfo.getShareable()) && isCurrentNotebook);

                WebMarkupContainer privateNbLabel = new WebMarkupContainer("privateNbLabel");
                listItem.add(privateNbLabel);
                privateNbLabel.setVisible(!isShared && (!isOwner || !notebookInfo.getShareable()) && isCurrentNotebook);

                AjaxLink removeLink = new AjaxLink("remove") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        try {
                            editNotebookPanel.configureForRemove(notebookInfo.getId());
                            editNotebookPanel.showModal();
                        } catch (Throwable t) {
                            LOGGER.warn("Error configuring for remove", t);
                            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                        }
                    }
                };
                listItem.add(removeLink);
                removeLink.setVisible(isOwner && isCurrentNotebook);

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

                if (notebookInfo.getId().equals(currentId)) {
                    selectedMarkupId = listItem.getMarkupId();
                }

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
