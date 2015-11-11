package portal.notebook;

import portal.notebook.api.CellType;

public class Sample2CellDescriptor implements CellDescriptor {

    @Override
    public CellType getCellType() {
        return CellType.SAMPLE2;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Integer producer";
    }
}
