package publicimagepath.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.helpers.ImageServiceDefinitionParser;
import publicimagepath.proxies.ImageServiceDefinition;

public class ImageServiceDefinitionParserTest {
	
	private MendixObjectEntity mendixObjectEntity;
	private ImageServiceDefinition imageServiceDefinition;

	@Before
	public void setUp() throws Exception {
		mendixObjectEntity = mock(MendixObjectEntity.class);
		imageServiceDefinition = mock(ImageServiceDefinition.class);
	}

	@Test
	public void testGetParameters() {
		String parameterId = "Id";
		String parameterName = "Name";
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		String parameterIdValue = "1";
		String parameterNameValue = "link_3.jpg";
		String requestPath = "testentities/" + parameterIdValue + "/testimages/" + parameterNameValue;
		ImageServiceDefinitionParser imageDefinitionParser = new ImageServiceDefinitionParser(mendixObjectEntity);
		Map<String, String> parameterMap = imageDefinitionParser.getParameters(imageServiceDefinition, requestPath);
		assertTrue(parameterIdValue.equals(parameterMap.get(parameterId)));
		assertTrue(parameterNameValue.equals(parameterMap.get(parameterName)));
		assertTrue(parameterMap.size() == 2);
		verify(mendixObjectEntity, times(1)).getPath(imageServiceDefinition);
	}

	@Test
	public void testGetParametersEmpty() {
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/testimages/");
		String requestPath = "testentities/testimages/";
		ImageServiceDefinitionParser imageDefinitionParser = new ImageServiceDefinitionParser(mendixObjectEntity);
		Map<String, String> parameterMap = imageDefinitionParser.getParameters(imageServiceDefinition, requestPath);
		assertTrue(parameterMap.isEmpty());
		verify(mendixObjectEntity, times(1)).getPath(imageServiceDefinition);
	}
}
