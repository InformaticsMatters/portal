package portal.webapp.notebook;

import java.io.Serializable;

public interface CellDescriptor extends Serializable {

    Class getCellClass();

}
