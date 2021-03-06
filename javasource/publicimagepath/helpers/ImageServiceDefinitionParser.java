package publicimagepath.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.proxies.ImageServiceDefinition;

public class ImageServiceDefinitionParser {
	
	private MendixObjectEntity mendixObjectEntity;
	private Pattern leadingAndTrailingSlashes = Pattern.compile("^/|/$");
	// Characters allowed in URI according to RFC 3986 Appendix A
	private Pattern parametersInPath = Pattern.compile("\\{[^/?#]*\\}");
	private Map<String, String> parameterMap = new HashMap<>();
	
	public ImageServiceDefinitionParser(MendixObjectEntity mendixObjectEntity) {
		this.mendixObjectEntity = mendixObjectEntity;
	}

	
	public Map<String, String> getParameters(ImageServiceDefinition imageServiceDefinition, String requestPath) {
		String path = mendixObjectEntity.getPath(imageServiceDefinition);
		path = leadingAndTrailingSlashes.matcher(path).replaceAll("");
		
		String imageDefinitionRegex = parametersInPath.matcher(path).replaceAll("(\\[^/?#]*)");
		String paramRegex = parametersInPath.matcher(path).replaceAll("(\\\\{[^/?#]*\\\\})");
		Pattern pattern = Pattern.compile(imageDefinitionRegex);
		Pattern paramPattern = Pattern.compile(paramRegex);
		Matcher parMatcher  = paramPattern.matcher(path);
		Matcher valueMatcher = pattern.matcher(requestPath);
		parMatcher.find(); 
		valueMatcher.find();
        
		for (int i = 1; i <= parMatcher.groupCount(); i++) {
            String parName = parMatcher.group(i).replace("{", "").replace("}", "");
            String value = valueMatcher.group(i);
            parameterMap.put(parName, value);
        }
        
		return parameterMap;
	}
}
