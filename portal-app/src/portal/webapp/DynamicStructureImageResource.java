package portal.webapp;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.request.resource.DynamicImageResource;
import portal.service.api.DatasetService;
import portal.service.api.PropertyDescriptor;
import portal.service.api.Row;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;

public class DynamicStructureImageResource extends DynamicImageResource {

    public static final Rectangle RECTANGLE = new Rectangle(200, 130);
    @Inject
    private DatasetService service;

    public DynamicStructureImageResource() {
        CdiContainer.get().getNonContextualManager().postConstruct(this);
    }

    private static BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    @Override
    protected void setResponseHeaders(ResourceResponse data, Attributes attributes) {
        // this disables some unwanted default caching
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        String datasetIdAsString = attributes.getParameters().get("datasetIdAsString").toString();
        String rowIdAsString = attributes.getParameters().get("rowIdAsString").toString();
        try {
            return renderStructure(datasetIdAsString, rowIdAsString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String loadStructureData(String datasetIdAsString, String rowIdAsString) {
        String structureData = null;
        Long datasetId = Long.valueOf(datasetIdAsString);
        Long rowId = Long.valueOf(rowIdAsString);
        Row row = service.findRowById(datasetId, rowId);
        if (row != null) {
            PropertyDescriptor propertyDescriptor = row.getDescriptor().findPropertyDescriptorById(PropertyDescriptor.STRUCTURE_PROPERTY_ID);
            structureData = (String) row.getProperty(propertyDescriptor);
        }
        return structureData;
    }

    protected Rectangle getRectangle() {
        return RECTANGLE;
    }

    protected Molecule getMolecule(String datasetIdAsString, String rowIdAsString) throws Exception {
        String structureAsString = loadStructureData(datasetIdAsString, rowIdAsString);
        Molecule molecule = MolImporter.importMol(structureAsString);
        molecule.dearomatize();
        return molecule;
    }

    private byte[] renderStructure(String datasetIdAsString, String rowIdAsString) throws Exception {
        MolPrinter molPrinter = new MolPrinter();
        molPrinter.setMol(getMolecule(datasetIdAsString, rowIdAsString));

        BufferedImage image = new BufferedImage(getRectangle().width, getRectangle().height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setColor(Color.white);
        graphics2D.fillRect(0, 0, getRectangle().width, getRectangle().height);
        double scale = molPrinter.maxScale(getRectangle());
        molPrinter.setScale(scale);
        molPrinter.paint(graphics2D, getRectangle());

        BufferedImage filteredImage = imageToBufferedImage(makeWhiteTransparent(image));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(filteredImage, "png", outputStream);
        return outputStream.toByteArray();
    }

    public Image makeWhiteTransparent(BufferedImage bufferedImage) {
        ImageFilter filter = new RGBImageFilter() {

            private int markerRGB = 0xFFFFFFFF;

            @Override
            public int filterRGB(final int x, final int y, final int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(bufferedImage.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }
}

