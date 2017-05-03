package publicimagepath.helpers;

import java.util.List;
import java.util.regex.Pattern;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.proxies.ImageServiceDefinition;

public class ImageServiceDefinitionMatcher {
	
	private MendixObjectEntity mendixObjectEntity;
	private Pattern leadingAndTrailingSlashes = Pattern.compile("^/|/$"); 
	private Pattern parametersInPath = Pattern.compile("\\{[a-zA-Z0-9_\\.-]*\\}");
	
	public ImageServiceDefinitionMatcher(MendixObjectEntity mendixObjectEntity) {
		this.mendixObjectEntity = mendixObjectEntity;
	}
	
	public ImageServiceDefinition find(List<ImageServiceDefinition> imageServiceDefinitions, String requestPath) throws Exception {
		
		for (ImageServiceDefinition imageServiceDefinition : imageServiceDefinitions) {
			String path = mendixObjectEntity.getPath(imageServiceDefinition);
			path = leadingAndTrailingSlashes.matcher(path).replaceAll("");
			
			String imageDefinitionRegex = parametersInPath.matcher(path).replaceAll("(\\[a-zA-Z0-9_\\.-\\]*)");
			Pattern pattern = Pattern.compile(imageDefinitionRegex);
			if (pattern.matcher(requestPath).matches()) {
				return imageServiceDefinition;
			}
		}
			
		throw new Exception("Url did not match any ImageServiceDefinition paths."); 
	}

}
