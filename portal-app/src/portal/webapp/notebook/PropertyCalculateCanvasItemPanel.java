package portal.webapp.notebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import portal.notebook.Cell;
import portal.notebook.Variable;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyCalculateCanvasItemPanel extends CanvasItemPanel<PropertyCalculateCell> {
    private static final Logger LOGGER = Logger.getLogger(PropertyCalculateCanvasItemPanel.class.getName());
    @Inject
    private NotebooksSession notebooksSession;
    @Inject
    private transient CalculatorsClient calculatorsClient;
    private Form<ModelObject> form;

    public PropertyCalculateCanvasItemPanel(String id, PropertyCalculateCell cell) {
        super(id, cell);
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
                notebooksSession.getNotebookContents().removeCell(getCell());
                notebooksSession.storeNotebook();
            }
        });
    }

    private void addListeners() {
        notebooksSession.getNotebookContents().addNotebookChangeListener(new NotebookChangeListener() {
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
        Variable variable = notebooksSession.getNotebookContents().findVariable(getCell().getName(), "outputFileName");
        if (variable != null) {
            form.getModelObject().setOutputFileName((String) variable.getValue());
        }
        form.getModelObject().setServiceName(getCell().getServiceName());
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        IModel<List<Variable>> inputVariableModel = new IModel<List<Variable>>() {
            @Override
            public List<Variable> getObject() {
                List<Variable> list = notebooksSession.listAvailableInputVariablesFor(getCell(), notebooksSession.getNotebookContents());
                return list;
            }

            @Override
            public void setObject(List<Variable> variableList) {

            }

            @Override
            public void detach() {

            }
        };
        DropDownChoice<Variable> inputVariableChoice = new DropDownChoice<Variable>("inputVariable", inputVariableModel);
        form.add(inputVariableChoice);
        DropDownChoice<String> serviceNameChoice = new DropDownChoice<String>("serviceName", Arrays.asList(CalculatorsClient.getServiceNames()));
        form.add(serviceNameChoice);
        TextField<String> outputFileNameField = new TextField<String>("outputFileName");
        form.add(outputFileNameField);
        IndicatingAjaxSubmitLink calculateLink = new IndicatingAjaxSubmitLink("calculate") {
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
        getCell().setServiceName(form.getModelObject().getServiceName());
        Variable outputVariable = notebooksSession.getNotebookContents().findVariable(getCell().getName(), "outputFileName");
        outputVariable.setValue(form.getModelObject().getOutputFileName());
        calculateTo(getCell().getServiceName(), getCell().getInputVariable().getValue().toString(), outputVariable.getValue().toString());
        notebooksSession.storeNotebook();
    }

    private void calculateTo(String serviceName, String inputFileName, String outputFileName) {
        try {
            List<MoleculeObject> list = notebooksSession.retrieveFileContentAsMolecules(inputFileName);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(byteArrayOutputStream, list);
            byteArrayOutputStream.flush();
            FileOutputStream outputStream = new FileOutputStream("files/" + outputFileName);
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                try {
                    LOGGER.log(Level.INFO, "Calling service...");
                    calculatorsClient.calculate(serviceName, inputStream, outputStream);
                    LOGGER.log(Level.INFO, "Service call finished");
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
        private String serviceName;
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

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }

}
