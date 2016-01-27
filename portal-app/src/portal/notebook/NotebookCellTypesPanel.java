package portal.notebook;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.notebook.api.CellType;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookCellTypesPanel extends Panel {

    public static final String DROP_DATA_TYPE_VALUE = "cellDescriptor";

    private Form<SearchCellData> searchForm;

    private WebMarkupContainer descriptorssContainer;
    private ListView<CellType> descriptorRepeater;

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
        descriptorssContainer = new WebMarkupContainer("descriptorsContainer");
        descriptorssContainer.setOutputMarkupId(true);

        List<CellType> cells = notebookSession.listCellType();
        descriptorRepeater = new ListView<CellType>("descriptor", cells) {

            @Override
            protected void populateItem(ListItem<CellType> listItem) {
                CellType cellType = listItem.getModelObject();
                listItem.add(new NotebookCellTypePanel("descriptorItem", cellType));
                listItem.setOutputMarkupId(true);
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_TYPE, DROP_DATA_TYPE_VALUE));
                listItem.add(new AttributeModifier(NotebookCanvasPage.DROP_DATA_ID, cellType.getName()));
            }
        };
        descriptorssContainer.add(descriptorRepeater);

        add(descriptorssContainer);
    }

    public void refreshCells() {
        CellFilterData cellFilterData = new CellFilterData();
        SearchCellData searchCellData = searchForm.getModelObject();
        cellFilterData.setPattern(searchCellData.getPattern());
        descriptorRepeater.setList(notebookSession.listCellType());
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(descriptorssContainer);
        }
    }

}
