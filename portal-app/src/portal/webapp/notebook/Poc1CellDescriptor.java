package portal.webapp.notebook;

public class Poc1CellDescriptor implements CellDescriptor {
    private String varName;

    @Override
    public Class getCellClass() {
        return Poc1CellPanel.class;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }



}
