package portal.notebook;

import org.apache.wicket.AttributeModifier;

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
            } else {
                result = css + ";" + currentValue;
            }
        } else {
            if (currentValue != null && currentValue.contains(css + ";")) {
                result = currentValue.replace(css + ";", "");
            } else {
                result = currentValue;
            }
        }
        return result;
    }

    public interface Toggler {

        boolean cssActiveIf();

    }
}
