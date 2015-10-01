package portal.webapp.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.Serializable;

public class PropertyCalculateCanvasItemPanel extends CanvasItemPanel<PropertyCalculateCell> {
    @Inject
    private NotebooksSession notebooksSession;

    private Form<ModelObject> form;

    public PropertyCalculateCanvasItemPanel(String id, Notebook notebook, PropertyCalculateCell cell) {
        super(id, notebook, cell);
        addHeader();
        addForm();
        load();
    }

    private void addHeader() {
        add(new Label("cellName", getCell().getName()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                getNotebook().removeCell(getCell());
                notebooksSession.saveNotebook(getNotebook());
            }
        });
    }

    private void load() {
        form.getModelObject().setInputVariable(getCell().getInputVariable());
        Variable variable = getNotebook().findVariable(getCell(), "outputFileName");
        if (variable != null) {
            form.getModelObject().setOutputFileName((String)variable.getValue());
        }
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        DropDownChoice<Variable> inputVariableChoice = new DropDownChoice<Variable>("inputVariable", getNotebook().getVariableList());
        form.add(inputVariableChoice);
        TextField<String> outputFileNameField = new TextField<String>("outputFileName");
        form.add(outputFileNameField);
        AjaxSubmitLink calculateLink = new AjaxSubmitLink("calculate") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                calculateAndSave();
            }
        };
        form.add(calculateLink);
        add(form);
    }

    private void calculateAndSave() {
        getCell().setInputVariable(form.getModelObject().getInputVariable());
        byte[] buffer = notebooksSession.retrieveFileContentAsMolecules((String) getCell().getInputVariable().getValue());
        calculateTo(buffer, form.getModelObject().getOutputFileName());
        getNotebook().findVariable(getCell(), "outputFileName").setValue(form.getModelObject().getOutputFileName());
        notebooksSession.saveNotebook(getNotebook());
    }

    private void calculateTo(byte[] source, String fileName) {
        try {
            FileOutputStream outputStream = new FileOutputStream("files/" + fileName);
            try {
                  outputStream.write(source);
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    class ModelObject implements Serializable {
        private Variable inputVariable;
        private String outputFileName;

        public Variable getInputVariable() {
            return inputVariable;
        }

        public void setInputVariable(Variable inputVariable) {
            this.inputVariable = inputVariable;
        }

        public String getOutputFileName() {
            return outputFileName;
        }

        public void setOutputFileName(String outputFileName) {
            this.outputFileName = outputFileName;
        }
    }

}
