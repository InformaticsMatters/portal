package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;
import org.squonk.notebook.api.CellInstance;

/**
 * @author simetrias
 */
public class AdvancedPopupPanel extends Panel {

    private final CellInstance cellInstance;
    private BindingsPanel bindingsPanel;

    public AdvancedPopupPanel(String id, CellInstance cellInstance, Panel advancedOptionsPanel) {
        super(id);
        setOutputMarkupId(true);
        this.cellInstance = cellInstance;
        add(advancedOptionsPanel);
    }

}
