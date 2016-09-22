package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.PopupContainerProvider;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class BoxPlotAdvancedOptionsPanel extends AbstractDatasetAdvancedOptionsPanel {
    private static final Logger LOGGER = Logger.getLogger(BoxPlotAdvancedOptionsPanel.class.getName());

    private Form<ModelObject> form;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public BoxPlotAdvancedOptionsPanel(String id, Long cellId) {
        super(id, cellId);
        setOutputMarkupId(true);
        addComponents();
    }


    private void addComponents() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new ModelObject()));

        DropDownChoice<String> x = new DropDownChoice<>("x", fieldNamesModel);
        form.add(x);

        DropDownChoice<String> y = new DropDownChoice<>("y", fieldNamesModel);
        form.add(y);

        add(form);

        form.add(new IndicatingAjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
                try {
                    if (callbackHandler != null) {
                        callbackHandler.onApplyAdvancedOptions();
                    }
                    popupContainerProvider.refreshContainer(getPage(), target);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Error storing notebook", t);
                    callbackHandler.notifyMessage("Error", t.getLocalizedMessage());
                }
            }
        });
    }

    public String getX() {
        return form.getModelObject().getX();
    }

    public void setX(String x) {
        form.getModelObject().setX(x);
    }

    public String getY() {
        return form.getModelObject().getY();
    }

    public void setY(String y) {
        form.getModelObject().setY(y);
    }


    private class ModelObject implements Serializable {

        private String x;
        private String y;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }

    }
}
