package portal.webapp.notebook;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TableDisplayCellPanel extends CellPanel<TableDisplayCellDescriptor> {

    private Form<TableDisplayModel> form;
    @Inject
    private NotebooksSession notebooksSession;

    public TableDisplayCellPanel(String id, NotebookDescriptor notebookDescriptor, TableDisplayCellDescriptor cellDescriptor) {
        super(id, notebookDescriptor, cellDescriptor);
        setOutputMarkupId(true);
        addForm();
        addOutcome();
    }

    private void addForm() {
        form = new Form<TableDisplayModel>("form");
        TableDisplayModel tableDisplayModel = new TableDisplayModel();
        tableDisplayModel.setSourceVarName(getCellDescriptor().getSourceVarName());
        TextField<String> sourceVarNameField = new TextField<String>("sourceVarName");
        AjaxSubmitLink runLink = new AjaxSubmitLink("show") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                processShow(target);
            }
        };
        form.setModel(new CompoundPropertyModel<TableDisplayModel>(tableDisplayModel));
        form.add(sourceVarNameField);
        form.add(runLink);
        add(form);
    }

    private void processShow(AjaxRequestTarget ajaxRequestTarget) {
        getCellDescriptor().setSourceVarName(form.getModelObject().getSourceVarName());
        notebooksSession.saveNotebookDescriptor(getNotebookDescriptor());
        ajaxRequestTarget.add(TableDisplayCellPanel.this);
    }

    private void addOutcome() {
        IModel<? extends List<Object[]>> listModel = new IModel<List<Object[]>>() {
            @Override
            public List<Object[]> getObject() {
                String varName = getCellDescriptor().getSourceVarName();
                ArrayList<Object[]> list = new ArrayList<Object[]>();
                Variable variable = varName == null ? null : getNotebookDescriptor().getVariableMap().get(varName);
                Object value = variable == null ? null : variable.getValue();
                if (value != null) {
                    Collection<?> collection = (Collection<?>) value;
                    for (Object item : collection) {
                        if (item.getClass().isArray()) {
                            list.add((Object[]) item);
                        } else {
                            list.add(new Object[]{item});
                        }
                    }
                }
                return list;
            }

            @Override
            public void setObject(List<Object[]> variables) {

            }

            @Override
            public void detach() {

            }
        };
        ListView<Object[]> listView = new ListView<Object[]>("item", listModel) {
            @Override
            protected void populateItem(ListItem<Object[]> listItem) {
                Label label = new Label("cells", Arrays.toString(listItem.getModelObject()));
                listItem.add(label);
            }
        };
        listView.setOutputMarkupId(true);
        add(listView);
    }

    public class TableDisplayModel implements Serializable {
        private String sourceVarName;
        public String getSourceVarName() {
            return sourceVarName;
        }

        public void setSourceVarName(String sourceVarName) {
            this.sourceVarName = sourceVarName;
        }

    }

}

