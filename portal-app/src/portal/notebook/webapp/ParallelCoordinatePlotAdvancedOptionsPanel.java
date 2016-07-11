package portal.notebook.webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.dataset.DatasetMetadata;
import portal.PopupContainerProvider;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class ParallelCoordinatePlotAdvancedOptionsPanel extends Panel {
    private static final Logger LOGGER = Logger.getLogger(ParallelCoordinatePlotAdvancedOptionsPanel.class.getName());
    private final Long cellId;
    private List<String> picklistItems;
    private Form<ModelObject> form;
    private CallbackHandler callbackHandler;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    @Inject
    private NotifierProvider notifierProvider;

    public ParallelCoordinatePlotAdvancedOptionsPanel(String id, Long cellId) {
        super(id);
        setOutputMarkupId(true);
        this.cellId = cellId;
        try {
            loadPicklist();
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Error loading picklist", t);
            // TODO
        }
        addComponents();
    }

    private void addComponents() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new ModelObject()));

        ListMultipleChoice<String> fields = new ListMultipleChoice<>("fields", picklistItems);
        fields.setMaxRows(10);
        form.add(fields);

        add(form);

        form.add(new IndicatingAjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
                try {
                    if (callbackHandler != null) {
                        callbackHandler.onApplyAdvancedOptions();
                    }
                    popupContainerProvider.refreshContainer(getPage(), target);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Error storing notebook", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });
    }

    private void loadPicklist() throws Exception {
        picklistItems = new ArrayList<>();
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        VariableInstance variableInstance = bindingInstance.getVariableInstance();
        if (variableInstance != null) {
            loadFieldNames(variableInstance);
        }
    }

    private void loadFieldNames(VariableInstance variableInstance) throws Exception {
        String string = notebookSession.readTextValue(variableInstance);
        if (string != null) {
            DatasetMetadata datasetMetadata = new ObjectMapper().readValue(string, DatasetMetadata.class);
            picklistItems.addAll(datasetMetadata.getValueClassMappings().keySet());
        }
    }

    public List<String> getFields() {
        return form.getModelObject().getFields();
    }

    public void setFields(List<String> fields) {
        form.getModelObject().setFields(fields);
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public interface CallbackHandler extends Serializable {

        void onApplyAdvancedOptions() throws Exception;

    }

    private class ModelObject implements Serializable {

        private List<String> fields;

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }

    }
}
