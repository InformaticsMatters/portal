package portal.notebook;


import com.sun.jersey.api.client.Client;
import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import portal.notebook.api.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RestPicklistFieldEditorPanel  extends FieldEditorPanel {


    private final String queryUri;

    public RestPicklistFieldEditorPanel(String id, FieldEditorModel fieldEditorModel, String queryUri) {
        super(id, fieldEditorModel);
        this.queryUri = queryUri;
        addComponents();
    }

    private void addComponents() {
        Model model = new Model<String>() {
            @Override
            public String getObject() {
                return (String)getFieldEditorModel().getValue();
            }

            @Override
            public void setObject(String object) {
                getFieldEditorModel().setValue(object);
            }
        };
        add(new Label("label", getFieldEditorModel().getDisplayName()));
        TextChoiceProvider<String> provider = new TextChoiceProvider<String>(){
            @Override
            protected String getDisplayText(String string) {
                return string;
            }

            @Override
            protected Object getId(String string) {
                return string;
            }

            @Override
            public void query(String pattern, int i, Response<String> response) {
                Strings strings = Client.create().resource(queryUri).queryParam("query", pattern).get(Strings.class);
                response.addAll(Arrays.asList(strings.getList()));
                response.setHasMore(false);
            }

            @Override
            public Collection<String> toChoices(Collection<String> idCollection) {
                List<String> list = new ArrayList<String>();
                for (String id : idCollection) {
                    list.add(id);
                }
                return list;
            }
        };
        Select2Choice<String> select2Choice = new Select2Choice<>("picklist", model);
        select2Choice.setProvider(provider);
        select2Choice.getSettings().setMinimumInputLength(1);
        select2Choice.setOutputMarkupId(true);
        add(select2Choice);
    }
}
