package portal.notebook.webapp;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.notebook.api.CellInstance;

import java.io.Serializable;

/**
 * Created by timbo on 21/08/2016.
 */
public class FileExportCanvasItemPanel extends CanvasItemPanel {

    private Form<ModelObject> form;


    public FileExportCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        CellInstance cellInstance = findCellInstance();

        addForm();
        loadModelFromPersistentData();
        addTitleBar();
    }

    private void addForm() {
        form = new Form<>("form", new CompoundPropertyModel<>(new ModelObject()));
        add(form);
    }

    private void loadModelFromPersistentData() {
        CellInstance cellInstance = findCellInstance();
        ModelObject model = form.getModelObject();
    }


    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public WebMarkupContainer getContentPanel() {
        return form;
    }

    @Override
    public void onExecute() throws Exception {

    }

    class ModelObject implements Serializable {

    }
}
