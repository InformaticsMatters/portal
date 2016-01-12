package portal.notebook;

import org.apache.wicket.markup.html.form.Form;
import portal.PopupContainerProvider;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPanel extends CanvasItemPanel {

    private Form form;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public ServiceCanvasItemPanel(String id, CellModel cellModel) {
        super(id, cellModel);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
    }

    private void addForm() {
        form = new Form("form");
        add(form);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {


    }
}
