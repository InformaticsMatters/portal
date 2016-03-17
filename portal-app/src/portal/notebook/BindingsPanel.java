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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.notebook.api.BindingInstance;
import portal.notebook.api.CellInstance;
import portal.notebook.api.VariableInstance;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author simetrias
 */
public class BindingsPanel extends Panel {

    private static final Logger logger = LoggerFactory.getLogger(BindingsPanel.class);
    private Callbacks callbacks;
    private Form<ConnectionPanelData> connectionForm;
    private CellInstance sourceCellInstance;
    private CellInstance targetCellInstance;
    private Select2Choice<VariableInstance> sourceChoice;
    private Select2Choice<BindingInstance> targetChoice;
    private AjaxSubmitLink bindAction;
    private Label sourceLabel;
    private Label targetLabel;
    @Inject
    private NotebookSession notebookSession;
    private boolean dirty;

    public BindingsPanel(String id) {
        super(id);
        addBindingList();
    }

    private void addBindingList() {
        IModel<List<BindingInstance>> listModel = new IModel<List<BindingInstance>>() {

            @Override
            public void detach() {

            }

            @Override
            public List<BindingInstance> getObject() {
                return buildBindingInstanceList();
            }

            @Override
            public void setObject(List<BindingInstance> bindingModels) {

            }

        };
        final WebMarkupContainer bindingListContainer = new WebMarkupContainer("bindingListContainer");
        bindingListContainer.setOutputMarkupId(true);
        ListView<BindingInstance> listView = new ListView<BindingInstance>("binding", listModel) {

            @Override
            protected void populateItem(ListItem<BindingInstance> listItem) {
                final BindingInstance bindingModel = listItem.getModelObject();
                listItem.add(new Label("targetName", bindingModel.getDisplayName()));
                VariableInstance variableInstance = bindingModel.getVariable();
                String sourceDisplayName = resolveDisplayNameFor(variableInstance);
                listItem.add(new Label("variableName", sourceDisplayName));
                AjaxLink unassignLink = new AjaxLink("unassign") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        bindingModel.setVariable(null);
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
        add(bindingListContainer);
    }

    private String resolveDisplayNameFor(VariableInstance variableInstance) {
        if (variableInstance == null) {
            return null;
        }
        CellInstance producerCellInstance = notebookSession.getCurrentNotebookInstance().findCellById(variableInstance.getCellId());
        return producerCellInstance.getName() + " " + variableInstance.getVariableDefinition().getDisplayName();
    }

    private List<BindingInstance> buildBindingInstanceList() {
        if (targetCellInstance == null) {
            return new ArrayList<BindingInstance>();
        } else {
            ArrayList<BindingInstance> list = new ArrayList<BindingInstance>(targetCellInstance.getBindingMap().values());
            Collections.sort(list, new Comparator<BindingInstance>() {
                @Override
                public int compare(BindingInstance o1, BindingInstance o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            return list;
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void configure(CellInstance sourceCellInstance, CellInstance targetCellInstance, boolean canAddBindings) {
        this.sourceCellInstance = sourceCellInstance;
        this.targetCellInstance = targetCellInstance;

        if (sourceCellInstance != null) {
            logger.info("Connecting " + sourceCellInstance.getName() + " to " + targetCellInstance.getName());
            SourceVariableProvider sourceVariableProvider = new SourceVariableProvider(this.sourceCellInstance.getOutputVariableMap());
            sourceChoice.setProvider(sourceVariableProvider);
            TargetBindingProvider targetBindingProvider = new TargetBindingProvider(this.targetCellInstance.getBindingMap());
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

    public CellInstance getSourceCellInstance() {
        return sourceCellInstance;
    }

    public CellInstance getTargetCellInstance() {
        return targetCellInstance;
    }

    public boolean isDirty() {
        return dirty;
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onClose();

    }

    private class ConnectionPanelData implements Serializable {

        private VariableInstance source;
        private BindingInstance target;

        public VariableInstance getSource() {
            return source;
        }

        public void setSource(VariableInstance source) {
            this.source = source;
        }

        public BindingInstance getTarget() {
            return target;
        }

        public void setTarget(BindingInstance target) {
            this.target = target;
        }
    }
}
