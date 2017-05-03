package publicimagepath.entities;

//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.util.List;

//import com.mendix.core.Core;
//import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
//import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import publicimagepath.proxies.ImageServiceDefinition;

public class ImageServiceDefinitionEntity {
	
	private IContext context;
	
	public ImageServiceDefinitionEntity(IContext context) {
		this.context = context;
	}
	
	public String getPath(ImageServiceDefinition imageServiceDefinition) {
		return imageServiceDefinition.getPath(context);
	}
	
	public String getMicroflowName(ImageServiceDefinition imageServiceDefinition) {
		return imageServiceDefinition.getMicroflowName(context);
	}
	
	public void setValue(IMendixObject object, String member, String value) {
		object.setValue(context, member, value);
	}
	
//	public InputStream getImage() throws CoreException {
//		List<IMendixObject> images = Core.retrieveXPathQuery(context, "//PublicImagePath.ImageServiceDefinition");
//		InputStream inputStream = new ByteArrayInputStream(new byte[0]);
//		for (IMendixObject image : images) {
//			inputStream = Core.getImage(context, image, false);			
//		}
//		return inputStream;
//	}

}
