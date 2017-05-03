package publicimagepath.usecases;

import java.util.List;

import com.mendix.core.CoreException;

import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixObjectRepository;

public class PublicImagePathLoader {

	private MendixObjectRepository mendixObjectRepository;	
	
	public PublicImagePathLoader(MendixObjectRepository mendixObjectRepository) {
		this.mendixObjectRepository = mendixObjectRepository;
	}

	public List<ImageServiceDefinition> load() throws CoreException {
		return mendixObjectRepository.getImageServiceDefinitions();
	}
}
