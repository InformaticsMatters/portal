package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

import javax.inject.Inject;
import java.io.Serializable;

public class ScriptCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;
    private Label outcomeLabel;
    private IModel<String> outcomeModel;
    @Inject
    private NotebookSession notebookSession;

    public ScriptCanvasItemPanel(String id, CellModel cell) {
        super(id, cell);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        addOutcome();
    }

    private void addOutcome() {
        outcomeModel = new IModel<String>() {
            @Override
            public String getObject() {
                String errorMessage = (String) getCellModel().getOutputVariableModelMap().get("errorMessage").getValue();
                if (errorMessage != null) {
                    return errorMessage;
                } else {
                    Object outcome = getCellModel().getOutputVariableModelMap().get("outcome").getValue();
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
        form = new Form<>("form");
        ModelObject modelObject = new ModelObject();
        modelObject.load();
        TextArea<String> codeArea = new TextArea<String>("code");
        form.setModel(new CompoundPropertyModel<>(modelObject));
        form.add(codeArea);
        add(form);
    }

    private void processRun(AjaxRequestTarget ajaxRequestTarget) {
        form.getModelObject().store();
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellModel().getId());
        notebookSession.reloadCurrentNotebook();
        updateCellModel();
        ajaxRequestTarget.add(outcomeLabel);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        processRun(getRequestCycle().find(AjaxRequestTarget.class));
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
            getCellModel().getOptionModelMap().get("code").setValue(code);
        }

        public void load() {
            code = (String) getCellModel().getOptionModelMap().get("code").getValue();
        }
    }

}




