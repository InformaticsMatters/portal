package portal.notebook.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import portal.PopupContainerProvider;
import portal.notebook.webapp.HeatmapCanvasItemPanel.ValueCollector;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author simetrias
 */
public class HeatmapAdvancedOptionsPanel extends AbstractDatasetAdvancedOptionsPanel {
    private static final Logger LOGGER = Logger.getLogger(HeatmapAdvancedOptionsPanel.class.getName());
    private Form<ModelObject> form;
    @Inject
    private PopupContainerProvider popupContainerProvider;

    public HeatmapAdvancedOptionsPanel(String id, Long cellId) {
        super(id, cellId);
        setOutputMarkupId(true);
        addComponents();
    }

    private void addComponents() {
        form = new Form<>("form");
        form.setModel(new CompoundPropertyModel<>(new ModelObject()));

        // TODO - restrict to string and integer
        DropDownChoice<String> rows = new DropDownChoice<>("rowsField", fieldNamesModel);
        form.add(rows);

        // TODO - restrict to string and integer
        DropDownChoice<String> cols = new DropDownChoice<>("colsField", fieldNamesModel);
        form.add(cols);

        DropDownChoice<String> values = new DropDownChoice<>("valuesField", fieldNamesModel);
        form.add(values);

        // TODO - if values data type is not numeric restrict to count as the only option
        List<String> collectors = Arrays.stream(ValueCollector.values()).map((e) -> e.toString()).collect(Collectors.toList());
        DropDownChoice<String> collector = new DropDownChoice<>("collector", collectors);
        form.add(collector);


        NumberTextField sizeField = new NumberTextField<>("cellSize");
        form.add(sizeField);

        NumberTextField leftMargin = new NumberTextField<>("leftMargin");
        form.add(leftMargin);

        NumberTextField topMargin = new NumberTextField<>("topMargin");
        form.add(topMargin);

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


    public String getRowsField() {
        return form.getModelObject().getRowsField();
    }

    public void setRowsField(String x) {
        form.getModelObject().setRowsField(x);
    }

    public String getColsField() {
        return form.getModelObject().getColsField();
    }

    public void setColsField(String y) {
        form.getModelObject().setColsField(y);
    }

    public String getValuesField() {
        return form.getModelObject().getValuesField();
    }

    public void setValuesField(String y) {
        form.getModelObject().setValuesField(y);
    }

    public String getCollector() {
        return form.getModelObject().getCollector();
    }

    public void setCollector(String collector) {
        form.getModelObject().setCollector(collector);
    }

    public Integer getCellSize() {
        return form.getModelObject().getCellSize();
    }

    public void setCellSize(Integer size) {
        form.getModelObject().setCellSize(size);
    }

    public Integer getLeftMargin() {
        return form.getModelObject().getLeftMargin();
    }

    public void setLeftMargin(Integer size) {
        form.getModelObject().setLeftMargin(size);
    }

    public Integer getTopMargin() {
        return form.getModelObject().getTopMargin();
    }

    public void setTopMargin(Integer size) {
        form.getModelObject().setTopMargin(size);
    }

    private class ModelObject implements Serializable {

        private String rowsField;
        private String colsField;
        private String valuesField;
        private String collector;
        private Integer cellSize;
        private Integer leftMargin;
        private Integer topMargin;

        public String getRowsField() {
            return rowsField;
        }

        public void setRowsField(String rowsField) {
            this.rowsField = rowsField;
        }

        public String getColsField() {
            return colsField;
        }

        public void setColsField(String colsField) {
            this.colsField = colsField;
        }

        public String getValuesField() {
            return valuesField;
        }

        public void setValuesField(String valuesField) {
            this.valuesField = valuesField;
        }

        public String getCollector() {
            return collector;
        }

        public void setCollector(String collector) {
            this.collector = collector;
        }

        public Integer getCellSize() {
            return cellSize;
        }

        public void setCellSize(Integer cellSize) {
            this.cellSize = cellSize;
        }

        public Integer getLeftMargin() {
            return leftMargin;
        }

        public void setLeftMargin(Integer leftMargin) {
            this.leftMargin = leftMargin;
        }

        public Integer getTopMargin() {
            return topMargin;
        }

        public void setTopMargin(Integer topMargin) {
            this.topMargin = topMargin;
        }
    }
}
