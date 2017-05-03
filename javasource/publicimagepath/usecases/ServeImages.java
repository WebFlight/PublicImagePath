package publicimagepath.usecases;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IActionManager;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive;

import publicimagepath.entities.ImageServiceDefinitionEntity;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixImageRepository;

public class ServeImages {

	private IMxRuntimeRequest request;
	private IMxRuntimeResponse response;
	private String s;
	private List<ImageServiceDefinition> imageServiceDefinitions;
	private ImageServiceDefinitionEntity imageServiceDefinitionEntity;
	private MendixImageRepository mendixImageRepository;
	private Pattern imagesPattern = Pattern.compile("^/images");
	private Pattern slashPattern = Pattern.compile("^/|/$"); 
	private Pattern urlToRe = Pattern.compile("\\{[a-zA-Z0-9_\\.-]*\\}");
	private Map<String, String> parameterMap = new HashMap<>();
	
	public ServeImages(IMxRuntimeRequest request, IMxRuntimeResponse response, String s,
			List<ImageServiceDefinition> imageServiceDefinitions, ImageServiceDefinitionEntity imageServiceDefinitionEntity, MendixImageRepository mendixImageRepository) {
		this.request = request;
		this.response = response;
		this.s = s;
		this.imageServiceDefinitions = imageServiceDefinitions;
		this.imageServiceDefinitionEntity = imageServiceDefinitionEntity;
		this.mendixImageRepository = mendixImageRepository;
	}
	
	public void serve() throws CoreException, IOException {
		
		String requestPath = request.getHttpServletRequest().getRequestURI();
		requestPath = imagesPattern.matcher(requestPath).replaceAll("");
		requestPath = slashPattern.matcher(requestPath).replaceAll("");
		
		// Match request with definitions
		for (ImageServiceDefinition imageServiceDefinition : imageServiceDefinitions) {
			String microflowName = imageServiceDefinitionEntity.getMicroflowName(imageServiceDefinition);
			String path = imageServiceDefinitionEntity.getPath(imageServiceDefinition);
			path = slashPattern.matcher(path).replaceAll("");
			
			String imageDefinitionRegex = urlToRe.matcher(path).replaceAll("(\\[a-zA-Z0-9_\\.-\\]*)");
			String paramRegex = urlToRe.matcher(path).replaceAll("(\\\\{[a-zA-Z0-9_\\.-]*\\\\})");
			Pattern pattern = Pattern.compile(imageDefinitionRegex);
			Pattern paramPattern = Pattern.compile(paramRegex);
			if (pattern.matcher(requestPath).matches()) {
				// Extract the key
				// Retrieve the source entity using the key
				Matcher parMatcher  = paramPattern.matcher(path);
				Matcher valueMatcher = pattern.matcher(requestPath);
		        while (parMatcher.find() && valueMatcher.find()) {
		            for (int i = 1; i <= parMatcher.groupCount(); i++) {
		                String parName = parMatcher.group(i).replace("{", "").replace("}", "");
		                String value = valueMatcher.group(i);
		                parameterMap.put(parName, value);
		            }
		        }
			}
			
			if(Core.getMicroflowNames().contains(microflowName)) {
				Map<String, IDataType> mfInputParameters = Core.getInputParameters(microflowName);
				Iterator<IDataType> it = mfInputParameters.values().iterator();
				while(it.hasNext()) {
					IDataType dataType = it.next();
					if(dataType.isMendixObject()) {
						Collection<? extends IMetaPrimitive> primitives = Core.getMetaObject(dataType.getObjectType()).getMetaPrimitives();
						IMendixObject inputObject = mendixImageRepository.instantiate(dataType.getObjectType());
			
						for (IMetaPrimitive primitive : primitives) {
							imageServiceDefinitionEntity.setValue(inputObject, primitive.getName(), parameterMap.get(primitive.getName()));
							System.out.println("Member " + primitive.getName() + " Value: " + parameterMap.get(primitive.getName()));
						}
						
						IMendixObject imageObject = mendixImageRepository.execute(microflowName, inputObject);
						InputStream imageInputStream = mendixImageRepository.getImage(imageObject);
						OutputStream outputStream = response.getOutputStream();
						IOUtils.copy(imageInputStream, outputStream);
						imageInputStream.close();
						outputStream.close();
					}
				}
			}
		}
	}
}
