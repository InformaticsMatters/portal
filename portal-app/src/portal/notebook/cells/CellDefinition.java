package portal.notebook.cells;

import portal.notebook.NotebookSession;

import java.util.List;

/**
 * Created by timbo on 28/11/2015.
 */
public interface CellDefinition<T extends CellModel> {

    /**
     * Internal name - user never sees this
     * @return
     */
    String getId();

    /**
     * Short name
     * @return
     */
    String getName();

    /**
     * Description e.g. for a tooltip
     * @return
     */
    String getDescription();

    /** Get the icon URL to use for representation in the pallette.
     *
     * @return
     */
    String getIconURL();

    /**
     * URL that can be used for more info e.g. Wiki page with documentation
     * @return
     */
    String getInfoURL();

    /**
     * A set of tags that classify the cell and allow it to be searched/filtered
     * @return
     */
    String[] getTags();

    /**
     * list of locations (folders etc.) where this cell will appear in the hierarchy
     * @return
     */
    String[] getPaths();

    /**
     * Is this cell executable
     * @return
     */
    boolean isExecutable();

    /**
     * Does the cell need execution to get it up to date.
     * @return
     */
    boolean needsExecution();

    /**
     * Does the cell support maximised mode e.g. display at full screen size
     * @return
     */
    boolean isMaximisable();

    /** Can the cell be resized in the horizontal direction
     *
     * @return
     */
    boolean isResizableX();

    /** Can the cell be resized in the vertical direction
     *
     * @return
     */
    boolean isResizableY();


    /** Render the cell UI.
     * This bit is a bit fuzzy. Ideally its completly decoupled from any frameworks (e.g. Wicket)
     * and specialised sub-classes will handle specific implementations, but that may not be possible.
     *
     * Sub-classes can make this more declarative by allowing specification of the options that
     * have to be renedered e.g. soemthing like:
     * List<CellOption> getBasicOptions(); // these appear in the main cell area.
     */
    void render(Object someContext);


    /** Similar to the render() method, this allows the advanced options (those that appear in a
     * popup opened from the header icon) to be defined in any way that is needed.
     * Similar to the render() method sub-classes can make this more declarative by allowing something
     * like this:
     * List<CellOption> getAdvancedOptions();
     *
     * @param someContext
     */
    void renderAdvancedOptions(Object someContext);




    // handle as variable name now - probably too simple
    List<String> getInputVariables();
    List<String> getOutputVariables();

    // TODO handle selection

    /** Get a model of the cell options e.g. to be used for rendering the UI
     *
     * @return
     */
    T getModel();

    /** Save the model
     *
     * @param model
     */
    void saveModel(T model);

    /** Restore the model e.g. when re-opening a notebook.
     *
     * @param model
     */
    void restoreModel(T model);

    /** Execute the cell.
     *
     * @param notebookSession
     * @throws Exception
     */
    void exectute(NotebookSession notebookSession) throws Exception;

}
