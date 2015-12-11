package portal.notebook;

import com.vaynberg.wicket.select2.Select2Choice;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import toolkit.wicket.semantic.SemanticModalPanel;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ConnectionPanel extends SemanticModalPanel {

    private Callbacks callbacks;
    private Form<ConnectionPanelData> connectionForm;
    private CellModel sourceCellModel;
    private CellModel targetCellModel;
    private Select2Choice<VariableModel> outputChoice;
    private Select2Choice<BindingModel> inputChoice;

    public ConnectionPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {
        connectionForm = new Form<>("form");
        connectionForm.setOutputMarkupId(true);
        getModalRootComponent().add(connectionForm);
        connectionForm.setModel(new CompoundPropertyModel<>(new ConnectionPanelData()));

        outputChoice = new Select2Choice<>("output");
        outputChoice.getSettings().setMinimumInputLength(0);
        outputChoice.getSettings().setAllowClear(true);
        outputChoice.setOutputMarkupId(true);
        outputChoice.setProvider(new OutputProvider(null));
        connectionForm.add(outputChoice);

        inputChoice = new Select2Choice<>("input");
        inputChoice.getSettings().setMinimumInputLength(0);
        inputChoice.getSettings().setAllowClear(true);
        inputChoice.setOutputMarkupId(true);
        inputChoice.setProvider(new InputProvider());
        connectionForm.add(inputChoice);

        final AjaxSubmitLink submit = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

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

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setSourceAndTargetModels(CellModel sourceCellModel, CellModel targetCellModel) {
        this.sourceCellModel = sourceCellModel;
        this.targetCellModel = targetCellModel;
        configureLists();
    }

    private void configureLists() {
        OutputProvider outputProvider = new OutputProvider(sourceCellModel.getOutputVariableModelMap());
        outputChoice.setProvider(outputProvider);
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onCancel();

    }

    private class ConnectionPanelData implements Serializable {
        private BindingModel input;
        private VariableModel output;

        public BindingModel getInput() {
            return input;
        }

        public void setInput(BindingModel input) {
            this.input = input;
        }

        public VariableModel getOutput() {
            return output;
        }

        public void setOutput(VariableModel output) {
            this.output = output;
        }


    }

}
