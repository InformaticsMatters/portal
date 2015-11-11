package portal.notebook;

import portal.notebook.api.CellType;

public class Sample1CellDescriptor implements CellDescriptor {

    @Override
    public CellType getCellType() {
        return CellType.SAMPLE1;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Add a + b";
    }
}
