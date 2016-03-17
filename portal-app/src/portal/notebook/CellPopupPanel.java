package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.CellInstance;

/**
 * @author simetrias
 */
public class CellPopupPanel extends Panel {

    private final CellInstance cellInstance;
    private BindingsPanel bindingsPanel;

    public CellPopupPanel(String id, CellInstance cellInstance) {
        super(id);
        this.cellInstance = cellInstance;
        addPanels();
    }

    private void addPanels() {
        bindingsPanel = new BindingsPanel("bindingsPanel", cellInstance);
        add(bindingsPanel);
    }
}
