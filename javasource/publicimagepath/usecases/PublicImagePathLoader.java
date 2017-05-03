package publicimagepath.usecases;

import java.util.List;

import com.mendix.core.CoreException;

import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.ImageServiceDefinitionRepository;

public class PublicImagePathLoader {

	private ImageServiceDefinitionRepository imageServiceDefinitionRepository;	
	
	public PublicImagePathLoader(ImageServiceDefinitionRepository imageServiceDefinitionRepository) {
		this.imageServiceDefinitionRepository = imageServiceDefinitionRepository;
	}

	public List<ImageServiceDefinition> load() throws CoreException {
		return imageServiceDefinitionRepository.getImageServiceDefinitions();
	}
}
