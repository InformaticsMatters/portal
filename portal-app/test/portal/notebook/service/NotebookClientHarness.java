package portal.notebook.service;


import org.squonk.client.NotebookVariableClient;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;
import portal.notebook.api.CellDefinitionRegistry;
import portal.notebook.api.CellInstance;
import portal.notebook.api.NotebookInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.cells.ChemblActivitiesFetcherCellDefinition;
import toolkit.derby.DerbyUtils;
import toolkit.test.AbstractTestCase;
import toolkit.test.TestCase;
import toolkit.test.TestMethod;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@TestCase
public class NotebookClientHarness extends AbstractTestCase {
    public static final String USER_NAME = "user1";
    @Inject
    private NotebookVariableClient notebookClient;
    @Inject
    private CellDefinitionRegistry cellDefinitionRegistry;
    private String testPrefix;
    private NotebookDTO notebookDescriptor;
    private NotebookEditableDTO notebookEditable;
    private NotebookEditableDTO savepointEditable;
    private NotebookSavepointDTO savepoint;

    public static void main(String[] args) throws Exception {
        NotebookClientHarness.runTestCase(NotebookClientHarness.class);
    }

    @TestMethod(ordinal = 0)
    public void prepare() throws Exception {
        DerbyUtils.startDerbyServer();
        testPrefix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    @TestMethod(ordinal = 1)
    public void testNotebooks() throws Exception {
        notebookDescriptor = notebookClient.createNotebook(testPrefix, testPrefix, USER_NAME);
        notebookDescriptor = notebookClient.updateNotebook(notebookDescriptor.getId(), testPrefix + "'", testPrefix + "'");
        if (!notebookDescriptor.getName().equals(testPrefix + "'")) {
            throw new RuntimeException("Different name");
        }
        if (!notebookDescriptor.getDescription().equals(testPrefix + "'")) {
            throw new RuntimeException("Different description");
        }
    }

    @TestMethod(ordinal = 2)
    public void testEditables() throws Exception {
        notebookClient.createEditable(notebookDescriptor.getId(), null, USER_NAME);
        notebookEditable = notebookClient.listEditables(notebookDescriptor.getId(), USER_NAME).get(0);
        NotebookCanvasDTO canvas1 = new NotebookCanvasDTO(1L);

        notebookClient.updateEditable(notebookEditable.getNotebookId(), notebookEditable.getId(), canvas1);
        notebookEditable = notebookClient.listEditables(notebookDescriptor.getId(), USER_NAME).get(0);
        NotebookCanvasDTO canvas2 =  notebookEditable.getCanvasDTO();
        if (!canvas1.getLastCellId().equals(canvas2.getLastCellId())) {
            throw new RuntimeException("Different content");
        }
        NotebookInstance notebookInstance = new NotebookInstance();
        notebookInstance.loadNotebookCanvasDTO(canvas2, cellDefinitionRegistry);
        CellInstance cellInstance = notebookInstance.addCellInstance(new ChemblActivitiesFetcherCellDefinition());
        cellInstance.setSizeWidth(267);
        cellInstance.setSizeHeight(167);
        cellInstance.setPositionTop(1);
        cellInstance.setPositionLeft(1);
        NotebookCanvasDTO notebookCanvasDTO = new NotebookCanvasDTO(notebookInstance.getLastCellId());
        notebookInstance.storeNotebookCanvasDTO(notebookCanvasDTO);
        notebookClient.updateEditable(notebookEditable.getNotebookId(), notebookEditable.getId(), notebookCanvasDTO);
        notebookEditable = notebookClient.listEditables(notebookDescriptor.getId(), USER_NAME).get(0);
        notebookInstance = new NotebookInstance();
        notebookInstance.loadNotebookCanvasDTO(notebookEditable.getCanvasDTO(), cellDefinitionRegistry);
        cellInstance = notebookInstance.getCellInstanceList().get(0);
        VariableInstance outputVariableInstance = cellInstance.getVariableInstanceMap().values().iterator().next();
        notebookClient.writeTextValue(notebookEditable.getNotebookId(), notebookEditable.getId(), cellInstance.getId(), outputVariableInstance.getVariableDefinition().getName(), "test");
        String value = notebookClient.readTextValue(notebookEditable.getNotebookId(), notebookEditable.getId(), outputVariableInstance.calculateKey());
        if (!value.equals("test")) {
            throw new RuntimeException("Different value");
        }
    }

    @TestMethod(ordinal = 3)
    public void testSavepoints() throws Exception {
        savepointEditable = notebookClient.createSavepoint(notebookEditable.getNotebookId(), notebookEditable.getId());
        List<NotebookSavepointDTO> list = notebookClient.listSavepoints(notebookDescriptor.getId());
        savepoint = list.get(list.size() - 1);
        if (!savepointEditable.getCanvasDTO().getLastCellId().equals(savepoint.getCanvasDTO().getLastCellId())) {
            throw new RuntimeException("Different content");
        }
    }

}


