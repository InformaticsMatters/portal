package portal.notebook;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import portal.notebook.service.NotebookInfo;

import javax.inject.Inject;
import java.util.List;

/**
 * @author simetrias
 */
public class NotebookListPanel extends Panel {

    private static final Logger logger = LoggerFactory.getLogger(NotebookListPanel.class);
    @Inject
    private NotebookSession notebooksSession;

    public NotebookListPanel(String id) {
        super(id);
        addNotebookList();
    }

    private void addNotebookList() {
        List<NotebookInfo> notebookList = notebooksSession.listNotebookInfo();

        logger.info(notebookList.size() + " notebook/s found.");

        ListView<NotebookInfo> listView = new ListView<NotebookInfo>("notebook", notebookList) {

            @Override
            protected void populateItem(ListItem<NotebookInfo> listItem) {
                NotebookInfo notebookInfo = listItem.getModelObject();
                listItem.add(new Label("name", notebookInfo.getName()));
            }
        };
        add(listView);
    }
}
