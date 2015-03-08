package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.service.api.DatamartSearch;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.SemanticModalPanel;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class DatamartSearchPanel extends SemanticModalPanel {

    private Callbacks callbacks;
    private Form<DatamartSearchData> form;
    @Inject
    private DatasetService datasetService;

    public DatamartSearchPanel(String id, String modalElement) {
        super(id, modalElement);
        addForm();
    }

    private void addForm() {
        form = new Form<>("form");
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);

        form.setModel(new CompoundPropertyModel<>(new DatamartSearchData()));
        TextField<String> descriptionField = new TextField<>("description");
        form.add(descriptionField);

        final AjaxSubmitLink submit = new IndicatingAjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                DatamartSearch datamartSearch = new DatamartSearch();
                datamartSearch.setDescription(DatamartSearchPanel.this.form.getModelObject().getDescription());
                datasetService.createFromDatamartSearch(datamartSearch);
                callbacks.onSubmit();
                hideModal();
            }
        };
        submit.setOutputMarkupId(true);
        form.add(submit);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        form.add(cancelAction);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onCancel();

    }

    private class DatamartSearchData implements Serializable {

        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

