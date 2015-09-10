package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class NotebookDebugCellPanel extends CellPanel<NotebookDebugCellDescriptor> {

    public NotebookDebugCellPanel(String id, NotebookDescriptor notebookDescriptor, NotebookDebugCellDescriptor cellDescriptor) {
        super(id, notebookDescriptor, cellDescriptor);
        setOutputMarkupId(true);
        addOutcome();
    }

    private void addOutcome() {
        AjaxLink refreshLink = new AjaxLink("refresh") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                 ajaxRequestTarget.add(NotebookDebugCellPanel.this);
            }
        };
        add(refreshLink);
        IModel<? extends List<Variable>> listModel = new IModel<List<Variable>>() {
            @Override
            public List<Variable> getObject() {
                return new ArrayList<Variable>(getNotebookDescriptor().getVariableMap().values());
            }

            @Override
            public void setObject(List<Variable> variables) {

            }

            @Override
            public void detach() {

            }
        };
        ListView<Variable> listView = new ListView<Variable>("item", listModel) {
            @Override
            protected void populateItem(ListItem<Variable> listItem) {
                Variable variable = listItem.getModelObject();
                Label varNameLabel = new Label("varName", variable.getName());
                listItem.add(varNameLabel);
                Label varValueLabel = new Label("varValue", variable.getValue() == null ? "null" : variable.getValue().toString());
                listItem.add(varValueLabel);
            }
        };
        listView.setOutputMarkupId(true);
        add(listView);
    }

}

