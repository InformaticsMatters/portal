package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class CellPopupPanel extends Panel {

    private BindingsPanel bindingsPanel;

    public CellPopupPanel(String id) {
        super(id);
        addPanels();
    }

    private void addPanels() {
        bindingsPanel = new BindingsPanel("bindingsPanel");
        add(bindingsPanel);
    }
}
