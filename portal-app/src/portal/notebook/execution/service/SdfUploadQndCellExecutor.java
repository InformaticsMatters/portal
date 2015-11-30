package portal.notebook.execution.service;

import com.squonk.notebook.api.CellType;
import com.squonk.notebook.client.CallbackClient;

import javax.inject.Inject;

public class SdfUploadQndCellExecutor implements QndCellExecutor {
    @Inject
    private CallbackClient callbackClient;

    @Override
    public boolean handles(CellType cellType) {
        return "SdfUploader".equals(cellType.getName());
    }


    @Override
    public void execute(String cellName) {

    }

}