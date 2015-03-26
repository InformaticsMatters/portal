package portal.webapp;

import com.vaynberg.wicket.select2.Select2Choice;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.cdi.CdiContainer;
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
        form.setModel(new CompoundPropertyModel<>(new DatamartSearchData()));
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);

        PropertyDefinitionProvider propertyDefinitionProvider = new PropertyDefinitionProvider();
        CdiContainer.get().getNonContextualManager().postConstruct(propertyDefinitionProvider);

        Select2Choice<PropertyDefinition> propertyDefinition = new Select2Choice<>("propertyDefinition");
        propertyDefinition.setProvider(propertyDefinitionProvider);
        propertyDefinition.getSettings().setMinimumInputLength(4);
        propertyDefinition.setOutputMarkupId(true);
        form.add(propertyDefinition);

        TextField<String> descriptionField = new TextField<>("description");
        form.add(descriptionField);

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
        private PropertyDefinition propertyDefinition;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public PropertyDefinition getPropertyDefinition() {
            return propertyDefinition;
        }

        public void setPropertyDefinition(PropertyDefinition propertyDefinition) {
            this.propertyDefinition = propertyDefinition;
        }
    }
}

