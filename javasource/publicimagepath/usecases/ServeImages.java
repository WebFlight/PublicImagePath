package publicimagepath.usecases;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.MendixException;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.helpers.IOUtilsWrapper;
import publicimagepath.helpers.ImageServiceDefinitionMatcher;
import publicimagepath.helpers.ImageServiceDefinitionParser;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixObjectRepository;

public class ServeImages {

	private IMxRuntimeRequest request;
	private IMxRuntimeResponse response;
	private List<ImageServiceDefinition> imageServiceDefinitions;
	private MendixObjectEntity mendixObjectEntity;
	private MendixObjectRepository mendixObjectRepository;
	private ImageServiceDefinitionMatcher imageServiceDefinitionMatcher;
	private ImageServiceDefinitionParser imageServiceDefinitionParser;
	private IOUtilsWrapper iOUtilsWrapper;
	private Pattern imagesPattern = Pattern.compile("^/images");
	private Pattern slashPattern = Pattern.compile("^/|/$");

	public ServeImages(IMxRuntimeRequest request, IMxRuntimeResponse response,
			List<ImageServiceDefinition> imageServiceDefinitions, MendixObjectEntity mendixObjectEntity, MendixObjectRepository mendixObjectRepository,
			ImageServiceDefinitionMatcher imageServiceDefinitionMatcher, ImageServiceDefinitionParser imageServiceDefinitionParser, IOUtilsWrapper iOUtilsWrapper) {
		this.request = request;
		this.response = response;
		this.imageServiceDefinitions = imageServiceDefinitions;
		this.mendixObjectEntity = mendixObjectEntity;
		this.mendixObjectRepository = mendixObjectRepository;
		this.imageServiceDefinitionMatcher = imageServiceDefinitionMatcher;
		this.imageServiceDefinitionParser = imageServiceDefinitionParser;
		this.iOUtilsWrapper = iOUtilsWrapper;
	}

	public void serve() throws IOException, MendixException, NoSuchAlgorithmException {

		String requestPath = request.getHttpServletRequest().getRequestURI();
		requestPath = imagesPattern.matcher(requestPath).replaceAll("");
		requestPath = slashPattern.matcher(requestPath).replaceAll("");

		ImageServiceDefinition imageServiceDefinition;
		Map<String, String> parameterMap = new HashMap<>();
		String microflowName = new String();
		try {
			imageServiceDefinition = imageServiceDefinitionMatcher.find(imageServiceDefinitions, requestPath);
			parameterMap = imageServiceDefinitionParser.getParameters(imageServiceDefinition, requestPath);
			microflowName = mendixObjectEntity.getMicroflowName(imageServiceDefinition);
		} catch (Exception e) {
			response.getHttpServletResponse().setStatus(404);
			response.getOutputStream().write(new String("404 NOT FOUND: URL does not match with ImageServiceDefinition.").getBytes());
			response.getOutputStream().close();
			return;
		}

		Map<String, IDataType> mfInputParameters = mendixObjectRepository.getMicroflowInputParameters(microflowName);
		IMendixObject imageObject = null;
		
		if (mfInputParameters.size() > 0) {
			Iterator<IDataType> it = mfInputParameters.values().iterator();
			
			while(it.hasNext()) {
				IDataType dataType = it.next();
				IMendixObject inputObject = mendixObjectRepository.instantiate(dataType.getObjectType());
				Map<String, ? extends IMendixObjectMember<?>> inputObjectMembers = mendixObjectEntity.getMembers(inputObject);
				for(String key : inputObjectMembers.keySet()) {
					IMendixObjectMember<?> member = inputObjectMembers.get(key);
					mendixObjectEntity.setValue(inputObject, member.getName(), parameterMap.get(member.getName()));
				}

				imageObject = mendixObjectRepository.execute(microflowName, inputObject);
			}
		}
		
		if (mfInputParameters.size() == 0) {
			imageObject = mendixObjectRepository.execute(microflowName, null);
		}
		
		
		if(imageObject == null) {
			response.getHttpServletResponse().setStatus(404);
			response.getOutputStream().write(new String("404 NOT FOUND: Image not found.").getBytes());
			response.getOutputStream().close();
			return;
		}
		
		InputStream imageInputStream = mendixObjectRepository.getImage(imageObject);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		iOUtilsWrapper.copy(imageInputStream, byteArrayOutputStream);
		byte[] imageBytes = byteArrayOutputStream.toByteArray();
		InputStream duplicatedImageInputStream = iOUtilsWrapper.createByteArrayInputStream(imageBytes);
		setHeaders(imageObject, imageBytes, response);
		OutputStream outputStream = response.getOutputStream();
		iOUtilsWrapper.copy(duplicatedImageInputStream, outputStream);
		duplicatedImageInputStream.close();
		outputStream.close();
	}	


	private void setHeaders(IMendixObject imageObject, byte[] imageBytes, IMxRuntimeResponse response) throws MendixException, IOException, NoSuchAlgorithmException{

		ByteArrayInputStream bais = iOUtilsWrapper.createByteArrayInputStream(imageBytes);

		ImageInputStream iis = iOUtilsWrapper.createImageInputStream(bais);

		Iterator<ImageReader> imageReaders = iOUtilsWrapper.getImageReaders(iis);
		ImageReader reader = imageReaders.next();
		String imageFormat = reader.getFormatName();

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
		Date dateTimeNextYear = calendar.getTime();
		Date changedDate = mendixObjectEntity.getChangedDate(imageObject);

		response.getHttpServletResponse().setHeader("Cache-control", "public, max-age=31536000");
		response.getHttpServletResponse().setHeader("Content-type", "image/" + imageFormat.toLowerCase());
		response.getHttpServletResponse().setHeader("Expires",  dateFormat.format(dateTimeNextYear));
		response.getHttpServletResponse().setHeader("Last-modified", dateFormat.format(changedDate));
		response.getHttpServletResponse().setHeader("Content-length", "" + imageBytes.length);
		response.getHttpServletResponse().setHeader("ETag", getEtag(imageBytes));

		iis.close();
	}

	private String getEtag(byte[] imageBytes) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.reset();
		md.update(imageBytes, 0, imageBytes.length);
		BigInteger bigInt = new BigInteger(1,md.digest());
		return bigInt.toString(16);
	}
}
