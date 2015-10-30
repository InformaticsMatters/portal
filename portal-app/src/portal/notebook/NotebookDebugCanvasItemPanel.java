package portal.notebook;

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

public class NotebookDebugCanvasItemPanel extends CanvasItemPanel<NotebookDebugCellModel> {

    private ListView<VariableModel> listView;
    private IModel<List<VariableModel>> listModel;
    private WebMarkupContainer listContainer;
    @Inject
    private NotebookSession notebookSession;

    public NotebookDebugCanvasItemPanel(String id, NotebookDebugCellModel cell) {
        super(id, cell);
        addHeader();
        setOutputMarkupId(true);
        addList();
        addListeners();
    }

    private void addHeader() {
        add(new Label("cellName", getCellModel().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                notebookSession.getNotebookModel().removeCell(getCellModel());
                notebookSession.storeNotebook();
            }
        });
    }

    private void addListeners() {
        VariableChangeListener variableChangeListener = new VariableChangeListener() {

            @Override
            public void onValueChanged(VariableModel source, Object oldValue) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);
            }

            @Override
            public void onVariableRemoved(VariableModel source) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);
            }
        };
        for (VariableModel variableModel : notebookSession.getNotebookModel().getVariableModelList()) {
            variableModel.removeChangeListener(variableChangeListener);
            variableModel.addChangeListener(variableChangeListener);
        }
        notebookSession.getNotebookModel().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);
            }

            @Override
            public void onCellAdded(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(listContainer);

            }
        });
    }

    private void addList() {
        listModel = new IModel<List<VariableModel>>() {
            @Override
            public List<VariableModel> getObject() {
                return new ArrayList<>(notebookSession.getNotebookModel().getVariableModelList());
            }

            @Override
            public void setObject(List<VariableModel> variableModels) {

            }

            @Override
            public void detach() {

            }
        };
        listView = new ListView<VariableModel>("item", listModel) {
            @Override
            protected void populateItem(ListItem<VariableModel> listItem) {
                VariableModel variableModel = listItem.getModelObject();
                Label varNameLabel = new Label("varName", variableModel.getProducer().getName() + "." + variableModel.getName());
                listItem.add(varNameLabel);
                Label varValueLabel = new Label("varValue", variableModel.getValue() == null ? "null" : variableModel.getValue().toString());
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

