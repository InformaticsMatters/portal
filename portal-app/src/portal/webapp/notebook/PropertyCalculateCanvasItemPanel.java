package portal.webapp.notebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.List;

public class PropertyCalculateCanvasItemPanel extends CanvasItemPanel<PropertyCalculateCell> {
    @Inject
    private NotebooksSession notebooksSession;

    private Form<ModelObject> form;

    public PropertyCalculateCanvasItemPanel(String id, Notebook notebook, PropertyCalculateCell cell) {
        super(id, notebook, cell);
        addHeader();
        addForm();
        addListeners();
        load();
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

    private void addListeners() {
        getNotebook().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(Cell cell) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }

            @Override
            public void onCellAdded(Cell cell) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }
        });
    }

    private void load() {
        form.getModelObject().setInputVariable(getCell().getInputVariable());
        Variable variable = getNotebook().findVariable(getCell().getName(), "outputFileName");
        if (variable != null) {
            form.getModelObject().setOutputFileName((String)variable.getValue());
        }
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        IModel<List<Variable>> dropDownModel = new IModel<List<Variable>>() {
            @Override
            public List<Variable> getObject() {
                List<Variable> list = notebooksSession.listAvailableInputVariablesFor(getCell(), getNotebook());
                return list;
            }

            @Override
            public void setObject(List<Variable> variableList) {

            }

            @Override
            public void detach() {

            }
        };
        DropDownChoice<Variable> inputVariableChoice = new DropDownChoice<Variable>("inputVariable", dropDownModel);
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
        List<MoleculeObject> objects = notebooksSession.retrieveFileContentAsMolecules((String) getCell().getInputVariable().getValue());
        calculateTo(objects, form.getModelObject().getOutputFileName());
        getNotebook().findVariable(getCell().getName(), "outputFileName").setValue(form.getModelObject().getOutputFileName());
        notebooksSession.saveNotebook(getNotebook());
    }

    private void calculateTo(List<MoleculeObject> objects, String fileName) {
        try {
            FileOutputStream outputStream = new FileOutputStream("files/" + fileName);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(outputStream, objects);
                outputStream.flush();
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
