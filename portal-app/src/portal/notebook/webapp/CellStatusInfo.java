package portal.notebook.webapp;

import java.io.Serializable;

public class CellStatusInfo implements Serializable {
    private Boolean hasBindings;
    private Boolean running;
    private Boolean succeed;

    public Boolean getHasBindings() {
        return hasBindings;
    }

    public void setHasBindings(Boolean hasBindings) {
        this.hasBindings = hasBindings;
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
        StringBuilder stringBuilder = new StringBuilder();
        if (hasBindings) {
            stringBuilder.append("Bindings set.");
        }
        if (Boolean.TRUE.equals(running)) {
            stringBuilder.append(" Running.");
        } else if (Boolean.TRUE.equals(succeed)) {
            stringBuilder.append(" Succeed.");
        } else if (Boolean.FALSE.equals(succeed)) {
            stringBuilder.append(" Error.");
        }
        return stringBuilder.toString();
    }


}
