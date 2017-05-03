package publicimagepath.repositories;

import java.io.InputStream;
import java.util.List;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

public class MendixImageRepository {
	
	private IContext context;
	
	public MendixImageRepository (IContext context) {
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
}
