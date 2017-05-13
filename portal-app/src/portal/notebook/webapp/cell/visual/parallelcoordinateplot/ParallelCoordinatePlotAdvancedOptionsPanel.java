package portal.notebook.webapp.cell.visual.parallelcoordinateplot;

import org.apache.wicket.markup.html.form.DropDownChoice;
import portal.notebook.webapp.AbstractDatasetAdvancedOptionsPanel;
import portal.notebook.webapp.cell.visual.parallelcoordinateplot.ParallelCoordinatePlotCanvasItemPanel.NullValues;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.PopupContainerProvider;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class ParallelCoordinatePlotAdvancedOptionsPanel extends AbstractDatasetAdvancedOptionsPanel {
    private static final Logger LOGGER = Logger.getLogger(ParallelCoordinatePlotAdvancedOptionsPanel.class.getName());
    private Form<ModelObject> form;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public ParallelCoordinatePlotAdvancedOptionsPanel(String id, Long cellId, CallbackHandler callbackHandler) {
        super(id, cellId, callbackHandler);
        setOutputMarkupId(true);
        addComponents();
    }


    private void addComponents() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new ModelObject()));

        CheckBoxMultipleChoice<String> fields = new CheckBoxMultipleChoice<>("fields", fieldNamesModel);
        fields.setMaxRows(10);
        form.add(fields);

        List<NullValues> nullValues = new ArrayList<>();
        nullValues.addAll(Arrays.asList(NullValues.values()));
        DropDownChoice<NullValues> p = new DropDownChoice<>("nullValues", nullValues);
        form.add(p);

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


    public List<String> getFields() {
        return form.getModelObject().getFields();
    }

    public void setFields(List<String> fields) {
        form.getModelObject().setFields(fields);
    }

    public NullValues getNullValues() {
        return form.getModelObject().getNullValues();
    }

    public void setNullValues(NullValues nullValues) {
        form.getModelObject().setNullValues(nullValues);
    }


    private class ModelObject implements Serializable {

        private List<String> fields;
        private NullValues nullValues;

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }

        public NullValues getNullValues() {
            return nullValues;
        }

        public void setNullValues(NullValues nullValues) {
            this.nullValues = nullValues;
        }
    }
}
