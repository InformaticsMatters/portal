package portal.webapp;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.service.api.DatasetDescriptor;

import java.util.ArrayList;
import java.util.List;

public class DatasetCardViewPanel extends Panel {

    private List<DatasetDescriptor> datasetDescriptorList;
    private ListView<DatasetDescriptor> listView;
    private ClickCardPopupPanel clickCardPopup;

    public DatasetCardViewPanel(String id) {
        super(id);
        datasetDescriptorList = new ArrayList<>();
        addCards();
        addClickCardPopup();
    }

    private void addCards() {
        listView = new ListView<DatasetDescriptor>("descriptors", new ArrayList<>()) {

            @Override
            protected void populateItem(ListItem<DatasetDescriptor> listItem) {
                DatasetDescriptor datasetDescriptor = listItem.getModelObject();
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
        clickCardPopup = new ClickCardPopupPanel("clickCardPopup");
        clickCardPopup.setDefaultModel(new CompoundPropertyModel<>(null));
        clickCardPopup.setVisible(false);
        add(clickCardPopup);
    }

    public void setDatasetDescriptorList(List<DatasetDescriptor> datasetDescriptorList) {
        this.datasetDescriptorList = datasetDescriptorList;
        listView.setList(datasetDescriptorList);
    }
}

