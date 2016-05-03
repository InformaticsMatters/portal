package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.*;
import toolkit.wicket.semantic.NotifierProvider;


import javax.inject.Inject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class BindingsPanel extends Panel {

    private static final Logger LOGGER = Logger.getLogger(BindingsPanel.class.getName());
    private CellInstance cellInstance;
    @Inject
    private NotebookSession notebookSession;
    private List<BindingInstance> bindingInstanceList;
    private WebMarkupContainer bindingListContainer;
    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private CellChangeManager cellChangeManager;

    public BindingsPanel(String id, CellInstance cellInstance) {
        super(id);
        this.cellInstance = cellInstance;
        addBindingList();
    }

    private void addBindingList() {
        bindingListContainer = new WebMarkupContainer("bindingListContainer");
        bindingListContainer.setOutputMarkupId(true);

        bindingInstanceList = rebuildBindingInstanceList();
        ListView<BindingInstance> listView = new ListView<BindingInstance>("binding", bindingInstanceList) {

            @Override
            protected void populateItem(ListItem<BindingInstance> listItem) {
                final BindingInstance bindingInstance = listItem.getModelObject();
                listItem.add(new Label("targetName", bindingInstance.getDisplayName()));
                VariableInstance variableInstance = bindingInstance.getVariableInstance();
                String sourceDisplayName = resolveDisplayNameFor(variableInstance);
                listItem.add(new Label("variableName", sourceDisplayName));
                AjaxLink unassignLink = new AjaxLink("unassign") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        try {
                            removeBinding(bindingInstance, ajaxRequestTarget);
                            ajaxRequestTarget.add(BindingsPanel.this.getPage());
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error removing binding", e);
                            notifierProvider.getNotifier(getPage()).notify("Error", e.getMessage());
                        }
                    }
                };
                unassignLink.setVisible(sourceDisplayName != null);
                listItem.add(unassignLink);
            }
        };
        bindingListContainer.add(listView);
        add(bindingListContainer);
    }

    private void removeBinding(BindingInstance bindingInstance, AjaxRequestTarget ajaxRequestTarget) throws Exception {
        CellInstance boundCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellInstance.getId());
        BindingInstance boundBindingInstance = boundCellInstance.getBindingInstanceMap().get(bindingInstance.getName());
        boundBindingInstance.setVariableInstance(null);
        notebookSession.storeCurrentEditable();
        cellInstance = boundCellInstance;
        cellChangeManager.notifyBindingChanged(boundCellInstance.getId(), bindingInstance.getName(), ajaxRequestTarget);
    }

    private List<BindingInstance> rebuildBindingInstanceList() {
        if (cellInstance == null) {
            return new ArrayList<>();
        } else {
            ArrayList<BindingInstance> list = new ArrayList<BindingInstance>(cellInstance.getBindingInstanceMap().values());
            Collections.sort(list, new Comparator<BindingInstance>() {

                @Override
                public int compare(BindingInstance o1, BindingInstance o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return list;
        }
    }

    private String resolveDisplayNameFor(VariableInstance variableInstance) {
        if (variableInstance == null) {
            return null;
        }
        CellInstance producerCellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(variableInstance.getCellId());
        return producerCellInstance.getName() + " " + variableInstance.getVariableDefinition().getDisplayName();
    }


}
