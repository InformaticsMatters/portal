package portal.notebook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.notebook.api.AbstractNotebookVersionDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;
import portal.notebook.webapp.HistoryTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HistoryHarness {

    public static void main(String[] args) throws IOException {
        Map<Long, AbstractNotebookVersionDTO> versionMap = new HashMap<>();
        versionMap.put(1l, new NotebookEditableDTO(1l, 1l, null, null, new Date(), null, null));
        versionMap.put(2l, new NotebookEditableDTO(2l, 1l, null, null, new Date(), null, null));
        versionMap.put(3l, new NotebookSavepointDTO(3l, 1l, 2l, null, new Date(), null, "sp1", null, null));
        versionMap.put(4l, new NotebookSavepointDTO(4l, 1l, 1l, null, new Date(), null, "sp2", null, null));
        versionMap.put(5l, new NotebookEditableDTO(5l, 1l, 3l, null, new Date(), null, null));
        versionMap.put(6l, new NotebookSavepointDTO(6l, 1l, 5l, null, new Date(), null, null, null, null));

        HistoryTree historyTree = new HistoryTree();
        historyTree.setName("A NB");
        historyTree.loadVersionMap(versionMap);

        ObjectMapper objectMapper = new ObjectMapper();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(byteArrayOutputStream, historyTree);
        byteArrayOutputStream.flush();

        System.out.println(new String(byteArrayOutputStream.toByteArray()));
        System.out.println("-------------------");

        String treesString = historyTree.toTreesString();
        System.out.println(treesString);
        System.out.println("-------------------");


    }
}
