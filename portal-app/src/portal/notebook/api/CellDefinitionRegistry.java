package portal.notebook.api;

import org.squonk.notebook.api.CellDefinition;

import java.util.Collection;

public interface CellDefinitionRegistry {

    Collection<CellDefinition> listCellDefinition();

    CellDefinition findCellDefinition(String name);

}
