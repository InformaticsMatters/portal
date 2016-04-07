package portal.notebook.webapp;

import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class AdvancedPopupPanel extends Panel {

    private final BindingsPanel.CellInstance cellInstance;
    private BindingsPanel bindingsPanel;

    public AdvancedPopupPanel(String id, BindingsPanel.CellInstance cellInstance, Panel advancedOptionsPanel) {
        super(id);
        setOutputMarkupId(true);
        this.cellInstance = cellInstance;
        add(advancedOptionsPanel);
    }

}
