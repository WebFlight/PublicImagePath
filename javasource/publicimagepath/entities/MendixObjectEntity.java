package publicimagepath.entities;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.mendix.core.CoreException;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive;

import publicimagepath.proxies.ImageServiceDefinition;

public class MendixObjectEntity {
	
	private IContext context;
	
	public MendixObjectEntity(IContext context) {
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
	
	public Map<String, ? extends IMendixObjectMember<?>> getMembers(IMendixObject object) {
		return object.getMembers(context);
	}
	
	@SuppressWarnings("unchecked")
	public <T> void setValue(IMendixObjectMember<T> member, String value) {
		member.setValue(context, (T) value);
	}
	
	public Date getChangedDate(IMendixObject object) throws CoreException {
		return object.getChangedDate(context);
	}
	
	public Collection<? extends IMetaPrimitive> getMetaPrimitives (IMetaObject metaObject) {
		return metaObject.getMetaPrimitives();
	}
}
