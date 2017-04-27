package portal.notebook.webapp.cell.visual.ngl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by timbo on 24/03/17.
 */
public class NglMoleculeSet implements Serializable {

    private final String mediaType;
    private final String extension;
    private final String molecules;
    private final Integer size;

    public NglMoleculeSet(
            @JsonProperty("mediaType") String mediaType,
            @JsonProperty("extension") String extension,
            @JsonProperty("molecules") String molecules,
            @JsonProperty("size") Integer size
            ) {
        this.mediaType = mediaType;
        this.extension = extension;
        this.molecules = molecules;
        this.size = size;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMolecules() {
        return molecules;
    }

    public Integer getSize() {
        return size;
    }
}
