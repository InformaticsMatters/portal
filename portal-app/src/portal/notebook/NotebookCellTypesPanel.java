package portal.notebook;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.time.Duration;
import portal.notebook.api.CellDefinition;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCellTypesPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "cellDescriptor";

    private Form<SearchCellData> searchForm;

    private WebMarkupContainer definitionsContainer;
    private ListView<CellDefinition> definitionsRepeater;

    @Inject
    private NotebookSession notebookSession;

    public NotebookCellTypesPanel(String id) {
        super(id);
        addSearchForm();
        addCells();
    }

    private void addSearchForm() {
        searchForm = new Form<>("form");
        searchForm.setModel(new CompoundPropertyModel<>(new SearchCellData()));
        searchForm.setOutputMarkupId(true);
        add(searchForm);

        TextField<String> patternField = new TextField<>("pattern");
        patternField.add(new AjaxFormSubmitBehavior(searchForm, "keyup") {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                refreshCells();
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setThrottlingSettings(new ThrottlingSettings(patternField.getMarkupId(), Duration.milliseconds(500), true));
            }
        });
        searchForm.add(patternField);

        AjaxSubmitLink searchAction = new IndicatingAjaxSubmitLink("search") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                refreshCells();
            }
        };
        searchForm.add(searchAction);
    }

    private void addCells() {
        definitionsContainer = new WebMarkupContainer("descriptorsContainer");
        definitionsContainer.setOutputMarkupId(true);

        List<CellDefinition> cells = notebookSession.listCellDefinition(null);
        definitionsRepeater = new ListView<CellDefinition>("descriptor", cells) {

            @Override
            protected void populateItem(ListItem<CellDefinition> listItem) {
                CellDefinition cellType = listItem.getModelObject();
                listItem.add(new NotebookCellTypePanel("descriptorItem", cellType));
                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_ID, cellType.getName()));
            }
        };
        definitionsContainer.add(definitionsRepeater);

        add(definitionsContainer);
    }

    public void refreshCells() {
        CellDefinitionFilterData cellDefinitionFilterData = new CellDefinitionFilterData();
        SearchCellData searchCellData = searchForm.getModelObject();
        cellDefinitionFilterData.setPattern(searchCellData.getPattern());
        definitionsRepeater.setList(notebookSession.listCellDefinition(cellDefinitionFilterData));
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(definitionsContainer);
        }
    }

}
