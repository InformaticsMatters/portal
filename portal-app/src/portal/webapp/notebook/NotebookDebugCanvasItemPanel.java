package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class NotebookDebugCanvasItemPanel extends CanvasItemPanel<NotebookDebugCell> {

    private ListView<Variable> listView;
    private IModel<List<Variable>> listModel;
    private WebMarkupContainer listContainer;
    @Inject
    private NotebooksSession notebooksSession;

    public NotebookDebugCanvasItemPanel(String id, Notebook notebook, NotebookDebugCell cell) {
        super(id, notebook, cell);
        addHeader();
        setOutputMarkupId(true);
        addList();
        addListeners();
    }

    private void addHeader() {
        add(new Label("cellName", getCell().getName()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getNotebook().removeCell(getCell());
                notebooksSession.saveNotebook(getNotebook());
            }
        });
    }

    private void addListeners() {
        VariableChangeListener variableChangeListener = new VariableChangeListener() {

            @Override
            public void onValueChanged(Variable source, Object oldValue) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);
            }

            @Override
            public void onVariableRemoved(Variable source) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);
            }
        };
        for (Variable variable : getNotebook().getVariableList()) {
            variable.removeChangeListener(variableChangeListener);
            variable.addChangeListener(variableChangeListener);
        }
        getNotebook().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(Cell cell) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);
            }

            @Override
            public void onCellAdded(Cell cell) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);

            }
        });
    }

    private void addList() {
        listModel = new IModel<List<Variable>>() {
            @Override
            public List<Variable> getObject() {
                return new ArrayList<>(getNotebook().getVariableList());
            }

            @Override
            public void setObject(List<Variable> variables) {

            }

            @Override
            public void detach() {

            }
        };
        listView = new ListView<Variable>("item", listModel) {
            @Override
            protected void populateItem(ListItem<Variable> listItem) {
                Variable variable = listItem.getModelObject();
                Label varNameLabel = new Label("varName", variable.getProducer().getName() + "." + variable.getName());
                listItem.add(varNameLabel);
                Label varValueLabel = new Label("varValue", variable.getValue() == null ? "null" : variable.getValue().toString());
                listItem.add(varValueLabel);
            }
        };
        listView.setOutputMarkupId(true);
        listContainer = new WebMarkupContainer("itemContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);
        listContainer.add(listView);
    }

}

