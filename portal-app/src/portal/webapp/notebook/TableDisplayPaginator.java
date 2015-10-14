package portal.webapp.notebook;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigation;

/**
 * @author simetrias
 */
public class TableDisplayPaginator extends AjaxPagingNavigation {

    private final TableDisplayVisualizer tableDisplayVisualizer;

    public TableDisplayPaginator(String id, TableDisplayVisualizer tableDisplayVisualizer) {
        super(id, tableDisplayVisualizer);
        this.tableDisplayVisualizer = tableDisplayVisualizer;
    }


}
