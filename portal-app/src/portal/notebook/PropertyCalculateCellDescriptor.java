package portal.notebook;


import portal.notebook.api.CellType;

public class PropertyCalculateCellDescriptor implements CellDescriptor {
    @Override
    public CellType getCellType() {
        return CellType.PROPERTY_CALCULATE;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Property calc.";
    }
}
