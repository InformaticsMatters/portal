package portal.notebook;

import org.apache.wicket.markup.html.panel.Panel;
import portal.notebook.api.OptionInstance;


public abstract class OptionEditorPanel extends Panel {


    private final OptionInstance optionInstance;

    public OptionEditorPanel(String id, OptionInstance optionInstance) {
        super(id);
        this.optionInstance = optionInstance;
    }

    public abstract void store(OptionInstance optionInstance);

    public OptionInstance getOptionInstance() {
        return optionInstance;
    }
}
