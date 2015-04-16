package portal.webapp;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import portal.service.api.DatasetService;
import toolkit.wicket.semantic.NotifierProvider;
import toolkit.wicket.semantic.SemanticResourceReference;

import javax.inject.Inject;

public class DrugWorkbenchPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;

    public DrugWorkbenchPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
    }


}
