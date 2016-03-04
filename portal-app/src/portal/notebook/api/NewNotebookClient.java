package portal.notebook.api;


import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 22/02/2016.
 */
public interface NewNotebookClient {

    /* In the initial case (before we implement savepoints) each NotebookDefinition can provide just one default NotebookEditable
     * from which you get the NotebookInstance which should minimise changes from current API.
     */

    // Client API methods

    NotebookDescriptor createNotebookDefinition(String username);        // create a new notebook
    List<NotebookDescriptor> listNotebookDefinitions(String username);   // all those you have access to
    NotebookDescriptor fetchNotebookDefinition(Long definitionId);       // optional?

    NotebookEditable getDefaultNotebookEditable(Long definitionId, String username); // typically the last one you had open
    List<NotebookEditable> fetchNotebookEditables(Long definitionId);
    NotebookEditable fetchNotebookEditable(Long editableId);             // optional?
    void storeNotebookEditable(NotebookEditable editable);               // update it
    Long createSavepoint(Long notebookEditableId, String username);      // create a new savepoint that is inserted in the history between this NotebookEditable and its parent

    List<NotebookSavepoint> fetchSavepoints(Long definitionId);
    void setSavepointDescription(String description);                    // gives this savepoint a description that can be helpful to describe its purpose
    void setSavepointLabel(String label);                                // gives this savepoint a specific label, removing that label from a different savepoint if present.
    void deleteSavepoint(Long savepointId);                              // remove this savepoint, shortening the history as appropriate (and deleting variables associated ONLY with this savepoint)

    CellInstance fetchCellInstance(Long editableId, String cellName);    // is this needed in API as you can get the cell from the NotebookInstance?
                                                                         // you can only work with cells in the context of a NotebookEditable

    /* reading and writing variables
    *
    * This differs from current API in:
    * 1. the producer cell is not present - variable names are unique within a notebook and I should not need to know who produced the data to be able to retrieve it.
    * 2. a key property is introduced to allow to distinguish multi-attribute variables (e.g. Dataset and its metadata)
    * 3. variables can be retrieved, but not stored for NotebookSavepoints
    * 4. variable can be retrieved using the "label" of a NotebookSavepoint
    * 5. read/Write for Integer is removed as that can be handled as text, and we don't want to introduce API methods for every Java data type.
     */
    String readTextValue(Long sourceId, String variableName, String key); // sourceId can be a notebookEditableId or a notebookSavepointId
    String readTextValue(String label, String variableName, String key); // versionId can be a notebookEditableId or a notebookSavepointId
    void writeTextValue(Long notebookEditableId, String variableName, String key, String value); // can only write against a notebookEditableId

    InputStream readStreamValue(Long versionId, String variableName, String key);
    InputStream readStreamValue(String label, String variableName, String key);
    void writeStreamValue(Long notebookEditableId, String variableName, String key, InputStream value);


    // API classes

    /** The master defintion of a notebook. There is only one of these for each notebook.
     * Access control is at this level (meaning if they are given access to the NotebookDefinition then they can see
     * all NotebookEditables and NotebookSavepoints).
     *
     */
    interface NotebookDescriptor {
        String getId();
        String getName();
        String getDescription();
        String getOwner();
    }

    /** A save snapshot of a notebook. Allows user to save a specific version of a notebook and go back to it
     * later as a starting point.
     * User cannot open these. They can only work with NotebookEditables.
     * Savepoints are essentially owner-less. They might have been created by someone, but they don't belong to
     * anyone as they can't be directly worked with (only used to create a new NotebookEditable).
     *
     * Where one savepoint is a parent of another they share variables that have not been changed by cell execution. Different
     * versions of variables are ONLY present when the data has changed.
     */
    interface NotebookSavepoint {
        Long getId();
        String getDescription();
        String getLabel();
        NotebookSavepoint getParent();
        List<NotebookEditable> getEditables();
        NotebookEditable createEditable();
        String getCreatedBy();
        Date getCreatedDate();
    }

    /** A working version of a notebook that a user can open, modify and execute.
     * In the case of a shared notebook each user typically has their own NotebookEditable to work with.
     * The exception would be when a second user "attaches" to a NotebookEditable of a different user and they
     * enter "collaboration" mode where changes made by one user show up immediately in the other users screen.
     *
     * When a new NotebookEditable is created from a NotebookSavepoint (its parent) it initially has references to all the variables
     * of the parent, but whenever any variables are updated through cell execution new versions of those variables are created
     */
    interface NotebookEditable {
        Long getId();
        NotebookSavepoint getParent();
        NotebookInstance getInstance();
        String getOwner();
        Date getCreatedDate();
        Date getLastUpdatedDate();
    }
}
