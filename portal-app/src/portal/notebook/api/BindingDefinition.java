package portal.notebook.api;


import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Defines the binding of a cell. The binding allows variables to be bound as inputs.
 * A binding definition has a name to identify it (each binding for a cell must have a unique name) and one or more pairs
 * of primary and secondary types. The primary type is the Java class that the input needs and the optional seconday type
 * is the generic class of that primary type (or null if not appropriate).
 * For example, if the cell needs and input of integers then the primate type would be java.lang.Integer and the secondary
 * type would be null as Integer does not support generics. If the input needed to be a lsit of integers then the primary
 * type would be java.util.List and the secondary type would be java.lang.Integer.
 * The most common data type used is @{link org.squonk.dataset.Dataset}, which would have have a secondary tpye of
 * @{link org.squonk.types.BasicObject} or one of its subclasses.
 *
 * A binding can support multiple input types as alternatives. If so then it knows how to read each of these. For instance
 * it could specify java.lang.Integer and java.lang.String (both with null secondary types) and it knows that if it gets a
 * String it would convert to an integer.
 *
 */
@XmlRootElement
public class BindingDefinition implements Serializable {
    private final static long serialVersionUID = 1l;
    private String name;
    private final List<Type> acceptedVariableTypeList = new ArrayList<>();

    public BindingDefinition() {
    }

    public BindingDefinition(String name) {
        this.name = name;
    }

    /** Convenience constructor for one input type
     *
     * @param name
     * @param primary The input's primary type
     * @param secondary The input's secondary type
     */
    public BindingDefinition(String name, Class primary, Class secondary) {
        this(name);
        this.acceptedVariableTypeList.add(new Type(primary, secondary));
    }

    /** Convenience constructor for two input types
     *
     * @param name
     * @param primary1 The primary type of the input's first accepted type
     * @param secondary1 The secondary type of the input's first accepted type
     * @param primary2 The primary type of the input's second accepted type
     * @param secondary2 The secondary type of the input's second accepted type
     */
    public BindingDefinition(String name, Class primary1, Class secondary1, Class primary2, Class secondary2) {
        this(name);
        this.acceptedVariableTypeList.add(new Type(primary1, secondary1));
        this.acceptedVariableTypeList.add(new Type(primary2, secondary2));
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

   public boolean supports(IODescriptor iod) {
       for (Type t : acceptedVariableTypeList) {
           if (IODescriptors.supports(t.primary, t.secondary, iod.getPrimaryType(), iod.getSecondaryType())) {
               return true;
           }
       }
       return false;
   }

    public List<Type> getAcceptedVariableTypeList() {
        return acceptedVariableTypeList;
    }

    public class Type implements Serializable {
        private final Class primary;
        private final Class secondary;

        Type(Class primary, Class secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        public Class getPrimary() {
            return primary;
        }

        public Class getSecondary() {
            return secondary;
        }
    }
}
