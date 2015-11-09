package portal.notebook;

import portal.notebook.api.CellType;

public class AddCellDescriptor implements CellDescriptor {

    @Override
    public CellType getCellType() {
        return CellType.ADD;
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
