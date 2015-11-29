package portal.notebook.cells;

import java.util.List;

/** Probably need further sub-types rather than stuffing all the options in here. e.g. a MultiSelectCellOption
 *
 * Created by timbo on 29/11/2015.
 */
public interface CellOption<T> {

    String getName();
    String getDescription(); // e.g. for tooltip
    T getType(); // String, Integer etc.
    T getDefaultValue();
    String validate(T value); // returns explaination as to why value is invalid, or null if valid
    List<T> pickListValues(); // list of values to choose from

    // multi-select stuff
    int minValues(); // the min number of allowed values. normally 0 (meaning optional) or 1
    int maxValues(); // the max number of allowed values. normally 1, but potentially more e.g. for a multi-select.

    // filtering stuff
    List<T> filter(String text); // get a list or possible options matching the specified filter


}
