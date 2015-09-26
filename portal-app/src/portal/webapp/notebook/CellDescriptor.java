package portal.webapp.notebook;

import java.io.Serializable;

public interface CellDescriptor extends Serializable {

    CellType getCellType();
    String getIcon();
    String getDescription();

}
