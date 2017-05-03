package publicimagepath.usecases;

import java.util.List;

import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;

import publicimagepath.entities.ImageServiceDefinitionEntity;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixImageRepository;

public class PublicImagePathHandler extends RequestHandler{
	
	private List<ImageServiceDefinition> imageServiceDefinitions;
	private ImageServiceDefinitionEntity imageServiceDefinitionEntity;
	private MendixImageRepository mendixImageRepository;
	
	public PublicImagePathHandler(List<ImageServiceDefinition> imageServiceDefinitions, ImageServiceDefinitionEntity imageServiceDefinitionEntity, MendixImageRepository mendixImageRepository){
		this.imageServiceDefinitions = imageServiceDefinitions;
		this.imageServiceDefinitionEntity = imageServiceDefinitionEntity;
		this.mendixImageRepository = mendixImageRepository;
	}

	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path) throws Exception {
		ServeImages serveImages = new ServeImages(request, response, path, imageServiceDefinitions, imageServiceDefinitionEntity, mendixImageRepository);
		serveImages.serve();
	}

}