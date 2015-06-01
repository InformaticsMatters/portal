package portal.webapp;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.MolPrinter;
import chemaxon.struc.Molecule;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import toolkit.wicket.marvin4js.MarvinSketcher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;

/**
 * @author simetrias
 */
public class ServiceCanvasItemPopupPanel extends Panel {

    public static final Rectangle RECTANGLE = new Rectangle(200, 130);

    private ServiceCanvasItemPanel.Callbacks callbacks;
    private MarvinSketcher marvinSketcherPanel;

    public ServiceCanvasItemPopupPanel(String id, ServiceCanvasItemPanel.Callbacks callbacks) {
        super(id);
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);

        add(new AjaxLink("delete") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onDelete();
            }
        });

        add(new AjaxLink("sketcher") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                marvinSketcherPanel.showModal();
            }
        });

        marvinSketcherPanel = new MarvinSketcher("marvinSketcherPanel", "modalElement");
        marvinSketcherPanel.setCallbacks(new MarvinSketcher.Callbacks() {

            @Override
            public void onSubmit() {
            }

            @Override
            public void onCancel() {
            }
        });
        add(marvinSketcherPanel);

    }

    private byte[] renderStructure(String structureAsString) throws Exception {
        MolPrinter molPrinter = new MolPrinter();
        Molecule molecule = MolImporter.importMol(structureAsString);
        molecule.dearomatize();
        molPrinter.setMol(molecule);

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

    protected Rectangle getRectangle() {
        return RECTANGLE;
    }

    private Image makeWhiteTransparent(BufferedImage bufferedImage) {
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

    private BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }
}
