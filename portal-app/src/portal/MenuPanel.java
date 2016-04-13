package portal;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.squonk.security.UserDetails;
import portal.notebook.webapp.NotebookCanvasPage;

import javax.inject.Inject;

public class MenuPanel extends Panel {

    @Inject
    private SessionContext sessionContext;
    @Inject
    private LogoutHandler logoutHandler;
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
                setResponsePage(NotebookCanvasPage.class);
                add(attributeAppender);
            }
        };
        add(notebookLink);

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
                logoutHandler.logout();
            }
        });

        add(new Label("username", new PropertyModel<String>(this, "getUserDisplayName")));
    }

    public String getUserDisplayName() {
        UserDetails userDetails = sessionContext.getLoggedInUserDetails();
        if (userDetails == null) {
            return "";
        } else {
            return userDetails.getDisplayName();
        }
    }

    public void setLeftSideItemVisible(boolean value) {
        leftSidebarLink.setVisible(value);
    }

}
