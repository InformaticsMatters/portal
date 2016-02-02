package portal.notebook;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.execution.steps.StepDefinitionConstants;

import javax.inject.Inject;
import java.io.Serializable;

public class ChemblActivitiesFetcherCanvasItemPanel extends CanvasItemPanel {

    private static final String OPT_ASSAY_ID = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_ASSAY_ID;
    private static final String OPT_PREFIX = StepDefinitionConstants.ChemblActivitiesFetcher.OPTION_PREFIX;

    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public ChemblActivitiesFetcherCanvasItemPanel(String id, CellModel cell) {
        super(id, cell);
        addForm();
        addTitleBar();
        load();
        setOutputMarkupId(true);
    }

    private void load() {
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        form.setOutputMarkupId(true);
        TextField<String> assayIdField = new TextField<String>(OPT_ASSAY_ID);
        form.add(assayIdField);
        TextField<String> prefixField = new TextField<String>(OPT_PREFIX);
        form.add(prefixField);
        add(form);
    }

    private void execute() {
        form.getModelObject().store();
        VariableModel outputVariableModel = notebookSession.getCurrentNotebookModel().findVariableModel(getCellModel().getName(), "output");
        outputVariableModel.setValue(null);
        notebookSession.storeCurrentNotebook();
        notebookSession.executeCell(getCellModel().getName());
        fireContentChanged();
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        execute();
    }

    class ModelObject implements Serializable {

        private String assayId;
        private String prefix;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getAssayId() {
            return assayId;
        }

        public void setAssayId(String assayId) {
            this.assayId = assayId;
        }

        public void load() {
            assayId = (String) getCellModel().getOptionModelMap().get(OPT_ASSAY_ID).getValue();
            prefix = (String) getCellModel().getOptionModelMap().get(OPT_PREFIX).getValue();
        }

        public void store() {
            getCellModel().getOptionModelMap().get(OPT_ASSAY_ID).setValue(assayId);
            getCellModel().getOptionModelMap().get(OPT_PREFIX).setValue(prefix);
        }

    }

}
