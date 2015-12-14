package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import portal.notebook.execution.service.CalculatorsClient;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Logger;

public class PropertyCalculateCanvasItemPanel extends CanvasItemPanel {
    private static final Logger LOGGER = Logger.getLogger(PropertyCalculateCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private transient CalculatorsClient calculatorsClient;
    private Form<ModelObject> form;

    public PropertyCalculateCanvasItemPanel(String id, CellModel cell, CallbackHandler callbackHandler) {
        super(id, cell, callbackHandler);
        addForm();
        addListeners();
        load();
        setOutputMarkupId(true);
    }

    private void addListeners() {
        notebookSession.getCurrentNotebookModel().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(CellModel cellModel) {
                if (cellModel != getCellModel()) {
                    for (BindingModel bindingModel : getCellModel().getBindingModelMap().values()) {
                        if (bindingModel.getVariableModel() != null && bindingModel.getVariableModel().getProducerCellModel() == cellModel) {
                            bindingModel.setVariableModel(null);
                        }
                    }
                    RequestCycle.get().find(AjaxRequestTarget.class).add(form);
                }
            }

            @Override
            public void onCellAdded(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }
        });
    }

    private void load() {
        VariableModel outputVariableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "outputFile");
        if (outputVariableModel != null) {
            form.getModelObject().setOutputFileName((String) outputVariableModel.getValue());
        }
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        DropDownChoice<String> serviceNameChoice = new DropDownChoice<String>("serviceName", getCellModel().getOptionModelMap().get("serviceName").getPicklistValueList());
        form.add(serviceNameChoice);
        TextField<String> outputFileNameField = new TextField<String>("outputFileName");
        form.add(outputFileNameField);
        IndicatingAjaxSubmitLink calculateLink = new IndicatingAjaxSubmitLink("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                calculateAndSave();
            }
        };
        add(calculateLink);
        add(form);
    }

    private void calculateAndSave() {
        if (isValidInput()) {
            form.getModelObject().store();
            VariableModel outputVariableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "outputFile");
            outputVariableModel.setValue(form.getModelObject().getOutputFileName());
            notebookSession.storeCurrentNotebook();
            notebookSession.executeCell(getCellModel().getName());
            notebookSession.reloadCurrentNotebook();
        }
    }

    private boolean isValidInput() {
        return getCellModel().getBindingModelMap().get("input").getVariableModel() != null && form.getModelObject().getServiceName() != null && form.getModelObject().getOutputFileName() != null;
    }


    class ModelObject implements Serializable {
        private String serviceName;
        private String outputFileName;

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

        public void load() {
            serviceName = (String) getCellModel().getOptionModelMap().get("serviceName").getValue();
        }

        public void store() {
            getCellModel().getOptionModelMap().get("serviceName").setValue(serviceName);
        }
    }

}
