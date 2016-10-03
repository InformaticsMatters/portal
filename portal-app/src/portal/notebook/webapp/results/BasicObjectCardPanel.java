package portal.notebook.webapp.results;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.squonk.types.BasicObject;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by timbo on 31/08/16.
 */
public class BasicObjectCardPanel<T extends BasicObject> extends Panel {

    protected final T o;
    protected final Map<String, Class> classMappings = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public BasicObjectCardPanel(String id, Map<String, Class> classMappings, T o) {
        super(id);
        this.classMappings.putAll(classMappings);
        this.o = o;
        addContent(o);
    }

    private void addContent(T o) {

        handleMainContent(o);

        add(new Label("uuid", o.getUUID()));

        RepeatingView tableRows = new RepeatingView("fields");
        add(tableRows);

        classMappings.forEach((String k, Object v) -> {
            WebMarkupContainer props = new WebMarkupContainer(tableRows.newChildId());
            tableRows.add(props);
            props.add(new Label("fieldkey", k));
            Object value = o.getValue(k);
            props.add(new MultiLineLabel("fieldvalue", value == null ? "" : value.toString()));
        });
    }

    protected void handleMainContent(T o) {
        // noop for BasicObject
    }

}
