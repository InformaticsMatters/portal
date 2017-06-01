package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.time.Duration;
import portal.SessionContext;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class NotebookListPanel extends Panel {

    private static final Logger LOG = Logger.getLogger(NotebookListPanel.class.getName());
    private Form<FilterNotebookData> searchForm;
    private final EditNotebookPanel editNotebookPanel;
    private WebMarkupContainer notebooks;
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
        addSearchForm();
        addNotebookList();
    }

    private void addSearchForm() {
        searchForm = new Form<>("form");
        searchForm.setModel(new CompoundPropertyModel<>(new FilterNotebookData()));
        searchForm.setOutputMarkupId(true);
        add(searchForm);

        TextField<String> patternField = new TextField<>("pattern");
        patternField.add(new AjaxFormSubmitBehavior(searchForm, "keyup") {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                getRequestCycle().find(AjaxRequestTarget.class).add(notebooks);;
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setThrottlingSettings(new ThrottlingSettings(patternField.getMarkupId(), Duration.milliseconds(500), true));
            }
        });
        searchForm.add(patternField);

        AjaxSubmitLink searchAction = new IndicatingAjaxSubmitLink("search") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                getRequestCycle().find(AjaxRequestTarget.class).add(notebooks);;
            }
        };
        searchForm.add(searchAction);
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
            LOG.log(Level.WARNING, "Error listing notebooks", t);
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
                    FilterNotebookData searchCellData = searchForm.getModelObject();
                    String pattern = searchCellData.getPattern();
                    return filterNotebooks(items, pattern);
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
                            LOG.log(Level.WARNING, "Error configuring for edit", t);
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
                            LOG.log(Level.WARNING, "Error updating notebook", t);
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
                            LOG.log(Level.WARNING, "Error configuring for remove", t);
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
                            LOG.log(Level.WARNING, "Error loading notebook", t);
                            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                        }
                    }
                });

                if (notebookInfo.getId().equals(currentId)) {
                    selectedMarkupId = listItem.getMarkupId();
                }

            }
        };

        // we need a WebMarkupContainer here for the AJAX target when filtering
        notebooks = new WebMarkupContainer("notebooks");
        notebooks.setOutputMarkupId(true);
        notebooks.add(listView);
        add(notebooks);
    }

    /** Update the list of notebooks from the service
     *
     * @throws Exception
     */
    public void refreshNotebookList() throws Exception {
        LOG.info("refresh notebooks...");
        FilterNotebookData searchCellData = searchForm.getModelObject();
        List<NotebookInfo> notebookInfos = notebookSession.listNotebookInfo();
        listView.setList(notebookInfos);
        getRequestCycle().find(AjaxRequestTarget.class).add(notebooks);
    }

    /** Apply the filter to the notebooks
     *
     * @param notebooks
     * @param pattern
     * @return
     */
    private List<NotebookInfo> filterNotebooks(List<NotebookInfo> notebooks, String pattern) {
        if (pattern != null && pattern.length() > 0) {
            pattern = pattern.toLowerCase();
            List<NotebookInfo> filtered = new ArrayList<>();
            for (NotebookInfo ni : notebooks) {
                if (ni.getName().toLowerCase().contains(pattern) || ni.getOwner().toLowerCase().contains(pattern)) {
                    filtered.add(ni);
                }
            }
            return filtered;
        } else {
            return notebooks;
        }
    }

    class FilterNotebookData implements Serializable {

        private String pattern;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }

}
