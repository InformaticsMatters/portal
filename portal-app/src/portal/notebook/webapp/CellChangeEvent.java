package portal.notebook.webapp;

import org.squonk.jobdef.JobStatus;

/** Defines a change to a cell that another cell needs to know about.
 * This class is abstract and the actual change classes are the inner classes of this class.
 * See the docs for these for specific details.
 *
 *
 * Created by timbo on 30/10/2016.
 */
public abstract class CellChangeEvent {

    public enum BindingChangeType {Bind, Unbind}
    public static final String SOURCE_ALL_DATA = "__source__all_data_changed__";

    private final Long sourceCellId;

    protected CellChangeEvent(Long sourceCellId) {
        this.sourceCellId = sourceCellId;
    }

    /** The cell ID of the cell that changed and triggered this event.
     *
     * @return
     */
    public Long getSourceCellId() {
        return sourceCellId;
    }

    /** Abstract class for events where the event has a source name.
     *
     */
    static abstract class SourceNamedChangeEvent extends CellChangeEvent {

        private final String sourceName;

        /** If the source name is not known then use the @{link SOURCE_ALL_DATA} constant which should be
         * interpretted as all variables from that source cell have changed.
         *
         * @param sourceCellId
         * @param sourceName
         */
        public SourceNamedChangeEvent(Long sourceCellId, String sourceName) {
            super(sourceCellId);
            this.sourceName = sourceName;
        }

        /** The name of the input variable or option that changed, or the @{link SOURCE_ALL_DATA} constant
         * if its not known or all data has changed for the soruce cell.
         *
         * @return
         */
        public String getSourceName() {
            return sourceName;
        }

    }

    /** Abstract class for events that represent a change to a binding.
     *
     */
    static abstract class AbstractBindingEvent extends SourceNamedChangeEvent {

        private final Long targetCellId;
        private final String targetName;
        private final BindingChangeType type;

        public AbstractBindingEvent(Long sourceCellId, String sourceName, Long targetCellId, String targetName, BindingChangeType type) {
            super(sourceCellId, sourceName);
            this.targetCellId = targetCellId;
            this.targetName = targetName;
            this.type = type;
        }

        /** Defines whether the binding was added or removed
         */
        public BindingChangeType getType() {
            return type;
        }

        /** The cell ID of the target cell
         *
         * @return
         */
        public Long getTargetCellId() {
            return targetCellId;
        }

        /** The binding name of the target cell
         *
         * @return
         */
        public String getTargetName() {
            return targetName;
        }
    }

    /** Represents a change to a data binding, that is a binding that connects an output variable to
     * a data input
     *
     */
    public static class DataBinding extends AbstractBindingEvent {


        public DataBinding(Long sourceCellId, String sourceName, Long targetCellId, String targetName, BindingChangeType type) {
            super(sourceCellId, sourceName, targetCellId, targetName, type);
        }

        @Override
        public String toString() {
            return "CellChangeEvent.DataBinding [" + getSourceCellId() + ":" + getSourceName() + " " +
                    getTargetCellId() + ":" + getTargetName() + " " + getType() + "]";
        }
    }

    /** Represents a change to an option binding, that is a binding that connects and output option to
     * an option input
     *
     */
    public static class OptionBinding extends AbstractBindingEvent {


        public OptionBinding(Long sourceCellId, String sourceName, Long targetCellId, String targetName, BindingChangeType type) {
            super(sourceCellId, sourceName, targetCellId, targetName, type);
        }

        @Override
        public String toString() {
            return "CellChangeEvent.OptionBinding [" + getSourceCellId() + ":" + getSourceName() + " " +
                    getTargetCellId() + ":" + getTargetName() + " " + getType() + "]";
        }
    }

    /** Represents an event for when the values of an option change. e.g. the user makes a selection in a cell.
     *
     */
    static class OptionValues extends SourceNamedChangeEvent {

        public OptionValues(Long sourceCellId, String sourceName) {
            super(sourceCellId, sourceName);
        }

        @Override
        public String toString() {
            return "CellChangeEvent.OptionValues [" + getSourceCellId() + ":" + getSourceName() + "]";
        }
    }

    /** Represents an event where a variable value has changed, probably due to cell execution.
     *
     */
    static class DataValues extends SourceNamedChangeEvent {

        private final JobStatus.Status jobStatus;

        public DataValues(Long sourceCellId, String sourceName, JobStatus.Status jobStatus) {
            super(sourceCellId, sourceName);
            this.jobStatus = jobStatus;
        }

        /** The job status that results from execution. Can be null if not related to any execution
         * status change.
         *
         * @return
         */
        public JobStatus.Status getJobStatus() {
            return jobStatus;
        }

        @Override
        public String toString() {
            return "CellChangeEvent.DataValues [" + getSourceCellId() + ":" + getSourceName() + "]";
        }
    }
}
