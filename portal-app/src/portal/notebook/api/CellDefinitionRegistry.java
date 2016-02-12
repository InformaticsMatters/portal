package portal.notebook.api;

import java.util.Collection;

public interface CellDefinitionRegistry {

    Collection<CellDefinition> listCellDefinition();

    CellDefinition findCellDefinition(String name);

}
