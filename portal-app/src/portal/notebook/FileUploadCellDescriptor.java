package portal.notebook;

import portal.notebook.CellDescriptor;
import portal.notebook.CellType;

/**
 * @author simetrias
 */
public class FileUploadCellDescriptor implements CellDescriptor {

    @Override
    public CellType getCellType() {
        return CellType.FILE_UPLOAD;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "File upload cell";
    }
}
