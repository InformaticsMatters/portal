package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class QndProducerCellPanel extends CellPanel<QndProducerCell> {
    @Inject
    private NotebooksSession notebooksSession;

    public QndProducerCellPanel(String id, Notebook notebook, QndProducerCell cell) {
        super(id, notebook, cell);
        add(new Label("name", cell.getName()));
        add(new AjaxLink("register") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getNotebook().unregisterVariablesForProducer(getCell());
                List<Variable> list = new ArrayList<Variable>();
                for (String name : getCell().getOutputVariableNameList()) {
                    Variable variable = new Variable();
                    variable.setProducer(getCell());
                    variable.setName(name);
                    variable.setValue("Value of " + name);
                    list.add(variable);
                }
                getNotebook().registerVariables(list);
                notebooksSession.saveNotebook(notebook);
            }
        });
    }
}
