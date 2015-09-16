package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;
import java.io.Serializable;

public class Poc1CellPanel extends CellPanel<Poc1CellDescriptor> {

    @Inject
    private NotebooksSession notebooksSession;
    private Form<Poc1Model> form;
    private Label outcomeLabel;
    private IModel<String> outcomeModel;

    public Poc1CellPanel(String id, NotebookDescriptor notebookDescriptor, Poc1CellDescriptor cellDescriptor) {
        super(id, notebookDescriptor, cellDescriptor);
        setOutputMarkupId(true);
        addForm();
        addOutcome();
    }

    private void addOutcome() {
        outcomeModel = new IModel<String>() {
            @Override
            public String getObject() {
                String varName = getCellDescriptor().getVarName();
                Variable variable = varName == null ? null : getNotebookDescriptor().getVariableMap().get(varName);
                Object value = variable == null ? null : variable.getValue();
                return value == null ? null : value.toString();
            }

            @Override
            public void setObject(String s) {

            }

            @Override
            public void detach() {

            }
        };
        outcomeLabel = new Label("outcome");
        outcomeLabel.setDefaultModel(outcomeModel);
        outcomeLabel.setOutputMarkupId(true);
        add(outcomeLabel);

    }

    private void addForm() {
        form = new Form<Poc1Model>("form");
        Poc1Model poc1Model = new Poc1Model();
        poc1Model.setVarName(getCellDescriptor().getVarName());
        Variable variable = getNotebookDescriptor().getVariableMap().get(getCellDescriptor().getVarName());
        poc1Model.setVarValue(variable == null ? null : (String) variable.getValue());
        TextField<String> varNameField = new TextField<String>("varName");
        TextField<String> varValueField = new TextField<String>("varValue");
        AjaxSubmitLink runLink = new AjaxSubmitLink("run") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                processRun(target);
            }
        };
        form.setModel(new CompoundPropertyModel<Poc1Model>(poc1Model));
        form.add(varNameField);
        form.add(varValueField);
        form.add(runLink);
        add(form);
    }

    private void processRun(AjaxRequestTarget ajaxRequestTarget) {
        getCellDescriptor().setVarName(form.getModelObject().getVarName());
        Variable variable = new Variable();
        variable.setName(getCellDescriptor().getVarName());
        variable.setVariableType(VariableType.STRING);
        variable.setValue(form.getModelObject().getVarValue());
        getNotebookDescriptor().getVariableMap().put(variable.getName(), variable);
        notebooksSession.saveNotebookDescriptor(getNotebookDescriptor());
        ajaxRequestTarget.add(outcomeLabel);
    }

    public class Poc1Model implements Serializable {
        private String varName;
        private String varValue;

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public String getVarValue() {
            return varValue;
        }

        public void setVarValue(String varValue) {
            this.varValue = varValue;
        }
    }

}

