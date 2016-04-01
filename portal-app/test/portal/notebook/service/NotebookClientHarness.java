package portal.notebook.service;


import org.squonk.client.NotebookClient;
import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.api2.NotebookSavepoint;
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
    private NotebookClient notebookClient;
    private String testPrefix;
    private NotebookDescriptor notebookDescriptor;
    private NotebookEditable notebookEditable;
    private NotebookEditable savepointEditable;
    private NotebookSavepoint savepoint;

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
        NotebookInstance notebookInstance = new NotebookInstance();
        String json = notebookInstance.toJsonString();
        notebookClient.updateEditable(notebookEditable.getNotebookId(), notebookEditable.getId(), json);
        notebookEditable = notebookClient.listEditables(notebookDescriptor.getId(), USER_NAME).get(0);
        String newJson = notebookEditable.getContent();
        if (!json.equals(newJson)) {
            throw new RuntimeException("Different content");
        }
        CellInstance cellInstance = notebookInstance.addCellInstance(new ChemblActivitiesFetcherCellDefinition());
        cellInstance.setSizeWidth(267);
        cellInstance.setSizeHeight(167);
        cellInstance.setPositionTop(1);
        cellInstance.setPositionLeft(1);
        notebookClient.updateEditable(notebookEditable.getNotebookId(), notebookEditable.getId(), notebookInstance.toJsonString());
        notebookEditable = notebookClient.listEditables(notebookDescriptor.getId(), USER_NAME).get(0);
        notebookInstance = NotebookInstance.fromJsonString(notebookEditable.getContent());
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
        List<NotebookSavepoint> list = notebookClient.listSavepoints(notebookDescriptor.getId());
        savepoint = list.get(list.size() - 1);
        if (!savepointEditable.getContent().equals(savepoint.getContent())) {
            throw new RuntimeException("Different content");
        }
    }

}


