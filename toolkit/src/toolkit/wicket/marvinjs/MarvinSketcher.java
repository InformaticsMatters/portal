package toolkit.wicket.marvinjs;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import toolkit.wicket.semantic.SemanticModalPanel;

import java.io.Serializable;

public class MarvinSketcher extends SemanticModalPanel {

    private static final int defaultHeight = 600;
    private static final int defaultWidth = 1000;
    private static final String SKETCHER_FRAME_ID = "sketcherFrame";
    private static final String PROMISE_MARVIN_CALL = "MarvinJSUtil.getEditor(':sketcherFrameId').then(function(instance){:javascript})";
    private Callbacks callbacks;
    private Form<SketcherFormModel> sketcherForm;
    private WebMarkupContainer sketcherContainer;

    public MarvinSketcher(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        sketcherContainer = new WebMarkupContainer("sketcherContainer");
        getModalRootComponent().add(sketcherContainer);
        addIframe();
        addForm();
        addActions();
    }

    private static String convertForJavaScript(String input) {
        String value = input;
        value = value.replace("\r\n", "\n");
        value = value.replace("\r", "\n");
        value = value.replace("\\", "\\\\");
        value = value.replace("\n", "\\n");
        value = value.replace("\"", "\\\"");
        value = value.replace("'", "\\'");
        return value;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new CssResourceReference(MarvinSketcher.class, "resources/css/doc-simetrias.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(MarvinSketcher.class, "resources/js/marvinjslauncher.js")));
    }

    @Override
    public void showModal() {
        super.showModal();
        getRequestCycle().find(AjaxRequestTarget.class).appendJavaScript(promiseMarvinCall("window.marvinSketcher = instance;"));
    }

    private void addIframe() {
        PackageResourceReference ref = new PackageResourceReference(MarvinSketcher.class, "resources/editor.html");
        String src = urlFor(ref, null).toString();
        WebMarkupContainer container = new WebMarkupContainer("sketcher");
        container.add(new AttributeModifier("src", src));
        sketcherContainer.add(container);
    }

    private void addForm() {
        sketcherForm = new Form<>("sketcherForm");
        sketcherForm.setModel(new CompoundPropertyModel<>(new SketcherFormModel()));
        sketcherForm.add(new HiddenField<String>("sketcherData"));
        getModalRootComponent().add(sketcherForm);
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    private void addActions() {
        AjaxSubmitLink acceptAction = new AjaxSubmitLink("accept") {

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                AjaxCallListener acl = new AjaxCallListener();
                acl.onBefore("$('#sketcherData').val(marvinSketcher.exportAsMrv())");
                attributes.getAjaxCallListeners().add(acl);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                callbacks.onSubmit();
            }
        };
        sketcherForm.add(acceptAction);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        sketcherForm.add(cancelAction);
    }

    public void setSketchData(AjaxRequestTarget ajaxRequestTarget, String data, String format) {
        if ("mol".equals(format)) {
            ajaxRequestTarget.appendJavaScript(promiseMarvinCall("instance.importAsMol('" + convertForJavaScript(data) + "');"));
        } else if ("mrv".equals(format)) {
            ajaxRequestTarget.appendJavaScript(promiseMarvinCall("instance.importAsMrv('" + convertForJavaScript(data) + "');"));
        }
    }

    public String getSketchData() {
        return sketcherForm.getModelObject().getSketcherData();
    }

    private String promiseMarvinCall(String javascript) {
        return PROMISE_MARVIN_CALL.replace(":sketcherFrameId", SKETCHER_FRAME_ID).replace(":javascript", javascript);
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onCancel();
    }

    private class SketcherFormModel implements Serializable {

        private String sketcherData;

        public String getSketcherData() {
            return sketcherData;
        }

        public void setSketcherData(String sketcherData) {
            this.sketcherData = sketcherData;
        }
    }

}
