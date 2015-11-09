package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;

public class AddCanvasItemPanel extends CanvasItemPanel<AddCellModel> {
    private static final Logger logger = LoggerFactory.getLogger(AddCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<AddData> form;

    public AddCanvasItemPanel(String id, AddCellModel cell) {
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

        TextField<Integer> num1Field = new TextField<Integer>("num1");
        form.add(num1Field);
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
        load();
    }

    private void load() {
        form.getModelObject().setNum1(getCellModel().getNum1());
        form.getModelObject().setNum2(getCellModel().getNum2());
        form.getModelObject().setResult((Integer) notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "result").getValue());
    }

    private void store() {
        notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "result").setValue(null);
        getCellModel().setNum1(form.getModelObject().getNum1());
        getCellModel().setNum2(form.getModelObject().getNum2());
        notebookSession.storeNotebook();
    }


    private class AddData implements Serializable {
        private Integer num1;
        private Integer num2;
        private Integer result;

        public Integer getNum1() {
            return num1;
        }

        public void setNum1(Integer num1) {
            this.num1 = num1;
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