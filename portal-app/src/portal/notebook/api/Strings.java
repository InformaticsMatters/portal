package portal.notebook.api;


import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XmlRootElement
public class Strings implements Serializable {
    private static final long serialVersionUID = 1l;
    private final List<String> list = new ArrayList<>();

    public int add(String string) {
        list.add(string);
        return list.size() - 1;
    }

    public void clear() {
        list.clear();
    }

    public String[] getList() {
        return list.toArray(new String[0]);
    }

    public void setList(String[] strings) {
        list.clear();
        list.addAll(Arrays.asList(strings));
    }

    @Override
    public String toString() {
        return Arrays.toString(getList());
    }


}
