package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.chemcentral.ChemcentralSession;
import portal.dataset.IDatasetDescriptor;
import toolkit.wicket.marvin4js.MarvinSketcher;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.List;

public class ChemcentralPage extends WebPage {

    protected DatasetCardViewPanel datasetCardViewPanel;
    private AjaxLink gridViewLink;
    private AjaxLink cardViewLink;
    private DatasetListViewPanel datasetListViewPanel;
    private ChemcentralSearchPanel chemcentralSearchPanel;
    private UploadModalPanel uploadModalPanel;
    private MarvinSketcher marvinSketcherPanel;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private ChemcentralSession chemcentralSession;

    public ChemcentralPage() {
        notifierProvider.createNotifier(this, "notifier");
        addPanels();
        addActions();
        addModals();
        refreshDatasetDescriptors();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(WorkflowPage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/lac.js")));
    }

    private void addActions() {
        gridViewLink = new AjaxLink("datasetListView") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                datasetListViewPanel.setVisible(true);
                datasetCardViewPanel.setVisible(false);
                ajaxRequestTarget.add(datasetListViewPanel);
                ajaxRequestTarget.add(datasetCardViewPanel);
                ajaxRequestTarget.add(cardViewLink);
                ajaxRequestTarget.add(gridViewLink);
                ajaxRequestTarget.appendJavaScript("makeMenuButtonActive('" + gridViewLink.getMarkupId() + "')");
            }
        };
        gridViewLink.setOutputMarkupId(true);
        add(gridViewLink);

        cardViewLink = new AjaxLink("datasetCardView") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                datasetCardViewPanel.setVisible(true);
                datasetListViewPanel.setVisible(false);
                ajaxRequestTarget.add(datasetCardViewPanel);
                ajaxRequestTarget.add(datasetListViewPanel);
                ajaxRequestTarget.add(cardViewLink);
                ajaxRequestTarget.add(gridViewLink);
                ajaxRequestTarget.appendJavaScript("makeMenuButtonActive('" + cardViewLink.getMarkupId() + "')");
            }
        };
        cardViewLink.setOutputMarkupId(true);
        add(cardViewLink);


        add(new AjaxLink("addFromFile") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                uploadModalPanel.showModal();
            }
        });

        add(new AjaxLink("addFromStructureSearch") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                marvinSketcherPanel.showModal();
            }
        });

        add(new AjaxLink("addFromChemcentral") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                chemcentralSearchPanel.showModal();
            }
        });
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));
        add(new FooterPanel("footerPanel"));

        datasetListViewPanel = new DatasetListViewPanel("datasetListViewPanel");
        datasetListViewPanel.setOutputMarkupId(true);
        datasetListViewPanel.setOutputMarkupPlaceholderTag(true);
        add(datasetListViewPanel);
        datasetListViewPanel.setVisible(false);

        datasetCardViewPanel = new DatasetCardViewPanel("datasetCardViewPanel");
        datasetCardViewPanel.setOutputMarkupId(true);
        datasetCardViewPanel.setOutputMarkupPlaceholderTag(true);
        add(datasetCardViewPanel);
    }

    private void addModals() {
        uploadModalPanel = new UploadModalPanel("uploadFilePanel", "modalElement");
        uploadModalPanel.setCallbacks(new UploadModalPanel.Callbacks() {

            @Override
            public void onSubmit() {
                refreshDatasetDescriptors();
            }

            @Override
            public void onCancel() {
            }
        });
        add(uploadModalPanel);

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", "modalElement");
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
                marvinSketcherPanel.hideModal();
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);

        chemcentralSearchPanel = new ChemcentralSearchPanel("chemcentralSearchPanel", "modalElement");
        chemcentralSearchPanel.setCallbacks(new ChemcentralSearchPanel.Callbacks() {

            @Override
            public void onSubmit() {
                refreshDatasetDescriptors();
            }

            @Override
            public void onCancel() {

            }
        });
        add(chemcentralSearchPanel);
    }

    private void refreshDatasetDescriptors() {
        List<IDatasetDescriptor> datasetDescriptorList = chemcentralSession.listDatasets(null);
        datasetListViewPanel.setDatasetDescriptorList(datasetDescriptorList);
        datasetCardViewPanel.setDatasetDescriptorList(datasetDescriptorList);
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(datasetListViewPanel);
            target.add(datasetCardViewPanel);
        }
    }
}
