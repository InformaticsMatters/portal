package portal.notebook.webapp;

import org.apache.wicket.AttributeModifier;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ToggleCssAttributeModifier extends AttributeModifier {

    private final Toggler toggler;
    private final String css;

    public ToggleCssAttributeModifier(String css, Toggler toggler) {
        super("class", "");
        this.toggler = toggler;
        this.css = css;
    }

    @Override
    protected String newValue(String currentValue, String replacementValue) {
        String result = null;
        if (toggler.cssActiveIf()) {
            if (currentValue == null) {
                result = css;
            } else if (currentValue.contains(css)) {
                // already contains. strange but we'll allow it
                result = currentValue;
            } else {
                result = css + " " + currentValue;
            }
        } else {
            if (currentValue != null && currentValue.contains(css)) {
                result = currentValue.replaceAll(" *" + css + " *", " ");
                result = result.replaceAll(" +", " ");
                result = result.trim();
            } else {
                result = currentValue;
            }
        }
        return result;
    }

    public interface Toggler extends Serializable {

        boolean cssActiveIf();

    }
}
