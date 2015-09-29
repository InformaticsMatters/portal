package portal.webapp.notebook;

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
