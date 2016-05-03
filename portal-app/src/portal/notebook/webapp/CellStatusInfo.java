package portal.notebook.webapp;

import java.io.Serializable;

public class CellStatusInfo implements Serializable {
    private Boolean bindingsComplete;
    private Boolean running;
    private Boolean succeed;

    public Boolean getBindingsComplete() {
        return bindingsComplete;
    }

    public void setBindingsComplete(Boolean bindingsComplete) {
        this.bindingsComplete = bindingsComplete;
    }

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Boolean getSucceed() {
        return succeed;
    }

    public void setSucceed(Boolean succeed) {
        this.succeed = succeed;
    }

    @Override
    public String toString() {
        if (!bindingsComplete) {
            return "Input needs defining.";
        } else if (Boolean.TRUE.equals(running)) {
            return "Executing";
        } else if (Boolean.TRUE.equals(succeed)) {
            return "Succeed.";
        } else if (Boolean.FALSE.equals(succeed)) {
            return "Error occured.";
        } else {
            return "Ready for execution";
        }
    }


}
