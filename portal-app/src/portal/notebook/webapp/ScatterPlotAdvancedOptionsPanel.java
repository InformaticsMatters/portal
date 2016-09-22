package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.PopupContainerProvider;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class ScatterPlotAdvancedOptionsPanel extends AbstractDatasetAdvancedOptionsPanel {
    private static final Logger LOGGER = Logger.getLogger(ScatterPlotAdvancedOptionsPanel.class.getName());
    private Form<ModelObject> form;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public ScatterPlotAdvancedOptionsPanel(String id, Long cellId) {
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

        List<String> sizes = new ArrayList<>();
        sizes.addAll(ScatterPlotCanvasItemPanel.POINT_SIZES.keySet());
        DropDownChoice<String> p = new DropDownChoice<>("pointSize", sizes);
        form.add(p);

        CheckBox checkBox = new CheckBox("showAxisLabels");
        form.add(checkBox);

        DropDownChoice<String> color = new DropDownChoice<>("color", fieldNamesModel);
        form.add(color);

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
                    callbackHandler.notifyMessage("Error", t.getMessage());
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

    public String getColor() {
        return form.getModelObject().getColor();
    }

    public void setColor(String color) {
        form.getModelObject().setColor(color);
    }

    public String getPointSize() {
        return form.getModelObject().getPointSize();
    }

    public void setPointSize(String pointSize) {
        form.getModelObject().setPointSize(pointSize);
    }

    public Boolean getShowAxisLabels() {
        return form.getModelObject().getShowAxisLabels();
    }

    public void setShowAxisLabels(Boolean value) {
        form.getModelObject().setShowAxisLabels(value);
    }

    private class ModelObject implements Serializable {

        private String x;
        private String y;
        private String color;
        private String pointSize;
        private Boolean showAxisLabels = Boolean.FALSE;

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

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getPointSize() {
            return pointSize;
        }

        public void setPointSize(String pointSize) {
            this.pointSize = pointSize;
        }

        public Boolean getShowAxisLabels() {
            return showAxisLabels;
        }

        public void setShowAxisLabels(Boolean showAxisLabels) {
            this.showAxisLabels = showAxisLabels;
        }
    }
}
