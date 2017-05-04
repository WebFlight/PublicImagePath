package publicimagepath.usecases;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IDataType;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixObjectRepository;

public class PublicImagePathLoader {

	private MendixObjectRepository mendixObjectRepository;
	private MendixObjectEntity mendixObjectEntity;
	private ILogNode logger;
	
	public PublicImagePathLoader(MendixObjectRepository mendixObjectRepository, MendixObjectEntity mendixObjectEntity, ILogNode logger) {
		this.mendixObjectRepository = mendixObjectRepository;
		this.mendixObjectEntity = mendixObjectEntity;
		this.logger = logger;
	}

	public List<ImageServiceDefinition> load() throws CoreException {
		return mendixObjectRepository.getImageServiceDefinitions();
	}
	
	public boolean validate(List<ImageServiceDefinition> imageServiceDefinitions) {
		boolean validation = true;
		for(ImageServiceDefinition imageServiceDefinition : imageServiceDefinitions) {
			validation = 
					validation &&
					checkMicroflowExists(imageServiceDefinition) &&
					checkPathExists(imageServiceDefinition) &&
					checkMicroflowInputs(imageServiceDefinition);
		}
		return validation;
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
}