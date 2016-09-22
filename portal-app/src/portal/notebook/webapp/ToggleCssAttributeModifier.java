package portal.notebook.webapp;

import org.apache.wicket.AttributeModifier;

import java.io.Serializable;

/**
 * Toggles a CSS attribute from a set of attributes, keeping the other ones that are present.
 * <p>Most commonly used for toggling a CSS class. For instance, if the current class
 * definition looks like this:
 * <code>class="foo bar baz"</code>
 * then toggling the bar class can be done, and toggling it off would result in
 * <code>class="foo baz"</code>
 * and toggling it back on would result in
 * <code>class="bar foo baz"</code>
 * </p>
 * <p>
 * Note that the order of the elements is not preserved. The added term is always added at the front.
 * <br>
 * Note that the replacement logic is very simplistic and cannot handling overlapping terms. e.g.
 * in the example above if there was a class named bar-big then the result would not be what was
 * desired (this can potentially be improved if necessary by smarter use of regular expressions).
 * </p>
 *
 * @author simetrias
 */
public class ToggleCssAttributeModifier extends AttributeModifier {

    private final Toggler toggler;
    private final String css;

    /**
     *
     * @param attribute The attribute name
     * @param css The value to toggle on or off
     * @param toggler Detemines whether to toggle on or off
     */
    public ToggleCssAttributeModifier(String attribute, String css, Toggler toggler) {
        super(attribute, "");
        this.toggler = toggler;
        this.css = css;
    }

    /** Toggler for the class attribute
     *
     * @param css The value to toggle on or off
     * @param toggler Detemines whether to toggle on or off
     */
    public ToggleCssAttributeModifier(String css, Toggler toggler) {
        this("class", css, toggler);
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
