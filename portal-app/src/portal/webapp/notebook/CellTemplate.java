package portal.webapp.notebook;

public interface CellTemplate<T extends CellDescriptor> {

    CellType getCellType();
    String getIcon();
    String getDescription();

}
