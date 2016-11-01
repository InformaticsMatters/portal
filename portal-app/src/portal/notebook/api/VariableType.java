package portal.notebook.api;

import org.squonk.dataset.Dataset;
import org.squonk.types.BasicObject;
import org.squonk.types.MoleculeObject;

import java.io.InputStream;

public enum VariableType {


    STRING("Text", String.class),
    DATASET_MOLS("Dataset:Mols", Dataset.class, MoleculeObject.class),
    DATASET_ANY("Dataset", Dataset.class) {
        public boolean supports(VariableType other) {
            if (super.supports(other) || other.primaryType == Dataset.class) {
                return true;
            }
            return false;
        }
    },
    DATASET_BASIC("Dataset:Basic", Dataset.class, BasicObject.class) {
        public boolean supports(VariableType other) {
            if (super.supports(other) || other.primaryType == Dataset.class) {
                return true;
            }
            return false;
        }
    },
    FILE("File", InputStream.class);

    Class primaryType;
    Class genericType;
    String name;

    VariableType(String name, Class primaryType) {
        this.name = name;
        this.primaryType = primaryType;
    }

    VariableType(String name, Class primaryType, Class genericType) {
        this.name = name;
        this.primaryType = primaryType;
        this.genericType = genericType;
    }

    public String getName() {
        return name;
    }

    public boolean supports(VariableType other) {
        return other == this;
    }


    private final static long serialVersionUID = 1l;

}
