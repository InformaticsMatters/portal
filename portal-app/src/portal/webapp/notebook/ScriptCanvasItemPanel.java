package portal.webapp.notebook;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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

public class ScriptCanvasItemPanel extends CanvasItemPanel<ScriptCell> {

    @Inject
    private NotebooksSession notebooksSession;
    private Form<CodeModel> form;
    private Label outcomeLabel;
    private IModel<String> outcomeModel;

    public ScriptCanvasItemPanel(String id, Notebook notebook, ScriptCell cell) {
        super(id, notebook, cell);
        setOutputMarkupId(true);
        addHeader();
        addForm();
        addOutcome();
    }

    private void addHeader() {
        add(new Label("cellName", getCell().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getNotebook().removeCell(getCell());
                notebooksSession.saveNotebook(getNotebook());
            }
        });
    }

    private void addOutcome() {
        outcomeModel = new IModel<String>() {
            @Override
            public String getObject() {
                if (getCell().getErrorMessage() != null) {
                    return getCell().getErrorMessage();
                } else {
                    return getCell().getOutcome() == null ? "[nothing]" : getCell().getOutcome().toString();
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
        modelObject.setCode(getCell().getCode());
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
        getCell().setCode(form.getModelObject().getCode());
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("Groovy");
        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        /**/
        getCell().getInputVariableList().clear();
        getCell().getInputVariableList().addAll(getNotebook().getVariableList());
        /**/
        for (Variable variable : getCell().getInputVariableList()) {
            if (variable.getValue() != null) {
                String producerName = variable.getProducer().getName().replaceAll(" ", "_");
                bindings.put(producerName + "_" + variable.getName(), variable.getValue());
            }
        }
        try {
            Object result = scriptToVm(engine.eval(getCell().getCode()));
            getCell().setOutcome(result);
            getCell().setErrorMessage(null);
            for (String key : getCell().getOutputVariableNameList()) {
                Object value = bindings.get(key);
                Variable variable = getNotebook().findVariable(getCell(), key);
                if (variable != null) {
                    variable.setValue(value);
                }
            }
        } catch (ScriptException se) {
            getCell().setErrorMessage(se.getMessage());
        }
        notebooksSession.saveNotebook(getNotebook());
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




