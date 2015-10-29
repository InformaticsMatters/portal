package portal;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author simetrias
 */
@SessionScoped
public class PopupContainerProvider implements Serializable {

    private Map<Integer, WebMarkupContainer> containersMap = new HashMap<>();
    private Integer lastPageKeyUsed = 0;

    public void createPopupContainerForPage(Page parentPage, String id) {
        Integer pageKey = generateAndAssignPageKey(parentPage);
        WebMarkupContainer popupContainer = new WebMarkupContainer(id);
        popupContainer.setOutputMarkupId(true);
        parentPage.add(popupContainer);
        WebMarkupContainer content = new WebMarkupContainer("content");
        content.setOutputMarkupId(true);
        popupContainer.add(content);
        containersMap.put(pageKey, popupContainer);
    }

    public void setPopupContentForPage(Page parentPage, Panel content) {
        WebMarkupContainer container = containersMap.get(retrievePageKey(parentPage));
        container.addOrReplace(content);
    }

    private Integer generateAndAssignPageKey(Page page) {
        Integer pageKey = getNextPageKey();
        page.setMetaData(PopupContainerProviderMetaDataKey.INSTANCE, pageKey);
        return pageKey;
    }

    private synchronized Integer getNextPageKey() {
        return ++lastPageKeyUsed;
    }

    private Integer retrievePageKey(Page page) {
        return page.getMetaData(PopupContainerProviderMetaDataKey.INSTANCE);
    }

    public void refreshContainer(Page parentPage, AjaxRequestTarget ajaxRequestTarget) {
        WebMarkupContainer container = containersMap.get(retrievePageKey(parentPage));
        ajaxRequestTarget.add(container);
    }
}
