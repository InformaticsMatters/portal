package portal.notebook;

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

public class ScriptCanvasItemPanel extends CanvasItemPanel<ScriptCellModel> {

    @Inject
    private NotebookSession notebookSession;
    private Form<CodeModel> form;
    private Label outcomeLabel;
    private IModel<String> outcomeModel;

    public ScriptCanvasItemPanel(String id, ScriptCellModel cell) {
        super(id, cell);
        setOutputMarkupId(true);
        addHeader();
        addForm();
        addOutcome();
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

    private void addOutcome() {
        outcomeModel = new IModel<String>() {
            @Override
            public String getObject() {
                if (getCellModel().getErrorMessage() != null) {
                    return getCellModel().getErrorMessage();
                } else {
                    return getCellModel().getOutcome() == null ? "[nothing]" : getCellModel().getOutcome().toString();
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
        modelObject.setCode(getCellModel().getCode());
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
        getCellModel().setCode(form.getModelObject().getCode());
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("Groovy");
        Bindings bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        /**/
        getCellModel().getInputVariableModelList().clear();
        getCellModel().getInputVariableModelList().addAll(notebookSession.getNotebookModel().getVariableModelList());
        /**/
        for (VariableModel variableModel : getCellModel().getInputVariableModelList()) {
            if (variableModel.getValue() != null) {
                String producerName = variableModel.getProducer().getName().replaceAll(" ", "_");
                bindings.put(producerName + "_" + variableModel.getName(), variableModel.getValue());
            }
        }
        bindings.put("session", notebookSession);
        try {
            Object result = scriptToVm(engine.eval(getCellModel().getCode()));
            getCellModel().setOutcome(result);
            getCellModel().setErrorMessage(null);
            for (String key : getCellModel().getOutputVariableNameList()) {
                Object value = engine.get(key);
                VariableModel variableModel = notebookSession.getNotebookModel().findVariable(getCellModel().getName(), key);
                if (variableModel != null) {
                    variableModel.setValue(value);
                }
            }
        } catch (ScriptException se) {
            getCellModel().setErrorMessage(se.getMessage());
        }
        notebookSession.storeNotebook();
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




