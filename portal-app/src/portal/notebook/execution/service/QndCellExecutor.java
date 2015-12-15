package portal.notebook.execution.service;

import org.squonk.notebook.api.CellType;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface QndCellExecutor {

    void execute(String cellName);
    boolean handles(CellType cellType);
}
