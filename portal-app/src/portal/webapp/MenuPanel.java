package portal.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

public class MenuPanel extends Panel {

    public MenuPanel(String id) {
        super(id);
        addActions();
    }

    private void addActions() {
        AjaxLink homeLink = new AjaxLink("home") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(PortalHomePage.class);
            }
        };
        add(homeLink);

        AjaxLink metadataLink = new AjaxLink("metadata") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(MetadataPage.class);
            }
        };
        add(metadataLink);

        AjaxLink workbenchLink = new AjaxLink("workbench") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(DrugWorkbenchPage.class);
            }
        };
        add(workbenchLink);

        AjaxLink workflowLink = new AjaxLink("workflow") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(WorkflowPage.class);
            }
        };
        add(workflowLink);

        add(new Link<String>("logout") {

            @Override
            public void onClick() {
            }
        });

        add(new Label("username", new PropertyModel<String>(this, "getUserName")));
    }

    public String getUserName() {
        return "User Name";
    }
}
