package portal.webapp.notebook;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;
import javax.script.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class CodeCellPanel extends CellPanel<CodeCell> {

    @Inject
    private NotebooksSession notebooksSession;
    private Form<CodeModel> form;
    private Label outcomeLabel;
    private IModel<String> outcomeModel;

    public CodeCellPanel(String id, Notebook notebook, CodeCell cellDescriptor) {
        super(id, notebook, cellDescriptor);
        setOutputMarkupId(true);
        addForm();
        addOutcome();
    }

    private void addOutcome() {
        outcomeModel = new IModel<String>() {
            @Override
            public String getObject() {
                if (getCellDescriptor().getErrorMessage() != null) {
                    return getCellDescriptor().getErrorMessage();
                } else {
                    return getCellDescriptor().getOutcome() == null ? "[nothing]" : getCellDescriptor().getOutcome().toString();
                }
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
        form = new Form<CodeModel>("form");
        CodeModel modelObject = new CodeModel();
        modelObject.setCode(getCellDescriptor().getCode());
        TextArea<String> codeArea = new TextArea<String>("code");
        AjaxSubmitLink runLink = new AjaxSubmitLink("run") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                processRun(target);
            }
        };
        form.setModel(new CompoundPropertyModel<CodeModel>(modelObject));
        form.add(codeArea);
        form.add(runLink);
        add(form);
    }

    private void processRun(AjaxRequestTarget ajaxRequestTarget) {
        getCellDescriptor().setCode(form.getModelObject().getCode());
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        for (Variable variable : getNotebook().getVariableMap().values()) {
            if (variable.getValue() != null) {
                bindings.put(variable.getName(), variable.getValue());
            }
        }
        try {
            Object result = scriptToVm(engine.eval(getCellDescriptor().getCode()));
            getCellDescriptor().setOutcome(result);
            getCellDescriptor().setErrorMessage(null);
            for (String key : bindings.keySet()) {
                Variable variable = getNotebook().getVariableMap().get(key);
                if (variable == null) {
                    variable = new Variable();
                    variable.setName(key);
                    getNotebook().getVariableMap().put(key, variable);
                }

                variable.setValue(scriptToVm(bindings.get(key)));
            }
        } catch (ScriptException se) {
            getCellDescriptor().setErrorMessage(se.getMessage());
        }
        notebooksSession.saveNotebookDescriptor(getNotebook());
        ajaxRequestTarget.add(outcomeLabel);
    }

    private Object scriptToVm(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror)o;
            Collection<Object> result = new ArrayList<Object>();
            Collection<Object> values = scriptObjectMirror.values();
            for (Object value : values) {
                result.add(scriptToVm(value));
            }
            return result;
        } else {
            return o;
        }
    }

    public class CodeModel implements Serializable {
        private String code;


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

}




