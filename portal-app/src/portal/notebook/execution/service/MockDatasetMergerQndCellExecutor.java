package portal.notebook.execution.service;

import com.im.lac.types.BasicObject;
import org.squonk.dataset.Dataset;
import org.squonk.notebook.api.BindingDTO;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.CellType;
import org.squonk.notebook.api.VariableKey;


import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

public class MockDatasetMergerQndCellExecutor extends AbstractDatasetExecutor {

    @Override
    public boolean handles(CellType cellType) {
        return "DatasetMerger".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {
        CellDTO cellDTO = callbackClient.retrieveCell(cellName);

        for (BindingDTO bindingDTO : cellDTO.getBindingMap().values()) {
            VariableKey variableKey = bindingDTO.getVariableKey();
            String varableFqn = variableKey == null ? null : (variableKey.getProducerName() + "." + variableKey.getName());
            System.out.println(bindingDTO.getName() + ": " + varableFqn);
        }

        List<BasicObject> mols = new ArrayList<>();
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));
        mols.add(new BasicObject(createMap()));

        Dataset dataset = new Dataset<>(BasicObject.class, mols);

        // write to Results
        try {
            writeDataset(cellDTO, "results", dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    Map createMap() {
        Random r = new Random();
        Map map = new HashMap();
        map.put("A", r.nextFloat());
        map.put("B", r.nextInt());
        map.put("C", "Hello " + r.nextInt());
        return map;
    }


}