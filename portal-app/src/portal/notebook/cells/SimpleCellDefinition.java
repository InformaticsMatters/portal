package portal.notebook.cells;



import org.squonk.notebook.api.CellDefinition;
import org.squonk.notebook.api.CellExecutor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * temporary
 */
@XmlRootElement
public class SimpleCellDefinition extends CellDefinition {
    private final static long serialVersionUID = 1l;

    public SimpleCellDefinition() {}

    public SimpleCellDefinition(String name, String description, String icon, String[] tags, Boolean executable) {
        super(name, description, icon, tags, executable);
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new DummyCellExecutor();
    }
}
