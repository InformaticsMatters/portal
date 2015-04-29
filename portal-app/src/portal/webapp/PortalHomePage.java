package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import portal.integration.DatamartSession;
import portal.service.api.DatasetDescriptor;
import portal.service.api.DatasetService;
import toolkit.wicket.marvin4js.MarvinSketcher;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;
import java.util.List;

public class PortalHomePage extends WebPage {

    //private DatasetCardViewPanel datasetCardViewPanel;
    protected DatasetCardView2Panel datasetCardView2Panel;
    private AjaxLink gridViewLink;
    private AjaxLink cardViewLink;
    private DatasetGridViewPanel datasetGridViewPanel;
    private ChemcentralSearchPanel chemcentralSearchPanel;
    private UploadModalPanel uploadModalPanel;
    private MarvinSketcher marvinSketcherPanel;

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService datasetService;
    @Inject
    private DatamartSession datamartSession;

    public PortalHomePage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addPanels();
        addActions();
        addModals();
        datamartSession.loadDatamartDatasetList();
        refreshDatasetDescriptors();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(PortalWebApplication.class, "resources/lac.js")));
    }

    private void addActions() {
        gridViewLink = new AjaxLink("datasetGridView") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                datasetGridViewPanel.setVisible(true);
                //datasetCardViewPanel.setVisible(false);
                datasetCardView2Panel.setVisible(false);
                ajaxRequestTarget.add(datasetGridViewPanel);
                //ajaxRequestTarget.add(datasetCardViewPanel);
                ajaxRequestTarget.add(datasetCardView2Panel);
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
                //datasetCardViewPanel.setVisible(true);
                datasetCardView2Panel.setVisible(true);
                datasetGridViewPanel.setVisible(false);
                //ajaxRequestTarget.add(datasetCardViewPanel);
                ajaxRequestTarget.add(datasetCardView2Panel);
                ajaxRequestTarget.add(datasetGridViewPanel);
                ajaxRequestTarget.add(cardViewLink);
                ajaxRequestTarget.add(gridViewLink);
                ajaxRequestTarget.appendJavaScript("makeMenuButtonActive('" + cardViewLink.getMarkupId() + "')");
                ajaxRequestTarget.appendJavaScript("cardPopup()");
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
        datasetGridViewPanel = new DatasetGridViewPanel("datasetGridViewPanel");
        datasetGridViewPanel.setOutputMarkupId(true);
        datasetGridViewPanel.setOutputMarkupPlaceholderTag(true);
        add(datasetGridViewPanel);

        /*datasetCardViewPanel = new DatasetCardViewPanel("datasetCardViewPanel");
        datasetCardViewPanel.setOutputMarkupId(true);
        datasetCardViewPanel.setOutputMarkupPlaceholderTag(true);
        add(datasetCardViewPanel);
        datasetCardViewPanel.setVisible(false);*/

        datasetCardView2Panel = new DatasetCardView2Panel("datasetCardViewPanel");
        datasetCardView2Panel.setOutputMarkupId(true);
        datasetCardView2Panel.setOutputMarkupPlaceholderTag(true);
        add(datasetCardView2Panel);
        datasetCardView2Panel.setVisible(false);
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
        // List<DatasetDescriptor> datasetDescriptorList = datasetService.listDatasetDescriptor(new ListDatasetDescriptorFilter());
        List<DatasetDescriptor> datasetDescriptorList = datamartSession.getDatasetDescriptorList();
        datasetGridViewPanel.setDatasetDescriptorList(datasetDescriptorList);
        //datasetCardViewPanel.setDatasetDescriptorList(datasetDescriptorList);
        datasetCardView2Panel.setDatasetDescriptorList(datasetDescriptorList);
        AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(datasetGridViewPanel);
            //target.add(datasetCardViewPanel);
            target.add(datasetCardView2Panel);
        }
    }
}
