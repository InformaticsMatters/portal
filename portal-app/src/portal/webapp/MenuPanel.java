package portal.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import portal.webapp.notebook.NotebookPage;
import portal.webapp.workflow.WorkflowPage;

import javax.inject.Inject;

public class MenuPanel extends Panel {

    @Inject
    private SessionContext sessionContext;
    private AjaxLink leftSidebarLink;

    public MenuPanel(String id) {
        super(id);
        addActions();
    }

    private void addActions() {
        final AttributeAppender attributeAppender = AttributeModifier.append("class", "active");

        AjaxLink homeLink = new AjaxLink("home") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(PortalHomePage.class);
                add(attributeAppender);
            }
        };
        add(homeLink);

        AjaxLink notebookLink = new AjaxLink("notebook") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(NotebookPage.class);
                add(attributeAppender);
            }
        };
        add(notebookLink);

        AjaxLink workflowLink = new AjaxLink("workflow") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(WorkflowPage.class);
                add(attributeAppender);
            }
        };
        add(workflowLink);

        leftSidebarLink = new AjaxLink("leftSidebarLink") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                ajaxRequestTarget.appendJavaScript("leftSideBarToggle()");
            }
        };
        add(leftSidebarLink);
        leftSidebarLink.setVisible(false);

        add(new Link<String>("logout") {

            @Override
            public void onClick() {
                sessionContext.setLoggedInUser(null);
                setResponsePage(getApplication().getHomePage());
            }
        });

        add(new Label("username", new PropertyModel<String>(this, "getUserName")));
    }

    public String getUserName() {
        return sessionContext.getLoggedInUser();
    }

    public void setLeftSideItemVisible(boolean value) {
        leftSidebarLink.setVisible(value);
    }

}
