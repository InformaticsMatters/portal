package portal.notebook;

import com.vaynberg.wicket.select2.Select2Choice;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toolkit.wicket.semantic.SemanticModalPanel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class ConnectionPanel extends SemanticModalPanel {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPanel.class);
    private Callbacks callbacks;
    private Form<ConnectionPanelData> connectionForm;
    private CellModel sourceCellModel;
    private CellModel targetCellModel;
    private Select2Choice<VariableModel> sourceChoice;
    private Select2Choice<BindingModel> targetChoice;
    private String targetMarkupId;
    private String sourceMarkupId;

    public ConnectionPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
        addBindingList();
    }

    private void addForm() {
        connectionForm = new Form<>("form");
        connectionForm.setOutputMarkupId(true);
        getModalRootComponent().add(connectionForm);
        connectionForm.setModel(new CompoundPropertyModel<>(new ConnectionPanelData()));

        sourceChoice = new Select2Choice<>("source");
        sourceChoice.getSettings().setMinimumInputLength(0);
        sourceChoice.setOutputMarkupId(true);
        sourceChoice.setProvider(new SourceVariableProvider(null));
        connectionForm.add(sourceChoice);

        targetChoice = new Select2Choice<>("target");
        targetChoice.getSettings().setMinimumInputLength(0);
        targetChoice.setOutputMarkupId(true);
        targetChoice.setProvider(new TargetBindingProvider(null));
        connectionForm.add(targetChoice);

        final AjaxSubmitLink submit = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                targetChoice.getModelObject().setVariableModel(sourceChoice.getModelObject());
                callbacks.onSubmit();
            }
        };
        submit.setOutputMarkupId(true);
        connectionForm.add(submit);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        connectionForm.add(cancelAction);
    }

    private void addBindingList() {
        IModel<List<BindingModel>> listModel = new IModel<List<BindingModel>>() {
            @Override
            public void detach() {

            }

            @Override
            public List<BindingModel> getObject() {
                return targetCellModel == null ? new ArrayList<BindingModel>() : new ArrayList<BindingModel>(targetCellModel.getBindingModelMap().values());
            }

            @Override
            public void setObject(List<BindingModel> bindingModels) {

            }

        };
        final WebMarkupContainer bindingListContainer = new WebMarkupContainer("bindingListContainer");
        bindingListContainer.setOutputMarkupId(true);
        ListView<BindingModel> listView = new ListView<BindingModel>("binding", listModel) {

            @Override
            protected void populateItem(ListItem<BindingModel> listItem) {
                final BindingModel bindingModel = listItem.getModelObject();
                listItem.add(new Label("targetName", bindingModel.getDisplayName()));
                VariableModel variableModel = bindingModel.getVariableModel();
                String sourceDisplayName = variableModel == null ? null : variableModel.getDisplayName();
                listItem.add(new Label("variableName", sourceDisplayName));
                listItem.add(new AjaxLink("unassign") {
                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        bindingModel.setVariableModel(null);
                        ajaxRequestTarget.add(bindingListContainer);
                    }
                });
            }
        };
        bindingListContainer.add(listView);
        getModalRootComponent().add(bindingListContainer);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void configure(String sourceMarkupId, CellModel sourceCellModel, String targetMarkupId, CellModel targetCellModel) {
        this.sourceMarkupId = sourceMarkupId;
        this.targetMarkupId = targetMarkupId;
        this.sourceCellModel = sourceCellModel;
        this.targetCellModel = targetCellModel;

        logger.info("Connecting " + sourceCellModel.getName() + " to " + targetCellModel.getName());

        SourceVariableProvider sourceVariableProvider = new SourceVariableProvider(this.sourceCellModel.getOutputVariableModelMap());
        sourceChoice.setProvider(sourceVariableProvider);
        TargetBindingProvider targetBindingProvider = new TargetBindingProvider(this.targetCellModel.getBindingModelMap());
        targetChoice.setProvider(targetBindingProvider);

        connectionForm.setModelObject(new ConnectionPanelData());
    }

    public String getSourceMarkupId() {
        return sourceMarkupId;
    }

    public String getTargetMarkupId() {
        return targetMarkupId;
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onCancel();

    }

    private class ConnectionPanelData implements Serializable {

        private VariableModel source;
        private BindingModel target;

        public VariableModel getSource() {
            return source;
        }

        public void setSource(VariableModel source) {
            this.source = source;
        }

        public BindingModel getTarget() {
            return target;
        }

        public void setTarget(BindingModel target) {
            this.target = target;
        }
    }
}
