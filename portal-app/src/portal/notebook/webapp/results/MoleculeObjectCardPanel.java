package portal.notebook.webapp.results;

import org.squonk.types.MoleculeObject;

import java.util.Map;

/**
 * Created by timbo on 31/08/16.
 */
public class MoleculeObjectCardPanel extends BasicObjectCardPanel<MoleculeObject> {


    MoleculeObjectCardPanel(String id, Map<String, Class> classMappings, MoleculeObject mo) {
        super(id, classMappings, mo);

    }

    @Override
    protected void handleMainContent() {
        // TODO handle the structure
    }
}
