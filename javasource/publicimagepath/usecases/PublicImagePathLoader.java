package publicimagepath.usecases;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixObjectRepository;

public class PublicImagePathLoader {

	private MendixObjectRepository mendixObjectRepository;
	private MendixObjectEntity mendixObjectEntity;
	private ILogNode logger;
	private Pattern parametersInPath = Pattern.compile("\\{[a-zA-Z0-9_\\.-]*\\}");
	
	public PublicImagePathLoader(MendixObjectRepository mendixObjectRepository, MendixObjectEntity mendixObjectEntity, ILogNode logger) {
		this.mendixObjectRepository = mendixObjectRepository;
		this.mendixObjectEntity = mendixObjectEntity;
		this.logger = logger;
	}

	public List<ImageServiceDefinition> load() throws CoreException {
		return mendixObjectRepository.getImageServiceDefinitions();
	}
	
	public boolean validate(List<ImageServiceDefinition> imageServiceDefinitions) {
		boolean valid = true;
		for(ImageServiceDefinition imageServiceDefinition : imageServiceDefinitions) {
			valid = 
					valid &&
					checkMicroflowExists(imageServiceDefinition) &&
					checkPathExists(imageServiceDefinition) &&
					checkMicroflowInputs(imageServiceDefinition) &&
					checkPathVariables(imageServiceDefinition);
		}
		return valid;
	}
	
	private boolean checkMicroflowExists (ImageServiceDefinition imageServiceDefinition) {
		String microflowName = mendixObjectEntity.getMicroflowName(imageServiceDefinition);
		if (mendixObjectRepository.microflowExists(microflowName) == false) {
			logger.error("Microflow " + microflowName + " does not exist for ImageServiceDefinition.");
			return false;
		}
		return true;
	}
	
	private boolean checkPathExists (ImageServiceDefinition imageServiceDefinition) {
		String path = mendixObjectEntity.getPath(imageServiceDefinition);
		if (path == null | path.equals("")) {
			logger.error("Path for ImageServiceDefinition is not specified.");
			return false;
		}
		return true;
	}
	
	private boolean checkMicroflowInputs (ImageServiceDefinition imageServiceDefinition) {
		String microflowName = mendixObjectEntity.getMicroflowName(imageServiceDefinition);
		Map<String, IDataType> microflowInputParameters = mendixObjectRepository.getMicroflowInputParameters(microflowName);
		
		if (microflowInputParameters.size() > 1) {
			logger.error("Microflow " + microflowName + " has more than 1 input parameter.");
			return false;
		}
		
		Iterator<IDataType> it = microflowInputParameters.values().iterator();
		while(it.hasNext()) {
			IDataType microflowInputParameter = it.next();
			if (microflowInputParameter.isMendixObject() == false) {
				logger.error("Input object of microflow " + microflowName + " is not an object.");
				return false;
			}
		}
		return true;
	}
	
	private boolean checkPathVariables (ImageServiceDefinition imageServiceDefinition) {
		String microflowName = mendixObjectEntity.getMicroflowName(imageServiceDefinition);
		Map<String, IDataType> microflowInputParameters = mendixObjectRepository.getMicroflowInputParameters(microflowName);
		Iterator<IDataType> it = microflowInputParameters.values().iterator();
		Set<String> microflowInputMembers = new HashSet<>();
		
		while(it.hasNext()) {
			IDataType dataType = it.next();
			if(dataType.isMendixObject()) {
				IMetaObject metaObject = mendixObjectRepository.getMetaObject(dataType.getObjectType());
				Collection<? extends IMetaPrimitive> primitives = mendixObjectEntity.getMetaPrimitives(metaObject);
				for (IMetaPrimitive primitive : primitives) {
					microflowInputMembers.add(primitive.getName());
				}
			}
		}
		
		String path = mendixObjectEntity.getPath(imageServiceDefinition);
		String paramRegex = parametersInPath.matcher(path).replaceAll("(\\\\{[a-zA-Z0-9_\\.-]*\\\\})");
		Pattern paramPattern = Pattern.compile(paramRegex);
		Matcher parMatcher  = paramPattern.matcher(path);
		parMatcher.find();
		boolean valid = true;
		for (int i = 1; i <= parMatcher.groupCount(); i++) {
            String parName = parMatcher.group(i).replace("{", "").replace("}", "");
            boolean parNameFound = microflowInputMembers.contains(parName);
            if (parNameFound == false) {
            	logger.error("Path variable " + parName + " not defined in input object of microflow " + microflowName + ".");
            }
            valid = valid && parNameFound;
        }
		return valid;
	}
}