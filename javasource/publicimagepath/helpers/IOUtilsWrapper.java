package publicimagepath.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.IOUtils;

public class IOUtilsWrapper {
	
	public void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		IOUtils.copy(inputStream, outputStream);
	}
	
	public Iterator<ImageReader> getImageReaders(ImageInputStream imageInputStream) {
		return ImageIO.getImageReaders(imageInputStream);
	}
	
	public ImageInputStream createImageInputStream(ByteArrayInputStream bais) throws IOException {
		return ImageIO.createImageInputStream(bais);
	}
	
	public ByteArrayInputStream createByteArrayInputStream(byte[] imageBytes) {
		return new ByteArrayInputStream(imageBytes);
	}
}
