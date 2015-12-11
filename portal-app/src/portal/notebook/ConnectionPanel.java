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

    private Select2Choice<VariableModel> outputSelect2Choice;
    private OutputProvider outputProvider = new OutputProvider();
    private Select2Choice<BindingModel> inputSelect2Choice;
    private InputProvider inputProvider = new InputProvider();

    public ConnectionPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {
        connectionForm = new Form<>("form");
        connectionForm.setOutputMarkupId(true);
        getModalRootComponent().add(connectionForm);
        connectionForm.setModel(new CompoundPropertyModel<>(new ConnectionPanelData()));

        outputSelect2Choice = new Select2Choice<VariableModel>("output");
        outputSelect2Choice.setProvider(outputProvider);
        outputSelect2Choice.getSettings().setMinimumInputLength(1);
        outputSelect2Choice.getSettings().setAllowClear(true);
        outputSelect2Choice.setOutputMarkupId(true);
        connectionForm.add(outputSelect2Choice);

        inputSelect2Choice = new Select2Choice<BindingModel>("input");
        inputSelect2Choice.setProvider(inputProvider);
        inputSelect2Choice.getSettings().setMinimumInputLength(1);
        inputSelect2Choice.getSettings().setAllowClear(true);
        inputSelect2Choice.setOutputMarkupId(true);
        connectionForm.add(inputSelect2Choice);

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
