package portal.notebook;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class ScriptCanvasItemPanel extends CanvasItemPanel {

    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;
    private Label outcomeLabel;
    private IModel<String> outcomeModel;

    public ScriptCanvasItemPanel(String id, CellModel cell) {
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
                String errorMessage = (String) getCellModel().getOptionMap().get("errorMessage");
                if (errorMessage != null) {
                    return errorMessage;
                } else {
                    Object outcome = getCellModel().getOptionMap().get("outcome");
                    return outcome == null ? "[nothing]" : outcome.toString();
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
        form = new Form<ModelObject>("form");
        ModelObject modelObject = new ModelObject();
        modelObject.load();
        TextArea<String> codeArea = new TextArea<String>("code");
        IndicatingAjaxSubmitLink runLink = new IndicatingAjaxSubmitLink("submit", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                processRun(target);
            }
        };
        form.setModel(new CompoundPropertyModel<ModelObject>(modelObject));
        form.add(codeArea);
        add(runLink);
        add(form);
    }

    private void processRun(AjaxRequestTarget ajaxRequestTarget) {
        form.getModelObject().store();
        notebookSession.storeNotebook();
        notebookSession.executeCell(getCellModel().getName());
        notebookSession.reloadNotebook();
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

    public class ModelObject implements Serializable {
        private String code;


        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public void store() {
            getCellModel().getOptionMap().put("code", code);
        }

        public void load() {
            code = (String) getCellModel().getOptionMap().get("code");
        }
    }

}




