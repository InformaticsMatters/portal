package portal.notebook.webapp;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import portal.PopupContainerProvider;
import portal.notebook.api.CellInstance;
import portal.notebook.service.Execution;
import portal.notebook.webapp.cell.CellDescriptionEditorPanel;
import toolkit.wicket.semantic.IndicatingAjaxSubmitLink;
import toolkit.wicket.semantic.NotifierProvider;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class CellTitleBarPanel extends Panel {
    private static final Logger LOG = Logger.getLogger(CellTitleBarPanel.class.getName());
    public static final String SETTING_DESCRIPTION = "cellDescription";
    private static final String SETTING_SHOW_DESCRIPTION = "showCellDescription";
    private static final String SETTING_SHOW_CONTENT = "showCellContent";

    private final Long cellId;
    private final CallbackHandler callbackHandler;
    private BindingsPopupPanel bindingsPopupPanel;
    private AdvancedPopupPanel advancedPopupPanel;
    private MultiLineLabel descriptionLabel;
    private IndicatingAjaxSubmitLink submitLink;
    private AjaxLink waitLink;
    @Inject
    private PopupContainerProvider popupContainerProvider;
    @Inject
    private NotebookSession notebookSession;
    @Inject
    private NotifierProvider notifierProvider;

    public CellTitleBarPanel(String id, CellInstance cellInstance, CallbackHandler callbackHandler) {
        super(id);
        setOutputMarkupId(true);
        this.cellId = cellInstance.getId();
        this.callbackHandler = callbackHandler;
        addTitleBarCssToggler();
        addControls(cellInstance);
        initExecutionStatus();
    }

    private CellInstance findCellInstance() {
        return notebookSession.getCurrentNotebookInstance().findCellInstanceById(cellId);
    }

    private void initExecutionStatus() {
        try {
            Execution lastExecution = notebookSession.findExecution(cellId);
            applyExecutionStatus(lastExecution);
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error findingExecution", t);
            notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
        }
    }

    private void addTitleBarCssToggler() {
        this.add(new ToggleCssAttributeModifier("failed-class", new ToggleCssAttributeModifier.Toggler() {

            @Override
            public boolean cssActiveIf() {
                try {
                    return isFailed();
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Error checking status", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                    return true;
                }
            }
        }));
    }

    private void addControls(CellInstance cellInstance) {
        add(new Label("cellName", cellInstance.getName().toLowerCase()));

        bindingsPopupPanel = new BindingsPopupPanel("content", cellInstance);

        AjaxLink advancedLink = new AjaxLink("advanced") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                onAdvancedLinkClicked(this, ajaxRequestTarget);
            }
        };
        advancedLink.setOutputMarkupId(true);
        add(advancedLink);
        Panel advancedOptionsPanel = callbackHandler.getAdvancedOptionsPanel();
        if (advancedOptionsPanel != null) {
            advancedPopupPanel = new AdvancedPopupPanel("content", cellInstance, advancedOptionsPanel);
            advancedLink.setVisible(true);
        } else {
            advancedLink.setVisible(false);
        }

        submitLink = new IndicatingAjaxSubmitLink("submit", callbackHandler.getExecuteFormComponent()) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    callbackHandler.onExecute();
                    target.add(CellTitleBarPanel.this);
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Error executing " + findCellInstance().getName(), t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        };
        submitLink.setOutputMarkupId(true);
        add(submitLink);

        waitLink = new AjaxLink("wait") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {

            }
        };
        waitLink.setEnabled(false);
        waitLink.setOutputMarkupId(true);
        add(waitLink);

        add(new AjaxLink("expand") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                try {
                    callbackHandler.onShowResults();
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Error showing dataset details panel", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });

        boolean showDescription = findShowElement(cellInstance, SETTING_SHOW_DESCRIPTION, false);
        boolean showContent = findShowElement(cellInstance, SETTING_SHOW_CONTENT, true);
        System.out.println("Show description: " + showDescription);
        descriptionLabel = new MultiLineLabel("descriptionContent", new IModel<String>() {

            @Override
            public void detach() {
            }

            @Override
            public String getObject() {
                CellInstance cellInstance = findCellInstance();
                return cellInstance == null ? "" : findDescription(cellInstance);
            }

            @Override
            public void setObject(String o) {
            }
        });
        descriptionLabel.add(new ToggleCssAttributeModifier("hidden", (ToggleCssAttributeModifier.Toggler) () -> {
            boolean b = findShowElement(findCellInstance(), SETTING_SHOW_DESCRIPTION, false);
            System.out.println("Description should be visible: " + b);
            return !b;
        }
        ));

        descriptionLabel.setOutputMarkupId(true);
        add(descriptionLabel);

        WebMarkupContainer contentPanel = callbackHandler.getContentPanel();
        if (contentPanel != null) {
            System.out.println("Setting content visible: " + showContent);
            contentPanel.setVisible(showContent);
        }

        Form form = new Form("form");
        add(form);


        CheckBox showDescriptionCheckbox = new CheckBox("showDescription", new Model<>(showDescription));
        form.add(showDescriptionCheckbox);
        CheckBox showContentCheckbox = new CheckBox("showContent", new Model<>(showContent));
        form.add(showContentCheckbox);

        form.add(new AjaxSubmitLink("changeVisibilityButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                boolean newDescriptionVisible = showDescriptionCheckbox.getModelObject();
                boolean newContentVisible = showContentCheckbox.getModelObject();
                System.out.println("Setting description visible: " + newDescriptionVisible);

                CellInstance cellInstance = findCellInstance();
                cellInstance.getSettings().put(CellTitleBarPanel.SETTING_SHOW_DESCRIPTION, newDescriptionVisible);
                cellInstance.getSettings().put(CellTitleBarPanel.SETTING_SHOW_CONTENT, newContentVisible);

                if (contentPanel != null) {
                    System.out.println("Setting content visible: " + newContentVisible);
                    contentPanel.setVisible(newContentVisible);
                }

                target.add(CellTitleBarPanel.this.getParent());

                try {
                    NotebookCanvasPage page = (NotebookCanvasPage) getPage();
                    page.getNotebookSession().storeCurrentEditable();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error saving panels visibility", e);
                    notifierProvider.getNotifier(getPage()).notify("Error saving panels visibility", e.getMessage());
                }
            }
        });

        form.add(new AjaxSubmitLink("editDescriptionButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                System.out.println("Need to edit description");
                CellDescriptionEditorPanel editor = ((NotebookCanvasPage) getPage()).getCellDescriptionEditorPanel();
                if (editor != null) {
                    editor.configure(cellId, CellTitleBarPanel.this);
                    editor.showModal();
                }
            }
        });

        form.add(new AjaxSubmitLink("bindingsButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                System.out.println("Need to edit bindings");
                try {
                    onBindingsLinkClicked(this, target);
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Error removing cell", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });

        form.add(new AjaxSubmitLink("deleteButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                System.out.println("Need to delete cell");
                try {
                    getCallbackHandler().onRemove(findCellInstance());
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Error removing cell", t);
                    notifierProvider.getNotifier(getPage()).notify("Error", t.getMessage());
                }
            }
        });
    }

    private String findDescription(CellInstance cellInstance) {
        String description = (String) cellInstance.getSettings().get(SETTING_DESCRIPTION);
        if (description == null) {
            description = cellInstance.getCellDefinition().getDescription();
        }
        if (description == null) {
            description = "No description found";
        }
        System.out.println("Description: " + description);
        return description;
    }

    private boolean findShowElement(CellInstance cellInstance, String prop, boolean defaultValue) {
        Boolean showDescription = (Boolean) cellInstance.getSettings().get(prop);
        return showDescription == null ? defaultValue : showDescription;
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        // enable the dropdown menu - maybe a better place to do this?
        response.render(OnDomReadyHeaderItem.forScript(
                "$('#" + getMarkupId() +
                        " .ui.dropdown').dropdown({action: 'hide', onChange: function(value, text) {" +
                        "applyCellMenuAction('" + getParent().getMarkupId() + "', value);" +
                        "}})"));
    }

    private void onBindingsLinkClicked(AbstractLink link, AjaxRequestTarget ajaxRequestTarget) {
        popupContainerProvider.setPopupContentForPage(getPage(), bindingsPopupPanel);
        popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
        String js = "$('#:link')" +
                ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.bindingsPopup.popup'), on : 'click'})" +
                ".popup('toggle').popup('destroy')";
        js = js.replace(":link", link.getMarkupId()).replace(":content", bindingsPopupPanel.getMarkupId());
        ajaxRequestTarget.appendJavaScript(js);
    }

    private void onAdvancedLinkClicked(AjaxLink link, AjaxRequestTarget ajaxRequestTarget) {
        popupContainerProvider.setPopupContentForPage(getPage(), advancedPopupPanel);
        popupContainerProvider.refreshContainer(getPage(), ajaxRequestTarget);
        String js = "$('#:link')" +
                ".popup({simetriasPatch: true, popup: $('#:content').find('.ui.cellPopup.popup'), on : 'click'})" +
                ".popup('toggle').popup('destroy')";
        js = js.replace(":link", link.getMarkupId()).replace(":content", advancedPopupPanel.getMarkupId());
        ajaxRequestTarget.appendJavaScript(js);
    }

    public void applyExecutionStatus(Execution execution) {
        boolean active = execution != null && execution.getJobActive();
        waitLink.setVisible(active);
        submitLink.setVisible(!active);
    }

    private boolean isFailed() {
        Execution lastExecution = notebookSession.findExecution(cellId);
        return lastExecution != null && Boolean.FALSE.equals(lastExecution.getJobSuccessful());
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public interface CallbackHandler extends Serializable {

        void onRemove(CellInstance cellModel) throws Exception;

        Form getExecuteFormComponent();

        void onExecute() throws Exception;

        default Panel getAdvancedOptionsPanel() {
            return null;
        }

        WebMarkupContainer getContentPanel();

        void onShowResults() throws Exception;
    }

}
