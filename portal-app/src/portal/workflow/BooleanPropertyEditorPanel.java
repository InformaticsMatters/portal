package portal.workflow;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.squonk.options.OptionDescriptor;

/**
 * @author simetrias
 */
public class BooleanPropertyEditorPanel extends Panel {

    private IModel<String> servicePropertyModel;

    public BooleanPropertyEditorPanel(String id, OptionDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        super(id);
        addComponents(servicePropertyDescriptor, servicePropertyModel);
    }

    private void addComponents(OptionDescriptor servicePropertyDescriptor, IModel<String> servicePropertyModel) {
        this.servicePropertyModel = servicePropertyModel;
        add(new Label("label", servicePropertyDescriptor.getLabel()));
        IModel<Boolean> model = new IModel<Boolean>() {
            @Override
            public Boolean getObject() {
                String string = servicePropertyModel.getObject();
                return string == null ? Boolean.FALSE : Boolean.valueOf(string);
            }

            @Override
            public void setObject(Boolean aBoolean) {
                servicePropertyModel.setObject(aBoolean.toString());
            }

            @Override
            public void detach() {

            }
        };
        CheckBox nombre = new CheckBox("boolean", model);
        add(nombre);
    }
}
