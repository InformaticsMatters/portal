package portal.webapp;

import com.vaynberg.wicket.select2.Select2Choice;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.integration.PropertyDefinition;
import portal.service.api.ChemcentralSearch;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.SemanticModalPanel;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author simetrias
 */
public class ChemcentralSearchPanel extends SemanticModalPanel {

    private Callbacks callbacks;
    private Form<DatamartSearchData> form;
    @Inject
    private DatasetService datasetService;

    public ChemcentralSearchPanel(String id, String modalElement) {
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

        PropertyDefinitionProvider propertyDefinitionProvider = new PropertyDefinitionProvider();

        Select2Choice<PropertyDefinition> propertyDefinitionSelect2Choice = new Select2Choice<>("propertyDefinition");
        propertyDefinitionSelect2Choice.setProvider(propertyDefinitionProvider);
        propertyDefinitionSelect2Choice.getSettings().setMinimumInputLength(1);
        propertyDefinitionSelect2Choice.setOutputMarkupId(true);
        form.add(propertyDefinitionSelect2Choice);

        final AjaxSubmitLink submit = new IndicatingAjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                ChemcentralSearch chemcentralSearch = new ChemcentralSearch();
                chemcentralSearch.setDescription(ChemcentralSearchPanel.this.form.getModelObject().getDescription());
                datasetService.createFromChemcentralSearch(chemcentralSearch);
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

