package portal.notebook;

import portal.notebook.api.CellType;

public class ChemblActivitiesFetcherCellDescriptor implements CellDescriptor {

    @Override
    public CellType getCellType() {
        return CellType.CHEMBLACTIVITIESFETCHER;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Chembl Activities Fetcher";
    }
}
