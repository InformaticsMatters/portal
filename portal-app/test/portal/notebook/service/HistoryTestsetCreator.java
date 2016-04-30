package portal.notebook.service;

import org.squonk.client.NotebookVariableClient;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import toolkit.derby.DerbyUtils;
import toolkit.test.AbstractTestCase;
import toolkit.test.TestCase;
import toolkit.test.TestMethod;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;

@TestCase
public class HistoryTestsetCreator extends AbstractTestCase {
    @Inject
    private NotebookVariableClient notebookVariableClient;

    public static void main(String[] args) throws Exception {
        NotebookClientTest.runTestCase(HistoryTestsetCreator.class);
    }

    @TestMethod(ordinal = 0)
    public void createTestset() throws Exception {
        String testName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        DerbyUtils.startDerbyServer();
        NotebookDTO notebookDTO = notebookVariableClient.createNotebook("user1", testName, testName);
        NotebookEditableDTO editable1DTO = notebookVariableClient.createEditable(notebookDTO.getId(), null, "user1");
        Thread.sleep(1000);
        NotebookEditableDTO editable2DTO = notebookVariableClient.createEditable(notebookDTO.getId(), null, "user1");
        NotebookEditableDTO savepoint1DTO  = notebookVariableClient.createSavepoint(notebookDTO.getId(), editable1DTO.getId(), "description");
        notebookVariableClient.setSavepointDescription(notebookDTO.getId(), savepoint1DTO.getId(), "SP1");
        notebookVariableClient.createEditable(notebookDTO.getId(), savepoint1DTO.getId(), "user1");
        NotebookEditableDTO savepoint2DTO  = notebookVariableClient.createSavepoint(notebookDTO.getId(), editable2DTO.getId(), "description");
        notebookVariableClient.setSavepointDescription(notebookDTO.getId(), savepoint2DTO.getId(), "SP2");
    }

}
