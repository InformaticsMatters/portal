package portal.notebook.webapp;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import portal.notebook.api.CellDefinition;
import portal.notebook.webapp.cell.CellUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 22/09/2016.
 */
public class AbstractDatasetAdvancedOptionsPanel extends Panel {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetAdvancedOptionsPanel.class.getName());

    protected final Long cellId;
    @SuppressWarnings("unchecked")
    protected final IModel<List<String>> fieldNamesModel = new Model();
    protected CallbackHandler callbackHandler;

    public AbstractDatasetAdvancedOptionsPanel(String id, Long cellId) {
        super(id);
        this.cellId = cellId;
        fieldNamesModel.setObject(Collections.emptyList());
    }

    protected void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        try {
            loadPicklist();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load fields");
            callbackHandler.notifyMessage("Error", "Failed to load fields");
            fieldNamesModel.setObject(Collections.emptyList());
        }
    }

    private void loadPicklist() throws Exception {
        fieldNamesModel.setObject(CellUtils.fieldNamesSorted(callbackHandler.getNotebookSesion(), cellId, CellDefinition.VAR_NAME_INPUT));
    }


    public interface CallbackHandler extends Serializable {
        void onApplyAdvancedOptions() throws Exception;
        void notifyMessage(String title, String message);
        NotebookSession getNotebookSesion();
    }
}
