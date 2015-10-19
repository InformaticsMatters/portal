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
import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyCalculateCanvasItemPanel extends CanvasItemPanel<PropertyCalculateCell> {
    private static final Logger LOGGER = Logger.getLogger(PropertyCalculateCanvasItemPanel.class.getName());
    @Inject
    private NotebooksSession notebooksSession;
    @Inject
    private transient CalculatorsClient calculatorsClient;

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
            form.getModelObject().setOutputFileName((String) variable.getValue());
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
        Variable outputVariable = getNotebook().findVariable(getCell().getName(), "outputFileName");
        outputVariable.setValue(form.getModelObject().getOutputFileName());
        calculateTo(getCell().getInputVariable().getValue().toString(), outputVariable.getValue().toString());
        notebooksSession.saveNotebook(getNotebook());
    }

    private void calculateTo(String inputFileName, String outputFileName) {
        try {
            List<MoleculeObject> list = notebooksSession.retrieveFileContentAsMolecules(inputFileName);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(byteArrayOutputStream, list);
            byteArrayOutputStream.flush();
            System.out.write(byteArrayOutputStream.toByteArray());
            System.out.println();
            FileOutputStream outputStream = new FileOutputStream("files/" + outputFileName);
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                //FileInputStream inputStream = new FileInputStream("files/" + inputFileName);
                try {
                    calculatorsClient.calculate("rings", inputStream, outputStream);
                } catch (Throwable t) {
                    outputStream.write("[]".getBytes());
                    LOGGER.log(Level.WARNING, "Error executing calculator", t);
                }
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
