package portal.notebook;

import portal.notebook.api.OptionInstance;

import java.io.Serializable;

public class OptionFieldEditorModel extends FieldEditorModel {
    private final OptionInstance optionInstance;

    public OptionFieldEditorModel(OptionInstance optionInstance) {
        this.optionInstance = optionInstance;
        setValue(optionInstance.getValue());
    }

    @Override
    String getDisplayName() {
        return optionInstance.getOptionDescriptor().getDisplayName();
    }

    public OptionInstance getOptionInstance() {
        return optionInstance;
    }

}
