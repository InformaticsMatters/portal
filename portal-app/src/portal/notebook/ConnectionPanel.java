package portal.notebook;

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

    public ConnectionPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {
        connectionForm = new Form<>("form");
        connectionForm.setOutputMarkupId(true);
        getModalRootComponent().add(connectionForm);
        connectionForm.setModel(new CompoundPropertyModel<>(new ConnectionPanelData()));

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


    }

}
