package portal.notebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.dataset.DatasetMetadata;
import portal.PopupContainerProvider;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class ScatterPlotAdvancedOptionsPanel extends Panel {

    private final Long cellId;
    private List<String> picklistItems;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    private Form<ModelObject> form;

    public ScatterPlotAdvancedOptionsPanel(String id, Long cellId) {
        super(id);
        setOutputMarkupId(true);
        this.cellId = cellId;
        loadPicklist();
        addComponents();
    }

    private void addComponents() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new ModelObject()));

        DropDownChoice<String> x = new DropDownChoice<>("x", picklistItems);
        form.add(x);

        DropDownChoice<String> y = new DropDownChoice<>("y", picklistItems);
        form.add(y);

        form.add(new IndicatingAjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
                ModelObject model = form.getModelObject();
                popupContainerProvider.refreshContainer(getPage(), target);
            }
        });
        add(form);
    }

    private void loadPicklist() {
        picklistItems = new ArrayList<>();
        CellInstance cellInstance = notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
        BindingInstance bindingInstance = cellInstance.getBindingInstanceMap().get(CellDefinition.VAR_NAME_INPUT);
        VariableInstance variableInstance = bindingInstance.getVariableInstance();
        if (variableInstance != null) {
            loadFieldNames(variableInstance);
        }
    }

    private void loadFieldNames(VariableInstance variableInstance) {
        try {
            String string = (String) variableInstance.getValue();
            if (string != null) {
                DatasetMetadata datasetMetadata = new ObjectMapper().readValue(string, DatasetMetadata.class);
                picklistItems.addAll(datasetMetadata.getValueClassMappings().keySet());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getXAxisFieldName() {
        return form.getModelObject().getX();
    }

    public String getYAxisFieldName() {
        return form.getModelObject().getY();
    }

    private class ModelObject implements Serializable {

        private String x;
        private String y;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }
    }
}
