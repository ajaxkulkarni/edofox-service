package com.rns.web.edo.service.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class EdoImageUtil {

	public static void main(String[] args) throws FileNotFoundException {
		File input = new File("F:\\Resoneuronance\\Edofox\\Document\\webcam.jpg");
		BufferedImage image;
		compressImage(new FileInputStream("F:\\Resoneuronance\\Edofox\\Document\\webcam.jpg"), "F:\\Resoneuronance\\Edofox\\Document\\compress.jpg", 0.1f);

		
	}

	public static File compressImage(InputStream input, String outputPath, Float compressionFactor) {
		BufferedImage image;
		OutputStream os = null;
		ImageOutputStream ios = null;
		ImageWriter writer = null;
		try {
			image = ImageIO.read(input);
			File compressedImageFile = new File(outputPath);
			os = new FileOutputStream(compressedImageFile);

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
			writer = (ImageWriter) writers.next();

			ios = ImageIO.createImageOutputStream(os);
			writer.setOutput(ios);

			ImageWriteParam param = writer.getDefaultWriteParam();

			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(compressionFactor);
			writer.write(null, new IIOImage(image, null, null), param);
			
			return compressedImageFile;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(ios != null) {
					ios.close();
				}
				if(os != null) {
					os.close();
				}
				if(writer != null) {
					writer.dispose();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}

}
