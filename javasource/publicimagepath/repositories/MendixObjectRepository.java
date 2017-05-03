package publicimagepath.repositories;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import publicimagepath.proxies.ImageServiceDefinition;

public class MendixObjectRepository {
	
	private IContext context;
	
	public MendixObjectRepository (IContext context) {
		this.context = context;
	}
	
	public InputStream getImage(IMendixObject object) {
		return Core.getImage(context, object, false);
	}
	
	public List<IMendixObject> getObjects(String xPath, String[] params) throws CoreException {
		return Core.retrieveXPathQueryEscaped(context, xPath, params);
	}
	
	public IMendixObject instantiate(String objectType) {
		return Core.instantiate(context, objectType);
	}
	
	public <R> R execute(String mfName, Object params) throws CoreException {
		return Core.execute(context, mfName, params);
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
	
	public boolean microflowExists(String microflowName) {
		return Core.getMicroflowNames().contains(microflowName);
	}
	
	public Map<String, IDataType> getMicroflowInputParameters(String microflowName) {
		return Core.getInputParameters(microflowName);
	}
}
