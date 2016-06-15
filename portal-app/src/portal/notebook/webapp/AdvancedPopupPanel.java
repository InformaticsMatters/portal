package portal.notebook.webapp;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.CellInstance;

/**
 * @author simetrias
 */
public class AdvancedPopupPanel extends Panel {

    private final CellInstance cellInstance;

    public AdvancedPopupPanel(String id, CellInstance cellInstance, Panel advancedOptionsPanel) {
        super(id);
        setOutputMarkupId(true);
        this.cellInstance = cellInstance;
        add(advancedOptionsPanel);
    }

}
