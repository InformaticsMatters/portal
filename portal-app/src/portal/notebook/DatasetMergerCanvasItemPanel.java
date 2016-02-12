package portal.notebook;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.squonk.execution.steps.StepDefinitionConstants;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.VariableInstance;

import javax.inject.Inject;
import java.io.Serializable;

public class DatasetMergerCanvasItemPanel extends CanvasItemPanel {

    private static final String OPT_MERGE_FIELD_NAME = StepDefinitionConstants.DatasetMerger.OPTION_MERGE_FIELD_NAME;
    private static final String OPT_KEEP_FIRST = StepDefinitionConstants.DatasetMerger.OPTION_KEEP_FIRST;

    private Form<ModelObject> form;
    @Inject
    private NotebookSession notebookSession;

    public DatasetMergerCanvasItemPanel(String id, Long cellId) {
        super(id, cellId);
        setOutputMarkupId(true);
        addForm();
        addTitleBar();
        load();
    }

    private void load() {
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        form.setOutputMarkupId(true);
        TextField<String> mergeFieldName = new TextField<String>(OPT_MERGE_FIELD_NAME);
        form.add(mergeFieldName);
        CheckBox keepFirstField = new CheckBox(OPT_KEEP_FIRST);
        form.add(keepFirstField);
        add(form);
    }

    @Override
    public Form getExecuteFormComponent() {
        return form;
    }

    @Override
    public void onExecute() {
        form.getModelObject().store();
        VariableInstance outputVariableInstance = notebookSession.getCurrentNotebookInstance().findVariable(getCellInstance().getName(), CellDefinition.VAR_NAME_OUTPUT);
        outputVariableInstance.setValue(null);
        notebookSession.executeCell(getCellInstance().getId());
        fireContentChanged();
    }

    class ModelObject implements Serializable {

        private String mergeFieldName;
        private Boolean keepFirst;

        public Boolean getKeepFirst() {
            return keepFirst;
        }

        public void setKeepFirst(Boolean keepFirst) {
            this.keepFirst = keepFirst;
        }

        public String getMergeFieldName() {
            return mergeFieldName;
        }

        public void setMergeFieldName(String mergeFieldName) {
            this.mergeFieldName = mergeFieldName;
        }

        public void load() {

            keepFirst = (Boolean) getCellInstance().getOptionMap().get(OPT_KEEP_FIRST).getValue();
            mergeFieldName = (String) getCellInstance().getOptionMap().get(OPT_MERGE_FIELD_NAME).getValue();
        }

        public void store() {
            getCellInstance().getOptionMap().get(OPT_KEEP_FIRST).setValue(keepFirst);
            getCellInstance().getOptionMap().get(OPT_MERGE_FIELD_NAME).setValue(mergeFieldName);
        }
    }

}
