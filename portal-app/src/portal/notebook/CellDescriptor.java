package portal.notebook;

import portal.notebook.api.CellType;

import java.io.Serializable;

public interface CellDescriptor extends Serializable {

    CellType getCellType();
    String getIcon();
    String getDescription();

}
