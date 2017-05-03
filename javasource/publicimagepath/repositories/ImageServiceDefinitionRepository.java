package publicimagepath.repositories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import publicimagepath.proxies.ImageServiceDefinition;

public class ImageServiceDefinitionRepository {
	
	private IContext context;
	
	public ImageServiceDefinitionRepository(IContext context) {
		this.context = context;
	}
	
	public List<ImageServiceDefinition> getImageServiceDefinitions() throws CoreException {
		List<IMendixObject> imageServiceDefinitionsRaw = Core.retrieveXPathQuery(context, "//PublicImagePath.ImageServiceDefinition");
		Iterator<IMendixObject> it = imageServiceDefinitionsRaw.iterator();
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		
		while (it.hasNext()) {
			imageServiceDefinitions.add(ImageServiceDefinition.initialize(context, it.next()));
		}
		
		return imageServiceDefinitions;
	}
	
	

}
