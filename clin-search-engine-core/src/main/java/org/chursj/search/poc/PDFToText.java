package org.chursj.search.poc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.util.FileSystemUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;

@Log4j(topic = "non")
@Log(topic = "none")
public class PDFToText {

	public class ConfigProperties {
		@Getter
		@Setter
		private String path;
	}

	// TODO NMO: tesseract path will be given by parameter
	// inputs & outputs will be give by parameters
	@SuppressWarnings({ "deprecation", "resource" })
	public static void main(String[] args) throws Exception {
		String inputFolder = "./input/"; // args[0]; //"./input/";
		String outputFolder = "./output/initial"; // args[1]; // "./output/gen";
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		System.setProperty("TESSDATA_PREFIX", "./tessdata");
		System.setProperty("LC_ALL", "C");
		System.setProperty("OMP_NUM_THREADS", "1");

		if (new File(outputFolder).exists() == true) {
			FileSystemUtils.deleteRecursively(new File(outputFolder));
		}
		for (final File fileEntry : new File(inputFolder).listFiles()) {
			try {
				final String pdfFilename = fileEntry.getName();
				final PDDocument document = PDDocument.load(new File(inputFolder + pdfFilename));
				final PDFRenderer pdfRenderer = new PDFRenderer(document);
				int pageCounter = 0;
				for (PDPage page : document.getPages()) {
					// convert page to image
					final BufferedImage pageImage = pdfRenderer.renderImage(pageCounter, 2, ImageType.GRAY);
					final TesseractOCRConfig tesserConfig = new TesseractOCRConfig();
					final TesseractOCRParser parser = new TesseractOCRParser();
					final ParseContext parsecontext = new ParseContext();
					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					final BodyContentHandler handler = new BodyContentHandler(out);
					final Metadata metadata = new Metadata();
					final TemporaryResources tmp = new TemporaryResources();

					tesserConfig.setLanguage("eng");
					tesserConfig.setEnableImageProcessing(1);
					tesserConfig.setTessdataPath("./tessdata");
					tesserConfig.setDepth(256);
					tesserConfig.setDensity(800);
					// TODO: NMO we have to pass the path as argument from properties
					tesserConfig.setTesseractPath("/usr/local/Cellar/tesseract/4.0.0_1/bin/");

					parsecontext.set(TesseractOCRParser.class, parser);
					parsecontext.set(TesseractOCRConfig.class, tesserConfig);

					final File file = tmp.createTemporaryFile();
					final FileOutputStream fos = new FileOutputStream(file);
					ImageIO.write(pageImage, "PNG", fos);
					// Thread.sleep(4000);
					final TikaInputStream tis = TikaInputStream.get(file, metadata);
					parser.parse(tis, handler, metadata, parsecontext);
					tmp.dispose();
					// +++++ OUTPUT +++++++
					String destContent = "./output/initial/" + pdfFilename.replace(".pdf", "");
					if (new File(destContent).exists() == false) {
						Files.createDirectories(new File(destContent).toPath());
					}
					final File txt = new File(
							destContent + "/" + pdfFilename.replace(".pdf", "") + "_Page" + (pageCounter + 1) + ".txt");
					final OutputStreamWriter outStream = new OutputStreamWriter(new FileOutputStream(txt),
							Charset.forName("UTF-8"));
					byte[] foarmettedContent = TextNlpCommand.applyNlp(new String(out.toByteArray(), Charset.defaultCharset()));
					
					// - put the result on xxx_formated.txt
					   
					// - put the initial one on his separate file 
					
					
					
					outStream.write(new String(out.toByteArray(), Charset.defaultCharset()));
					outStream.close();
					pageCounter = pageCounter + 1;
				}
				document.close();
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
}