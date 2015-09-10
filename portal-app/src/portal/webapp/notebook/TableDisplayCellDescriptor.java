package portal.webapp.notebook;

public class TableDisplayCellDescriptor implements CellDescriptor {
    private String sourceVarName;


    @Override
    public Class getCellClass() {
        return TableDisplayCellPanel.class;
    }

    public String getSourceVarName() {
        return sourceVarName;
    }

    public void setSourceVarName(String sourceVarName) {
        this.sourceVarName = sourceVarName;
    }
}
