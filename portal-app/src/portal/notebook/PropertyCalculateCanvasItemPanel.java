package portal.notebook;

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
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyCalculateCanvasItemPanel extends CanvasItemPanel<PropertyCalculateCellModel> {
    private static final Logger LOGGER = Logger.getLogger(PropertyCalculateCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private transient CalculatorsClient calculatorsClient;
    private Form<ModelObject> form;

    public PropertyCalculateCanvasItemPanel(String id, PropertyCalculateCellModel cell) {
        super(id, cell);
        addHeader();
        addForm();
        addListeners();
        load();
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

    private void addListeners() {
        notebookSession.getNotebookModel().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }

            @Override
            public void onCellAdded(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }
        });
    }

    private void load() {
        form.getModelObject().setInputVariable(getCellModel().getInputVariableModel());
        VariableModel variableModel = notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "outputFileName");
        if (variableModel != null) {
            form.getModelObject().setOutputFileName((String) variableModel.getValue());
        }
        form.getModelObject().setServiceName(getCellModel().getServiceName());
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
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
        getCellModel().setInputVariableModel(form.getModelObject().getInputVariable());
        getCellModel().setServiceName(form.getModelObject().getServiceName());
        VariableModel outputVariableModel = notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "outputFile");
        outputVariableModel.setValue(form.getModelObject().getOutputFileName());
        calculateTo(getCellModel().getServiceName(), getCellModel().getInputVariableModel().getValue().toString(), outputVariableModel.getValue().toString());
        notebookSession.storeNotebook();
    }

    private void calculateTo(String serviceName, String inputFileName, String outputFileName) {
        try {
            List<MoleculeObject> list = notebookSession.retrieveFileContentAsMolecules(inputFileName);
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
        private VariableModel inputVariable;
        private String serviceName;
        private String outputFileName;

        public VariableModel getInputVariable() {
            return inputVariable;
        }

        public void setInputVariable(VariableModel inputVariable) {
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