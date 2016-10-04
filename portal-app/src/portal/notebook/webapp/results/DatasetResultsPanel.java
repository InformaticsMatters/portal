package portal.notebook.webapp.results;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.eclipse.jetty.io.RuntimeIOException;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;
import org.squonk.types.MoleculeObjectHighlightable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by timbo on 30/08/16.
 */
public class DatasetResultsPanel extends Panel {

    private static final Logger LOG = Logger.getLogger(DatasetResultsPanel.class.getName());

    private static final int DEFAULT_COLS = 5;
    private static final int DEFAULT_NUM_RECORDS = 100;
    private static final int MAX_RECORDS = 1000;
    private static final String SETTING_COLS = "resultsviewer.results.cols";
    private static final String SETTING_HIGHLIGHTER = "resultsviewer.results.highlighter";
    private static final String[] NUMBERS = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen"};
    protected static final String HIGHLIGHTER_NONE = "None";
    private final DatasetResultsHandler.CellDatasetProvider cellDatasetProvider;
    private IModel<DatasetMetadata> datasetMetadataModel;
    private IModel<List<? extends BasicObject>> resultsModel;
    private Model<String> highlighterModel = new Model<>();
    private IModel<List<String>> highlighterChoicesModel;
    private IModel gridModel = new Model(generateGridClass(DEFAULT_COLS));
    private int limit = DEFAULT_NUM_RECORDS;
    private int offset = 0;


    public DatasetResultsPanel(String id, IModel<DatasetMetadata> datasetMetadataModel, IModel<List<? extends BasicObject>> resultsModel, DatasetResultsHandler.CellDatasetProvider cellDatasetProvider) {
        super(id);
        this.datasetMetadataModel = datasetMetadataModel;
        this.resultsModel = resultsModel;
        this.cellDatasetProvider = cellDatasetProvider;
        setOutputMarkupId(true);

        try {
            updateData();
        } catch (Exception e) {
            // TODO - better notification of errors
            throw new RuntimeIOException("Failed to load data", e);
        }

        addComponents();
    }

    private Map<String, Object> cellSettings() {
        return cellDatasetProvider.getCellInstance().getSettings();
    }

    private void addComponents() {

        Map<String, Object> settings = cellSettings();
        Integer cols = safeGetInteger(settings, SETTING_COLS, 5);
        gridModel.setObject(generateGridClass(cols));

        final WebMarkupContainer grid = new WebMarkupContainer("grid");
        grid.setOutputMarkupId(true);
        grid.add(new AttributeModifier("class", gridModel));
        add(grid);

        Form form = new Form("form");
        form.setOutputMarkupId(true);
        add(form);


        Model<Integer> colsModel = new Model<>(cols);
        form.add(new NumberTextField<>("cols", colsModel)
                .setMinimum(1)
                .setMaximum(15)
                .add(new OnChangeAjaxBehavior() {

                    // TODO - bug here. If the value is set using keyboard then you get an exception:
                    // java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
                    // this happens even with an empty opUpdate() method, but only happens if the OnChangeAjaxBehavior is added.
                    // Could be a Wicket bug?

                    @Override
                    protected void onUpdate(final AjaxRequestTarget target) {

                        // get the correct cell instance every time as it might have changed
                        Map<String, Object> settings = cellSettings();

                        Integer cols = safeGetInteger(settings, SETTING_COLS, null);

                        Integer value = colsModel.getObject();
                        if (value != null && cols != value) {

                            if (value < 1 || value > 15) {
                                value = 5;
                            }
                            settings.put(SETTING_COLS, value);
                            String gridCssClass = generateGridClass(value);
                            gridModel.setObject(gridCssClass);

                            if (target != null) {
                                String js = "$('#" + grid.getMarkupId() + "').attr('class', '" + gridCssClass + "');";
                                //target.appendJavaScript("console.log('" + gridClass + "');");
                                target.appendJavaScript(js);
                            }

                            // TODO - should find a finer-grain way to handle this - save when modal closes?
                            cellDatasetProvider.saveNotebook();
                        }
                    }
                })
        );

        IModel<Integer> limitModel = new Model<>(DEFAULT_NUM_RECORDS);
        IModel<Integer> offsetModel = new Model<>(0);

        form.add(new NumberTextField<>("limit", limitModel)
                        .setMinimum(0)
                        .setMaximum(1000)
//                .setStep(100)
        );

        form.add(new NumberTextField<>("offset", offsetModel)
                        .setMinimum(0)
//                .setStep(100)
        );


        form.add(new AjaxButton("fetch") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {

                try {
                    Integer o = offsetModel.getObject();
                    if (o == null) {
                        offset = 0;
                    } else {
                        offset = o;
                    }
                    Integer l = limitModel.getObject();
                    if (l == null) {
                        limit = DEFAULT_NUM_RECORDS;
                    } else {
                        limit = l;
                    }
                    updateData();
                    offsetModel.setObject(offset);
                    limitModel.setObject(limit);
                    if (target != null) {
                        target.add(form);
                        target.add(grid);
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to reload records", e);
                    return;
                }
            }

        });

        form.add(new AjaxButton("next") {

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {

                try {
                    Integer o = offsetModel.getObject();
                    Integer l = limitModel.getObject();
                    if (o == null) {
                        offset = 0;
                    }
                    if (l == null) {
                        limit = DEFAULT_NUM_RECORDS;
                    }
                    offset = offset + l;
                    updateData();
                    offsetModel.setObject(offset);
                    limitModel.setObject(limit);
                    if (target != null) {
                        target.add(form);
                        target.add(grid);
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to reload records", e);
                    return;
                }
            }
        });

        form.add(new AjaxButton("all") {

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {

                try {
                    limit = MAX_RECORDS;
                    offset = 0;

                    updateData();
                    offsetModel.setObject(offset);
                    limitModel.setObject(limit);
                    if (target != null) {
                        target.add(form);
                        target.add(grid);
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to reload records", e);
                    return;
                }
            }
        });

        highlighterChoicesModel = new IModel<List<String>>() {

            @Override
            public void detach() {
            }

            @Override
            public List<String> getObject() {
                DatasetMetadata meta = datasetMetadataModel.getObject();
                Map<String, Class> typesMapppings = meta.getValueClassMappings();
                List<String> results = new ArrayList<>();
                results.add(HIGHLIGHTER_NONE);

                typesMapppings.forEach((fld, cls) -> {
                    if (MoleculeObjectHighlightable.class.isAssignableFrom(cls)) {
                        results.add(fld);
                    }
                });
                return results;
            }

            @Override
            public void setObject(List<String> o) {
                // never set
            }
        };

        String currentHighlighter = (String)settings.get(SETTING_HIGHLIGHTER);
        highlighterModel.setObject(currentHighlighter == null ? HIGHLIGHTER_NONE : currentHighlighter);
        DropDownChoice<String> highlightersChoice = new DropDownChoice<>("highlighters", highlighterModel, highlighterChoicesModel);
        highlightersChoice.setOutputMarkupId(true);
        highlightersChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
                String chosen = highlightersChoice.getModelObject();

                Map<String, Object> settings = cellSettings();
                String current = (String) settings.get(SETTING_HIGHLIGHTER);
                if (chosen == null && current == null) {
                    // do nothing
                } else if (chosen == null) {
                    settings.remove(SETTING_HIGHLIGHTER);
                    cellDatasetProvider.saveNotebook();
                } else if (current != null && !current.equals(chosen)) {
                    settings.put(SETTING_HIGHLIGHTER, chosen);
                    cellDatasetProvider.saveNotebook();
                }
                target.add(grid);
            }

        });
        form.add(highlightersChoice);
        
        grid.add(new ListView<BasicObject>("card", resultsModel) {

            @Override
            protected void populateItem(ListItem<BasicObject> listItem) {
                BasicObject o = listItem.getModelObject();
                DatasetMetadata meta = datasetMetadataModel.getObject();
                Map<String, Class> mappings = meta.getValueClassMappings();

                if (o instanceof MoleculeObject) {
                    listItem.add(new MoleculeObjectCardPanel("column", mappings, (MoleculeObject) o, datasetMetadataModel, cellDatasetProvider.getStructureIOClient(), highlighterModel));
                } else {
                    listItem.add(new BasicObjectCardPanel("column", mappings, o));
                }
            }
        });
    }


    private Integer safeGetInteger(Map<String, Object> settingsMap, String prop, Integer defaultValue) {
        Integer cols = null;
        try {
            cols = (Integer) settingsMap.get(prop);
        } catch (Exception e) {
            // ignore
        }
        return cols == null ? defaultValue : cols;
    }

    private String generateGridClass(int cols) {
        return "ui " + NUMBERS[cols] + " column grid";
    }

    private synchronized <T extends BasicObject> void updateData() throws Exception {

        Dataset<T> dataset = cellDatasetProvider.getDataset();
        if (limit < 1 || limit > MAX_RECORDS) {
            limit = MAX_RECORDS;
        }
        if (offset < 0) {
            offset = 0;
        }
        if (limit < 0 || limit > MAX_RECORDS) {
            limit = DEFAULT_NUM_RECORDS;
        }
        Stream<T> stream = dataset.getStream();
        try {
            if (offset != 0) {
                stream = stream.skip((long) offset);
            }
            stream = stream.limit((long) limit);
            List<T> records = stream.collect(Collectors.toList());
            resultsModel.setObject(records);
            LOG.fine("Loaded records. offset=" + offset + " limit=" + limit + " size=" + records.size());
        } finally {
            stream.close();
        }

    }
}
