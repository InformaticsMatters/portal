package portal.webapp.notebook;

public class CodeCellDescriptor implements CellDescriptor {
    private String code;
    private String errorMessage;
    private Object outcome;

    @Override
    public Class getCellClass() {
        return CodeCellPanel.class;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getOutcome() {
        return outcome;
    }

    public void setOutcome(Object outcome) {
        this.outcome = outcome;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
