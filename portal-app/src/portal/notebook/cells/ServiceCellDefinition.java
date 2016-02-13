package portal.notebook.cells;

import com.im.lac.job.jobdef.JobStatus;
import org.squonk.core.ServiceDescriptor;
import portal.notebook.api.CellDefinition;
import portal.notebook.api.CellExecutionData;
import portal.notebook.api.CellExecutor;

/**
 * @author simetrias
 */
public class ServiceCellDefinition extends CellDefinition {

    private ServiceDescriptor serviceDescriptor;

    public ServiceCellDefinition(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    @Override
    public CellExecutor getCellExecutor() {
        return new CellExecutor() {

            @Override
            public JobStatus execute(CellExecutionData data) throws Exception {
                return null;
            }
        };
    }

    @Override
    public String getName() {
        return serviceDescriptor.getName();
    }

    @Override
    public String getDescription() {
        return serviceDescriptor.getDescription();
    }

    @Override
    public Boolean getExecutable() {
        return true;
    }
}
