package portal.notebook;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Logger;

public class ChemblActivitiesFetcherCanvasItemPanel extends CanvasItemPanel<ChemblActivitiesFetcherCellModel> {
    private static final Logger LOGGER = Logger.getLogger(PropertyCalculateCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;

    public ChemblActivitiesFetcherCanvasItemPanel(String id, ChemblActivitiesFetcherCellModel cell) {
        super(id, cell);
        addHeader();
        addForm();
        addListeners();
        load();
        setOutputMarkupId(true);
    }

    private void addHeader() {
        add(new Label("cellName", getCellModel().getName().toLowerCase()));
        add(new AjaxLink("remove") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                notebookSession.getNotebookModel().removeCell(getCellModel());
                notebookSession.storeNotebook();
                ajaxRequestTarget.add(ChemblActivitiesFetcherCanvasItemPanel.this);
            }
        });
    }

    private void addListeners() {
        notebookSession.getNotebookModel().addNotebookChangeListener(new NotebookChangeListener() {
            @Override
            public void onCellRemoved(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }

            @Override
            public void onCellAdded(CellModel cellModel) {
                RequestCycle.get().find(AjaxRequestTarget.class).add(form);
            }
        });
    }

    private void load() {
        form.getModelObject().setAssayId(getCellModel().getAssayId());
        form.getModelObject().setPrefix(getCellModel().getPrefix());
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        TextField<String> assayIdField = new TextField<String>("assayId");
        form.add(assayIdField);
        TextField<String> prefixField = new TextField<String>("prefix");
        form.add(prefixField);
        IndicatingAjaxSubmitLink executeLink = new IndicatingAjaxSubmitLink("execute") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                execute();
            }
        };
        form.add(executeLink);
        add(form);
    }

    private void execute() {
        getCellModel().setAssayId(form.getModelObject().getAssayId());
        getCellModel().setPrefix(form.getModelObject().getPrefix());
        VariableModel outputVariableModel = notebookSession.getNotebookModel().findVariable(getCellModel().getName(), "results");
        outputVariableModel.setValue(null);
        notebookSession.storeNotebook();
        notebookSession.executeCell(getCellModel().getName());
    }


    class ModelObject implements Serializable {
        private VariableModel inputVariable;
        private String assayId;
        private String prefix;

        public VariableModel getInputVariable() {
            return inputVariable;
        }

        public void setInputVariable(VariableModel inputVariable) {
            this.inputVariable = inputVariable;
        }

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
    }

}
