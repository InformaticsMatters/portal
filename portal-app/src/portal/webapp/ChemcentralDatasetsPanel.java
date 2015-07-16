package portal.webapp;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.dataset.IDatasetDescriptor;

import java.util.ArrayList;
import java.util.List;

public class ChemcentralDatasetsPanel extends Panel {

    private ListView<IDatasetDescriptor> listView;
    private ChemcentralDatasetPopupPanel clickCardPopup;

    public ChemcentralDatasetsPanel(String id) {
        super(id);
        addCards();
        addClickCardPopup();
    }

    private void addCards() {
        listView = new ListView<IDatasetDescriptor>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<IDatasetDescriptor> listItem) {
                IDatasetDescriptor datasetDescriptor = listItem.getModelObject();
                listItem.add(new Label("description", datasetDescriptor.getDescription()));
                listItem.add(new Label("rowCount", datasetDescriptor.getRowCount()));

                listItem.add(new AjaxEventBehavior("click") {

                    @Override
                    protected void onEvent(AjaxRequestTarget ajaxRequestTarget) {
                        clickCardPopup.setDefaultModelObject(datasetDescriptor);
                        clickCardPopup.setVisible(true);
                        ajaxRequestTarget.add(clickCardPopup);
                        String js = "$('#" + listItem.getMarkupId() + "').popup({popup: '.ui.clickCardPopup.popup', on : 'click'}).popup('toggle')";
                        ajaxRequestTarget.appendJavaScript(js);
                    }
                });
            }
        };
        add(listView);
    }

    private void addClickCardPopup() {
        clickCardPopup = new ChemcentralDatasetPopupPanel("clickCardPopup");
        clickCardPopup.setDefaultModel(new CompoundPropertyModel<>(null));
        clickCardPopup.setVisible(false);
        add(clickCardPopup);
    }

    public void setDatasetDescriptorList(List<IDatasetDescriptor> datasetDescriptorList) {
        listView.setList(datasetDescriptorList);
    }
}

