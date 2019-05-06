package org.chursj.search.poc;

import com.rometools.utils.Strings;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileSystemUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Log4j(topic = "non")
@Log(topic = "none")
public class PDFExtractMain {

    // TODO NMO: tesseract path will be given by parameter
    // inputs & outputs will be give by parameters
    // @SuppressWarnings({ "deprecation", "resource" })
    private static PdfContentIndexer indexer = new PdfContentIndexer();

    public static void main(String[] args) throws Exception {

        /*
        final Properties ppties = PDFExtractMain.fwkProperties();
        String[] excludeWords = ((String) ppties.get("excluded.words.list")).split(",");
        String[] posList = ((String) ppties.get("part.of.speech.list")).split(",");
        int extractLevel = (int) ppties.get("extract.deep.level");
        String inputFolder = (String) ppties.get("pdfs.inputs"); // args[0]; //"./input/";
        String tessPath = (String) ppties.get("tesseract.path");
        String serverURL = (String) ppties.get("db.url");
        String mode = (String) ppties.get("content.extract.mode");*/

        String[] excludeWords = new String[]{""};
        String[] posList = new String[]{""};
        int extractLevel = 1;
        String inputFolder = "./articles/"; // args[0]; //"./input/";
        String tessPath = "./tessdata/";
        String serverURL = "http://localhost:9200";
        String mode = "";
        String pathToTesseract = "/usr/share/tesseract-ocr";

        String outputFolder = "./output/initial"; // args[1]; // "./output/gen";
        ArrayList<ExtractContentBean> allWords = new ArrayList<>();
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        System.setProperty("TESSDATA_PREFIX", "./tessdata");
        System.setProperty("LC_ALL", "C");
        System.setProperty("OMP_NUM_THREADS", "1");

        File outFolder = new File(outputFolder);
        if (outFolder.exists()) {
            FileSystemUtils.deleteRecursively(outFolder);
        }

        try {

            for (final File fileEntry : new File(inputFolder).listFiles()) {
                final String pdfFilename = fileEntry.getName();
                System.out.println(pdfFilename);
                final PDDocument document = PDDocument.load(new File(inputFolder + pdfFilename));
                final PDFRenderer pdfRenderer = new PDFRenderer(document);

                for (int pageCounter = 0; pageCounter < document.getPages().getCount(); pageCounter++) {
                    // convert page to image
                    final BufferedImage pageImage = pdfRenderer.renderImage(pageCounter, 2, ImageType.GRAY);
                    final ByteArrayOutputStream outPutFromBodyContent = new ByteArrayOutputStream();
                    final BodyContentHandler handler = new BodyContentHandler(outPutFromBodyContent);

                    final TesseractOCRConfig tesserConfig = new TesseractOCRConfig();
                    final TesseractOCRParser parser = new TesseractOCRParser();
                    final ParseContext parsecontext = new ParseContext();
                    final Metadata metadata = new Metadata();

                    tesserConfig.setLanguage("eng");
                    tesserConfig.setEnableImageProcessing(1);
                    tesserConfig.setTessdataPath("./tessdata");
                    tesserConfig.setDepth(256);
                    tesserConfig.setDensity(800);
                    // TODO: NMO we have to pass the path as argument from properties
                    tesserConfig.setTesseractPath(tessPath);    //TODO je n'ai pas le tesseract!
                    parsecontext.set(TesseractOCRParser.class, parser);
                    parsecontext.set(TesseractOCRConfig.class, tesserConfig);

                    final TemporaryResources tempRessources = new TemporaryResources();
                    final File file = tempRessources.createTemporaryFile();
                    final FileOutputStream fos = new FileOutputStream(file);
                    ImageIO.write(pageImage, "PNG", fos);

                    final TikaInputStream tis = TikaInputStream.get(file, metadata);
                    parser.parse(tis, handler, metadata, parsecontext);
                    tempRessources.dispose();
                    // +++++ OUTPUT +++++++
                    String destContent = "./output/initial/" + pdfFilename.replace(".pdf", "");
                    if (!new File(destContent).exists()) {
                        Files.createDirectories(new File(destContent).toPath());
                    }
                    final File txt = new File(
                            destContent + "/" + pdfFilename.replace(".pdf", "") + "_Page" + (pageCounter) + ".txt");
                    final OutputStreamWriter outStream = new OutputStreamWriter(new FileOutputStream(txt),
                            Charset.forName("UTF-8"));
                    System.out.println(pdfFilename + ", Page " + pageCounter);
                    String Filename = pdfFilename.replace(".pdf", "");

                    // define the response depending on mode

                    List<ExtractContentBean> wordsInPage = NlpExtractOperation.applyNlp(
                            new String(outPutFromBodyContent.toByteArray(), Charset.defaultCharset()), Filename, excludeWords, posList,
                            extractLevel, mode);

                    /*
                     * if (mode.equals("word")) { Map<String,String> mapedObject =
                     * ((Map<String,String>)pageMap);
                     *
                     * mapedObject.keySet().stream().forEach(pageWordLine -> { RiString myRiString =
                     * new RiString(pageWordLine); String pos = myRiString.pos()[0]; String word =
                     * pageWordLine; String countPos = mapedObject.get(pageWordLine); int
                     * onlyPageCount =
                     * Integer.parseInt(mapedObject.get(pageWordLine).split("_")[0]); String
                     * onlyPagePos = String.valueOf(mapedObject.get(pageWordLine).split("_")[1]);
                     *
                     * if (!Objects.isNull(pdfMap.get(pageWordLine))) { int exisetingCounter =
                     * Integer.parseInt(pdfMap.get(pageWordLine).split("_")[0]); int newCounter =
                     * exisetingCounter + onlyPageCount; pdfMap.put(pageWordLine, newCounter + "_" +
                     * onlyPagePos);
                     *
                     * } else { pdfMap.put(pageWordLine, onlyPageCount + "_" + onlyPagePos); }
                     *
                     * }); } else { for (String sentence: ((String[]) pageMap)) { String context =
                     * pageMap.get(pageWordLine); pdfMap.put(pageWordLine,context);
                     * System.out.println(context); }
                     */

                    /*
                     * StreamSupport.stream((String[]) pageMap, true) .forEach(pageWordLine -> {
                     * String context = pageMap.get(pageWordLine); pdfMap.put(pageWordLine,context);
                     * System.out.println(context); }); }
                     */

                    // I create a csv file for this current pdf document

                    /*
                     * StringBuilder csvText = new StringBuilder(); FileOutputStream csvFile = new
                     * FileOutputStream( new File("./output/initial/" + pdfFilename + ".csv"));
                     *
                     * csvText.append("Word").append(";").append("Count").append(";").append("pos").
                     * append("\n"); pdfMap.keySet().stream().forEach(oneElement -> {
                     *
                     * RiString myRiString = new RiString(oneElement); int onlyPageCount =
                     * Integer.parseInt(pdfMap.get(oneElement).split("_")[0]); String onlyPagePos =
                     * String.valueOf(pdfMap.get(oneElement).split("_")[1]);
                     * csvText.append(oneElement).append(";").append(onlyPageCount).append(";").
                     * append(onlyPagePos) .append("\n"); });
                     * csvFile.write(csvText.toString().getBytes());
                     */

                    System.out.println("**** " + pageCounter + " ****");
                    // create an object
                    allWords.addAll(wordsInPage);

                    // displayReport(contentBo);

                    // TODO NMO: Use ES client to push the object into ES DB server

                }

                document.close();
            }
        } catch (NullPointerException ex) {

        }

        for (ExtractContentBean element : allWords) {
            boolean x = indexer.doIndex(element, serverURL);
            System.out.println(x);
        }
    }

    private static void displayReport(ExtractContentBean contentBo) {
        System.out.println("[ Pdf-Engine] ******* REPORT ******* \n");

        // System.out.println( String.format("[ Pdf-Engine] --- Pdf Name: %d",
        // contentBo.getFileName()) ) ;
        // System.out.println( String.format("[ Pdf-Engine] --- Content: %d",
        // contentBo.getContent()) ) ;
    }

    public static Properties fwkProperties() throws IOException {
        Properties prop = new Properties();
        prop.load(new ClassPathResource("configuration.properties").getInputStream());

        prop = validateConfig(prop);

        return prop;
    }

    private static Properties validateConfig(Properties ppties) {
        String deepLevel = (String) ppties.get("extract.deep.level");
        int extractLevel = (!Objects.isNull(deepLevel) && !Strings.isBlank(deepLevel)
                && !Strings.isEmpty(deepLevel)) ? Integer.parseInt(deepLevel) : 1;
        String inputFolder = (String) ppties.get("pdfs.inputs"); // args[0]; //"./input/";
        String tessPath = (String) ppties.get("tesseract.path");
        ppties.put("extract.deep.level", extractLevel);

        if ((Strings.isBlank(String.valueOf(inputFolder)) && Strings.isEmpty(String.valueOf(inputFolder)))
                || (Strings.isBlank(String.valueOf(tessPath)) && Strings.isEmpty(String.valueOf(tessPath)))) {

            showMessage();
        }
        return ppties;

    }

    private static void showMessage() {
        System.out.println("[ Pdf-Engine] missing input configuration");
        System.out.println("[ Pdf-Engine] --- Check tesseract path & input pdfs folder");
        System.exit(0);

    }

}
