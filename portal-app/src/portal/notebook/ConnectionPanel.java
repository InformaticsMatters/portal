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

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private AjaxSubmitLink bindAction;
    private Label sourceLabel;
    private Label targetLabel;
    @Inject
    private NotebookSession notebookSession;
    private boolean dirty;

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

        sourceLabel = new Label("sourceLabel", "Source variable");
        connectionForm.add(sourceLabel);
        targetLabel = new Label("targetLabel", "Target binding");
        connectionForm.add(targetLabel);

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

        bindAction = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (targetChoice.getModelObject() != null) {
                    targetChoice.getModelObject().setVariableModel(sourceChoice.getModelObject());
                }
                callbacks.onSubmit();
            }
        };
        bindAction.setOutputMarkupId(true);
        connectionForm.add(bindAction);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onClose();
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
                return buildBindingModelList();
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
                String sourceDisplayName = variableModel == null ? null : (variableModel.getProducerCellModel().getName() + " " + variableModel.getDisplayName());
                listItem.add(new Label("variableName", sourceDisplayName));
                AjaxLink unassignLink = new AjaxLink("unassign") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        bindingModel.setVariableModel(null);
                        notebookSession.storeCurrentNotebook();
                        ajaxRequestTarget.add(bindingListContainer);
                        dirty = true;
                    }
                };
                unassignLink.setVisible(sourceDisplayName != null);
                listItem.add(unassignLink);
            }
        };
        bindingListContainer.add(listView);
        getModalRootComponent().add(bindingListContainer);
    }

    private List<BindingModel> buildBindingModelList() {
        if (targetCellModel == null) {
            return new ArrayList<BindingModel>();
        } else {
            ArrayList<BindingModel> list = new ArrayList<BindingModel>(targetCellModel.getBindingModelMap().values());
            Collections.sort(list, new Comparator<BindingModel>() {
                @Override
                public int compare(BindingModel o1, BindingModel o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return list;
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void configure(CellModel sourceCellModel, CellModel targetCellModel, boolean canAddBindings) {
        this.sourceCellModel = sourceCellModel;
        this.targetCellModel = targetCellModel;

        if (sourceCellModel != null) {
            logger.info("Connecting " + sourceCellModel.getName() + " to " + targetCellModel.getName());
            SourceVariableProvider sourceVariableProvider = new SourceVariableProvider(this.sourceCellModel.getOutputVariableModelMap());
            sourceChoice.setProvider(sourceVariableProvider);
            TargetBindingProvider targetBindingProvider = new TargetBindingProvider(this.targetCellModel.getBindingModelMap());
            targetChoice.setProvider(targetBindingProvider);
        }

        connectionForm.setModelObject(new ConnectionPanelData());
        dirty = false;

        sourceLabel.setVisible(canAddBindings);
        targetLabel.setVisible(canAddBindings);
        sourceChoice.setVisible(canAddBindings);
        targetChoice.setVisible(canAddBindings);
        bindAction.setVisible(canAddBindings);
    }

    public CellModel getSourceCellModel() {
        return sourceCellModel;
    }

    public CellModel getTargetCellModel() {
        return targetCellModel;
    }

    public boolean isDirty() {
        return dirty;
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onClose();

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
