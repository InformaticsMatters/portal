package portal.notebook;

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
        String token = css + " ";
        if (toggler.cssActiveIf()) {
            if (currentValue == null) {
                result = css;
            } else {
                result = token + currentValue;
            }
        } else {
            if (currentValue != null && currentValue.contains(token)) {
                result = currentValue.replace(token, "");
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
