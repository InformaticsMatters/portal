package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.squonk.types.depict.HTMLRenderers;

/**
 * Created by timbo on 05/10/16.
 */
public class ResultsUtils {

    private static final HTMLRenderers HTML = HTMLRenderers.getInstance();

    public static WebComponent generateContent(String id, Object value) {
        if (value == null) {
            return new Label(id, "");
        } else if (HTML.canRender(value.getClass())) {
            Label l = new Label(id, HTML.render(value));
            l.setEscapeModelStrings(false);
            return l;
        } else {
            return new MultiLineLabel(id, value.toString());
        }
    }
}
