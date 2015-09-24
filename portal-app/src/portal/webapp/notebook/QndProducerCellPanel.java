package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;

import javax.inject.Inject;
import java.util.Map;

public class QndProducerCellPanel extends CellPanel<QndProducerCell> {
    @Inject
    private NotebooksSession notebooksSession;

    public QndProducerCellPanel(String id, Notebook notebook, QndProducerCell cell) {
        super(id, notebook, cell);
        add(new Label("name", cell.getName()));
        add(new AjaxLink("register") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCell().getOutputVariableNameList().add("var1");
                getCell().getOutputVariableNameList().add("var2");
                getCell().getOutputVariableNameList().add("var3");
                Map<String, Variable> map = getNotebook().findVariablesForProducer(getCell());
                if (map.isEmpty()) {
                    map = getNotebook().registerVariablesForProducer(getCell());
                    for (Variable variable : map.values()) {
                        variable.setValue("Value for " + variable.getName());
                    }
                }
                notebooksSession.saveNotebook(notebook);
            }
        });
        add(new AjaxLink("change") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getCell().getOutputVariableNameList().remove(2);
                getCell().getOutputVariableNameList().add("var4");
                Map<String, Variable> map = getNotebook().registerVariablesForProducer(getCell());
                for (Variable variable : map.values()) {
                    variable.setValue("Value for " + variable.getName());
                }
                notebooksSession.saveNotebook(notebook);
            }
        });
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getNotebook().removeCell(getCell());
                notebooksSession.saveNotebook(notebook);
            }
        });
    }
}
