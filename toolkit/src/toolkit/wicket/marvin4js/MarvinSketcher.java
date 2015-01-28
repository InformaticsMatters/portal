package toolkit.wicket.marvin4js;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
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
    private static final String ASYNC_INSTALL = "" +
            "getMarvinPromise(':sketcherFrameId').done(function(instance) {" +
            "    window.marvinJavascriptSketcher=instance;" +
            "});";
    private static final String ASYNC_JAVASCRIPT = "" +
            "getMarvinPromise(':sketcherFrameId').done(function(instance) {" +
            "    :javascript" +
            "});";

    private CallbackHandler callbackHandler;
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
        response.render(CssHeaderItem.forReference(new CssResourceReference(MarvinSketcher.class, "resources/css/doc.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(MarvinSketcher.class, "resources/js/util.js")));
        response.render(OnDomReadyHeaderItem.forScript(ASYNC_INSTALL.replace(":sketcherFrameId", SKETCHER_FRAME_ID)));
    }

    private void addIframe() {
        PackageResourceReference ref = new PackageResourceReference(MarvinSketcher.class, "resources/editor.html");
        String src = urlFor(ref, null).toString();
        WebMarkupContainer container = new WebMarkupContainer("sketcher");
        container.add(new AttributeModifier("src", src));
        sketcherContainer.add(container);
    }

    private void addForm() {
        sketcherForm = new Form<SketcherFormModel>("sketcherForm");
        sketcherForm.setModel(new CompoundPropertyModel<SketcherFormModel>(new SketcherFormModel()));
        sketcherForm.add(new HiddenField<String>("sketcherData"));
        getModalRootComponent().add(sketcherForm);
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    private void addActions() {
        AjaxSubmitLink acceptAction = new AjaxSubmitLink("accept") {

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                AjaxCallListener acl = new AjaxCallListener();
                acl.onBefore("$('#sketcherData').val(window.marvinJavascriptSketcher.exportAsMrv());");
                attributes.getAjaxCallListeners().add(acl);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                callbackHandler.onAcceptAction(target);
            }
        };
        sketcherForm.add(acceptAction);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbackHandler.onCancelAction(ajaxRequestTarget);
            }
        };
        sketcherForm.add(cancelAction);
    }

    public void setData(AjaxRequestTarget ajaxRequestTarget, String data, String format) {
        if ("mol".equals(format)) {
            ajaxRequestTarget.appendJavaScript(asyncJavascript("instance.importAsMol('" + convertForJavaScript(data) + "');"));
        } else if ("mrv".equals(format)) {
            ajaxRequestTarget.appendJavaScript(asyncJavascript("instance.importAsMrv('" + convertForJavaScript(data) + "');"));
        }
    }

    private String asyncJavascript(String javascript) {
        return ASYNC_JAVASCRIPT.replace(":sketcherFrameId", SKETCHER_FRAME_ID).replace(":javascript", javascript);
    }

    public String getSketcherData() {
        return sketcherForm.getModelObject().getSketcherData();
    }

    public interface CallbackHandler extends Serializable {

        void onAcceptAction(AjaxRequestTarget ajaxRequestTarget);

        void onCancelAction(AjaxRequestTarget ajaxRequestTarget);
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
