package portal.notebook;

import org.apache.wicket.markup.html.form.Form;

import java.io.Serializable;

/**
 * @author simetrias
 */
public interface CellCallbackHandler extends Serializable {

    abstract void onRemove(CellModel cellModel);

    abstract void onEditBindings(CellModel cellModel);

    void onContentChanged();

    Form getExecuteFormComponent();

    void onExecute();


}
