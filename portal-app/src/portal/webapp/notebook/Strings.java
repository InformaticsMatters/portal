package portal.webapp.notebook;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Strings implements Serializable {
    private static final long serialVersionUID = 1l;
    private final List<String> strings = new ArrayList<>();

    public int add(String string) {
        strings.add(string);
        return strings.size() - 1;
    }

    public void clear() {
        strings.clear();
    }

    public String[] getStrings() {
        return strings.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return Arrays.toString(getStrings());
    }


}
