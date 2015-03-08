package toolkit.wicket.semantic;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.Form;


public class IndicatingAjaxSubmitLink extends AjaxSubmitLink implements IAjaxIndicatorAware {

    private final AjaxIndicatorAppender ajaxIndicatorAppender;

    public IndicatingAjaxSubmitLink(String id) {
        super(id);
        ajaxIndicatorAppender = new AjaxIndicatorAppender();
        add(ajaxIndicatorAppender);
    }

    public IndicatingAjaxSubmitLink(String id, Form<?> form) {
        super(id, form);
        ajaxIndicatorAppender = new AjaxIndicatorAppender();
        add(ajaxIndicatorAppender);
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return ajaxIndicatorAppender.getMarkupId();
    }
}
