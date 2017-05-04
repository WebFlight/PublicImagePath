package publicimagepath.usecases;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.IOUtils;

import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.MendixException;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;

import publicimagepath.entities.MendixObjectEntity;
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
	private Pattern imagesPattern = Pattern.compile("^/images");
	private Pattern slashPattern = Pattern.compile("^/|/$"); 
	
	public ServeImages(IMxRuntimeRequest request, IMxRuntimeResponse response, String s,
			List<ImageServiceDefinition> imageServiceDefinitions, MendixObjectEntity imageServiceDefinitionEntity, MendixObjectRepository mendixObjectRepository,
			ImageServiceDefinitionMatcher imageServiceDefinitionMatcher, ImageServiceDefinitionParser imageServiceDefinitionParser) {
		this.request = request;
		this.response = response;
		this.imageServiceDefinitions = imageServiceDefinitions;
		this.mendixObjectEntity = imageServiceDefinitionEntity;
		this.mendixObjectRepository = mendixObjectRepository;
		this.imageServiceDefinitionMatcher = imageServiceDefinitionMatcher;
		this.imageServiceDefinitionParser = imageServiceDefinitionParser;
	}
	
	public void serve() throws IOException, MendixException {
		
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

        
        if(mendixObjectRepository.microflowExists(microflowName)) {
			Map<String, IDataType> mfInputParameters = mendixObjectRepository.getMicroflowInputParameters(microflowName);
			Iterator<IDataType> it = mfInputParameters.values().iterator();
			while(it.hasNext()) {
				IDataType dataType = it.next();
				if(dataType.isMendixObject()) {
					IMendixObject inputObject = mendixObjectRepository.instantiate(dataType.getObjectType());
					Map<String, ? extends IMendixObjectMember<?>> inputObjectMembers = mendixObjectEntity.getMembers(inputObject);
					for(String key : inputObjectMembers.keySet()) {
						IMendixObjectMember<?> member = inputObjectMembers.get(key);
						mendixObjectEntity.setValue(inputObject, member.getName(), parameterMap.get(member.getName()));
					}
					
					IMendixObject imageObject = mendixObjectRepository.execute(microflowName, inputObject);
					if(imageObject == null) {
						response.getHttpServletResponse().setStatus(404);
						response.getOutputStream().write(new String("404 NOT FOUND: Image not found.").getBytes());
						response.getOutputStream().close();
						return;
					}
					
					InputStream imageInputStream = mendixObjectRepository.getImage(imageObject);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					IOUtils.copy(imageInputStream, baos);
					byte[] imageBytes = baos.toByteArray();
			        ByteArrayInputStream anotherBais = new ByteArrayInputStream(imageBytes);
			        setHeaders(imageObject, imageBytes, response);
			        OutputStream outputStream = response.getOutputStream();
			        IOUtils.copy(anotherBais, outputStream);
					anotherBais.close();
					outputStream.close();
				}
			}
		}	
	}
	
	private void setHeaders(IMendixObject imageObject, byte[] imageBytes, IMxRuntimeResponse response) throws MendixException, IOException {
		
		ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
		
		ImageInputStream iis = ImageIO.createImageInputStream(bais);
		Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
		ImageReader reader = (ImageReader) imageReaders.next();
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
        response.getHttpServletResponse().setHeader("ETag", UUID.nameUUIDFromBytes(imageBytes).toString());
		
	}
}
