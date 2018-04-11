package portal.notebook.webapp.results;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import portal.notebook.webapp.AbstractCellDatasetProvider;
import portal.notebook.webapp.CellOutputDatasetProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by timbo on 27/08/2016.
 */
public class DatasetDetailsPanel extends Panel {

    private final IModel<DatasetMetadata> datasetMetadataModel;
    private Class<? extends BasicObject> datasetType;

    private final List<ResultsPanelProvider> panelProviders = new ArrayList<>();
    private final RepeatingView tabsRepeater;
    private final RepeatingView viewersRepeater;


    public DatasetDetailsPanel(String id, CellOutputDatasetProvider cellDatasetProvider) {
        this(id, cellDatasetProvider, Collections.emptyList());
    }

    public DatasetDetailsPanel(String id, AbstractCellDatasetProvider cellDatasetProvider, List<ResultsPanelProvider> firstPanels) {
        super(id);
        this.datasetMetadataModel = new CompoundPropertyModel<>((DatasetMetadata) null);

        // first add the externally specified panels
        panelProviders.addAll(firstPanels);
        // now add the results panel
        panelProviders.add(new ResultsPanelProvider("results", "Results") {
            @Override
            public MarkupContainer createPanel(int index, Class dataType) {
                return new DatasetResultsPanel(String.valueOf(index), datasetMetadataModel, cellDatasetProvider);
            }
        });
        // now add the metadata panel
        panelProviders.add(new ResultsPanelProvider("metadata", "Metadata") {
            @Override
            public MarkupContainer createPanel(int index, Class dataType) {
                return new DatasetMetadataPanel(String.valueOf(index), datasetMetadataModel);
            }
        });
        // and finally the export panel
        panelProviders.add(new ResultsPanelProvider("export", "Export") {
            @Override
            public MarkupContainer createPanel(int index, Class dataType) {
                if (dataType == MoleculeObject.class) {
                    return new MoleculeObjectExportPanel(String.valueOf(index), cellDatasetProvider);
                } else if (dataType == BasicObject.class) {
                    return new BasicObjectExportPanel(String.valueOf(index), cellDatasetProvider);
                } else {
                    return new WebMarkupContainer(String.valueOf(index));
                }
            }
        });

        tabsRepeater = new RepeatingView("tabs");
        viewersRepeater = new RepeatingView("viewers");
        add(tabsRepeater);
        add(viewersRepeater);

        createTabs();
        addDummyContent();
    }


    public <T extends BasicObject> boolean prepare(DatasetMetadata<T> meta) throws Exception {
        if (meta == null) {
            addDummyContent();
            datasetType = null;
            return false;
        }
        datasetMetadataModel.setObject(meta);
        if (datasetType == null || datasetType != meta.getType()) {
            // first time through or when the dataset type has changed
            datasetType = meta.getType();
            addRealContent();
        }
        Component c = viewersRepeater.get("0");
        if (c != null && c instanceof DatasetResultsPanel) {
            DatasetResultsPanel rp = (DatasetResultsPanel) c;
            rp.reload();
        }
        return true;
    }

    private void createTabs() {
        // <a class="active item" data-tab="results">Results</a>
        for (int i=0; i<panelProviders.size(); i++) {
            ResultsPanelProvider pp = panelProviders.get(i);
            Component c = new WebMarkupContainer(tabsRepeater.newChildId())
                    .add(new Label("title", pp.getName()))
                    .add(new AttributeModifier("data-tab", pp.getId()));
            if (i == 0) {
                c.add(AttributeModifier.append("class", "active"));
            }
            tabsRepeater.add(c);
        }
    }

    private void addRealContent() {

        viewersRepeater.removeAll();
        // <div wicket:id="results" class="ui bottom attached active tab segment" data-tab="results">
        for (int i=0; i<panelProviders.size(); i++) {
            ResultsPanelProvider pp = panelProviders.get(i);
            Component c = pp.createPanel(i, datasetType)
                    .add(new AttributeModifier("data-tab", pp.getId()));
            if (i == 0) {
                c.add(AttributeModifier.append("class", "active"));
            }

            viewersRepeater.add(c);
        }
    }

    private void addDummyContent() {

        viewersRepeater.removeAll();
        // <div wicket:id="results" class="ui bottom attached active tab segment" data-tab="results">
        for (int i=0; i<panelProviders.size(); i++) {
            ResultsPanelProvider pp = panelProviders.get(i);
            viewersRepeater.add(new WebMarkupContainer(String.valueOf(i))
                    .add(new AttributeModifier("data-tab", pp.getId())));
        }

    }

}