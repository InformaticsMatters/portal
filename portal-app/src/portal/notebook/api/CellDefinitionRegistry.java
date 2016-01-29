package portal.notebook.api;

import portal.notebook.api.CellDefinition;

import java.util.Collection;

public interface CellDefinitionRegistry {

    Collection<CellDefinition> listCellDefinition();
    CellDefinition retrieveCellDefinition(String name);

}
