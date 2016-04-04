package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.PopupContainerProvider;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class ThreeDimMolAdvancedOptionsPanel extends Panel {

    private final Long cellId;
    private List<String> picklistItems;
    private Form<ModelObject> form;
    private CallbackHandler callbackHandler;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public ThreeDimMolAdvancedOptionsPanel(String id, Long cellId) {
        super(id);
        setOutputMarkupId(true);
        this.cellId = cellId;
        addComponents();
    }

    private void addComponents() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new ModelObject()));

        form.add(new TextField<String>("pdbId"));

        form.add(new IndicatingAjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
                if (callbackHandler != null) {
                    callbackHandler.onApplyAdvancedOptions();
                }
                popupContainerProvider.refreshContainer(getPage(), target);
            }
        });
        add(form);
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public String getPdbId() {
        return form.getModelObject().getPdbId();
    }

    public void setPdbId(String pdbId) {
        form.getModelObject().setPdbId(pdbId);
    }

    public interface CallbackHandler extends Serializable {

        void onApplyAdvancedOptions();

    }

    private class ModelObject implements Serializable {

        private String pdbId;

        public String getPdbId() {
            return pdbId;
        }

        public void setPdbId(String pdbId) {
            this.pdbId = pdbId;
        }
    }
}
