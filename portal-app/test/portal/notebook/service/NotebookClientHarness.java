package portal.notebook.service;


import org.squonk.client.NotebookClient;
import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.api2.NotebookSavepoint;
import portal.notebook.api.CellInstance;
import portal.notebook.api.NotebookInstance;
import portal.notebook.api.VariableInstance;
import portal.notebook.cells.ChemblActivitiesFetcherCellDefinition;
import toolkit.test.AbstractTestCase;
import toolkit.test.TestCase;
import toolkit.test.TestMethod;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@TestCase
public class NotebookClientHarness extends AbstractTestCase {
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
    public void prepare() {
       testPrefix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    @TestMethod(ordinal = 1)
    public void testNotebooks() throws Exception {
        notebookDescriptor = notebookClient.createNotebook(testPrefix, testPrefix, "testUser");
        notebookDescriptor = notebookClient.updateNotebook(notebookDescriptor.getId(), testPrefix + "'", testPrefix + "'");
        assert notebookDescriptor.getName().equals(testPrefix + "'");
        assert notebookDescriptor.getDescription().equals(testPrefix + "'");
    }

    @TestMethod(ordinal = 2)
    public void testEditables() throws Exception {
        notebookEditable = notebookClient.createEditable(notebookDescriptor.getId(), null, "testUser");
        NotebookInstance notebookInstance = new NotebookInstance();
        String json = notebookInstance.toJsonString();
        notebookEditable = notebookClient.updateEditable(notebookEditable.getNotebookId(), notebookEditable.getId(), json);
        assert json.equals(notebookEditable.getContent());
        notebookInstance.addCellInstance(new ChemblActivitiesFetcherCellDefinition());
        notebookEditable =  notebookClient.updateEditable(notebookEditable.getNotebookId(), notebookEditable.getId(), notebookInstance.toJsonString());
        notebookInstance = NotebookInstance.fromJsonString(notebookEditable.getContent());
        CellInstance cellInstance = notebookInstance.getCellInstanceList().get(0);
        VariableInstance outputVariableInstance = cellInstance.getVariableInstanceMap().values().iterator().next();
        notebookClient.writeStreamValue(notebookEditable.getNotebookId(), notebookEditable.getId(), cellInstance.getId(), outputVariableInstance.getVariableDefinition().getName(), new ByteArrayInputStream("test".getBytes()));
        String value = notebookClient.readTextValue(notebookEditable.getNotebookId(), notebookEditable.getId(), outputVariableInstance.calculateKey());
        assert value.equals("test");
    }

    @TestMethod(ordinal = 3)
    public void testSavepoints() throws Exception {
        savepointEditable = notebookClient.createSavepoint(notebookEditable.getNotebookId(), notebookEditable.getId());
        List<NotebookSavepoint> list = notebookClient.listSavepoints(notebookDescriptor.getId());
        savepoint = list.get(list.size() - 1);
        assert savepointEditable.getContent().equals(savepoint.getContent());
    }

}


