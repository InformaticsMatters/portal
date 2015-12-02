package portal.notebook;

import com.squonk.notebook.api.CellType;

import java.util.Collections;
import java.util.List;

public class TableDisplayCellModel extends AbstractCellModel {
    private static final Long serialVersionUID = 1l;

    public TableDisplayCellModel(CellType cellType) {
        super(cellType);
    }

    @Override
    public List<String> getOutputVariableNameList() {
        return Collections.emptyList();
    }



}
