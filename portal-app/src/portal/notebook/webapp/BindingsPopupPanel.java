package portal.notebook.webapp;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.CellInstance;

/**
 * @author simetrias
 */
public class BindingsPopupPanel extends Panel {

    private final CellInstance cellInstance;

    public BindingsPopupPanel(String id, CellInstance cellInstance) {
        super(id);
        setOutputMarkupId(true);
        this.cellInstance = cellInstance;
        addPanels();
    }

    private void addPanels() {
        BindingsPanel bindingsPanel = new BindingsPanel("bindingsPanel", cellInstance);
        add(bindingsPanel);
    }
}
