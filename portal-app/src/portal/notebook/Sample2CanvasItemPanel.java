package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class Sample2CanvasItemPanel extends CanvasItemPanel<Sample2CellModel> {
    private static final Logger logger = LoggerFactory.getLogger(Sample2CanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<AddData> form;

    public Sample2CanvasItemPanel(String id, Sample2CellModel cell) {
        super(id, cell);
        addHeader();
        addForm();
        load();
    }

    private void addHeader() {
        add(new Label("cellName", getCellModel().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                notebookSession.getNotebookModel().removeCell(getCellModel());
                notebookSession.storeNotebook();
                ajaxRequestTarget.add(getParent());
            }
        });
    }


    private void addForm() {
        form = new Form<>("form");
        form.setOutputMarkupId(true);

        form.setModel(new CompoundPropertyModel<>(new AddData()));
        IModel<List<VariableModel>> inputVariableModel = new IModel<List<VariableModel>>() {
            @Override
            public List<VariableModel> getObject() {
                List<VariableModel> list = notebookSession.listAvailableInputVariablesFor(getCellModel(), notebookSession.getNotebookModel());
                return list;
            }

            @Override
            public void setObject(List<VariableModel> variableList) {

            }

            @Override
            public void detach() {

            }
        };
        DropDownChoice<VariableModel> inputVariableChoice = new DropDownChoice<VariableModel>("inputVariable", inputVariableModel);
        form.add(inputVariableChoice);
        TextField<Integer> num2Field = new TextField<Integer>("num2");
        form.add(num2Field);
        TextField<Integer> resultField = new TextField<Integer>("result");
        resultField.setEnabled(false);
        form.add(resultField);


        IndicatingAjaxSubmitLink submit = new IndicatingAjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    execute();
                    target.add(form);
                } catch (Throwable t) {
                    logger.error(null, t);
                }
            }
        };
        submit.setOutputMarkupId(true);
        form.add(submit);
        form.setOutputMarkupId(true);
        add(form);

    }

    private void execute() throws IOException {
        store();
        notebookSession.executeCell(getCellModel().getName());
        notebookSession.reloadNotebook();
        load();
    }

    private void load() {
        form.getModelObject().setInputVariable(getCellModel().getInputVariableModel());
        form.getModelObject().setNum2(getCellModel().getNum2());
        form.getModelObject().setResult((Integer) notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "result").getValue());
    }

    private void store() {
        notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "result").setValue(null);
        getCellModel().setInputVariableModel(form.getModelObject().getInputVariable());
        getCellModel().setNum2(form.getModelObject().getNum2());
        notebookSession.storeNotebook();
    }


    private class AddData implements Serializable {
        private Integer num2;
        private Integer result;
        private VariableModel inputVariable;


        public VariableModel getInputVariable() {
            return inputVariable;
        }

        public void setInputVariable(VariableModel inputVariable) {
            this.inputVariable = inputVariable;
        }

        public Integer getNum2() {
            return num2;
        }

        public void setNum2(Integer num2) {
            this.num2 = num2;
        }

        public Integer getResult() {
            return result;
        }

        public void setResult(Integer result) {
            this.result = result;
        }
    }

}