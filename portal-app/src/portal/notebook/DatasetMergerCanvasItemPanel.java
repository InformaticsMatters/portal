package portal.notebook;

import com.squonk.notebook.api.VariableType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Logger;

public class DatasetMergerCanvasItemPanel extends CanvasItemPanel {
    private static final Logger LOGGER = Logger.getLogger(DatasetMergerCanvasItemPanel.class.getName());
    @Inject
    private NotebookSession notebookSession;
    private Form<ModelObject> form;

    public DatasetMergerCanvasItemPanel(String id, CellModel cell) {
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
        form.getModelObject().load();
    }

    private void addForm() {
        form = new Form<ModelObject>("form", new CompoundPropertyModel<ModelObject>(new ModelObject()));
        form.setOutputMarkupId(true);
        TextField<String> mergeFieldName = new TextField<String>("mergeFieldName");
        form.add(mergeFieldName);
        CheckBox keepFirstField = new CheckBox("keepFirst");
        form.add(keepFirstField);
        IndicatingAjaxSubmitLink executeLink = new IndicatingAjaxSubmitLink("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                execute();
            }
        };
        executeLink.setOutputMarkupId(true);
        add(executeLink);
        add(form);
    }

    private void execute() {
        // hack until CellType supports bindings
        if (getCellModel().getBindingModelList().isEmpty()) {
            for (int i = 0; i < 5; i++) {
                BindingModel bindingModel = new BindingModel();
                bindingModel.setDisplayName("Dataset");
                bindingModel.setName("dataset-" + (i + 1));
                bindingModel.setVariableType(VariableType.DATASET);
                getCellModel().getBindingModelList().add(bindingModel);
            }
        }
        form.getModelObject().store();
        VariableModel outputVariableModel = notebookSession.getNotebookModel().findVariableModel(getCellModel().getName(), "results");
        outputVariableModel.setValue(null);
        notebookSession.storeNotebook();
        notebookSession.executeCell(getCellModel().getName());
        notebookSession.reloadNotebook();
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
            keepFirst = (Boolean) getCellModel().getOptionMap().get("keepFirst");
            mergeFieldName = (String) getCellModel().getOptionMap().get("mergeFieldName");
        }

        public void store() {
            getCellModel().getOptionMap().put("keepFirst", keepFirst);
            getCellModel().getOptionMap().put("mergeFieldName", mergeFieldName);
        }
    }

}
