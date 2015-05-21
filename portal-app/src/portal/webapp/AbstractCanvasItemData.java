package portal.webapp;

import java.io.Serializable;

/**
 * @author simetrias
 */
public abstract class AbstractCanvasItemData implements Serializable {

    private String positionX;
    private String positionY;

    public String getPositionX() {
        return positionX;
    }

    public void setPositionX(String positionX) {
        this.positionX = positionX;
    }

    public String getPositionY() {
        return positionY;
    }

    public void setPositionY(String positionY) {
        this.positionY = positionY;
    }

}
