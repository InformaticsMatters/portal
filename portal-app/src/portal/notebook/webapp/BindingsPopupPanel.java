package portal.notebook.webapp;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class BindingsPopupPanel extends Panel {

    private final BindingsPanel.CellInstance cellInstance;

    public BindingsPopupPanel(String id, BindingsPanel.CellInstance cellInstance) {
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
