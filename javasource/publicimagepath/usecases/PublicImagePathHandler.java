package publicimagepath.usecases;

import java.util.List;

import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.helpers.IOUtilsWrapper;
import publicimagepath.helpers.ImageServiceDefinitionMatcher;
import publicimagepath.helpers.ImageServiceDefinitionParser;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixObjectRepository;

public class PublicImagePathHandler extends RequestHandler{
	
	private List<ImageServiceDefinition> imageServiceDefinitions;
	private MendixObjectEntity mendixObjectEntity;
	private MendixObjectRepository mendixObjectRepository;
	private ImageServiceDefinitionMatcher imageServiceDefinitionMatcher;
	private ImageServiceDefinitionParser imageServiceDefinitionParser;
	private IOUtilsWrapper iOUtilsWrapper;
	
	public PublicImagePathHandler(List<ImageServiceDefinition> imageServiceDefinitions, MendixObjectEntity mendixObjectEntity, MendixObjectRepository mendixObjectRepository,
			ImageServiceDefinitionMatcher imageServiceDefinitionMatcher, ImageServiceDefinitionParser imageServiceDefinitionParser, IOUtilsWrapper iOUtilsWrapper){
		this.imageServiceDefinitions = imageServiceDefinitions;
		this.mendixObjectEntity = mendixObjectEntity;
		this.mendixObjectRepository = mendixObjectRepository;
		this.imageServiceDefinitionMatcher = imageServiceDefinitionMatcher;
		this.imageServiceDefinitionParser = imageServiceDefinitionParser;
		this.iOUtilsWrapper = iOUtilsWrapper;
	}

	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path) throws Exception {
		ServeImages serveImages = new ServeImages(
				request, 
				response,  
				imageServiceDefinitions, 
				mendixObjectEntity, 
				mendixObjectRepository,
				imageServiceDefinitionMatcher, 
				imageServiceDefinitionParser,
				iOUtilsWrapper
				);
		serveImages.serve();
	}

}