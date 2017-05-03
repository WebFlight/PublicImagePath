package publicimagepath.usecases;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.mendix.core.CoreException;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
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
	
	public void serve() throws IOException, CoreException {
		
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
					OutputStream outputStream = response.getOutputStream();
					IOUtils.copy(imageInputStream, outputStream);
					imageInputStream.close();
					outputStream.close();
				}
			}
		}	
	}
}
