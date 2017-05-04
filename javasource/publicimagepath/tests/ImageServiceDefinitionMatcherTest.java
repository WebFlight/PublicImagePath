package publicimagepath.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.helpers.ImageServiceDefinitionMatcher;
import publicimagepath.proxies.ImageServiceDefinition;

public class ImageServiceDefinitionMatcherTest {

	private MendixObjectEntity mendixObjectEntity;
	private ImageServiceDefinition imageServiceDefinition;
	
	
	@Before
	public void setUp() throws Exception {
		mendixObjectEntity = mock(MendixObjectEntity.class);
		imageServiceDefinition = mock(ImageServiceDefinition.class);
	}

	@Test
	public void testFindTrue() {
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{Id}/testimages/{Name}");
		
		String requestPath = "testentities/1/testimages/link_3.jpg";
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		imageServiceDefinitions.add(imageServiceDefinition);
		ImageServiceDefinitionMatcher imageServiceDefinitionMatcher = new ImageServiceDefinitionMatcher(mendixObjectEntity);
		ImageServiceDefinition returnedImageServiceDefinition = null;
		try {
			returnedImageServiceDefinition = imageServiceDefinitionMatcher.find(imageServiceDefinitions, requestPath);
		} catch (Exception e) {
			
		}
		assertSame(imageServiceDefinition, returnedImageServiceDefinition);
		verify(mendixObjectEntity, times(1)).getPath(imageServiceDefinition);
	}
	
	@Test(expected = Exception.class)
	public void testFindFalse() throws Exception {
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{Id}/testimages/{Name}");
		
		String requestPath = "testentities/1/";
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		imageServiceDefinitions.add(imageServiceDefinition);
		ImageServiceDefinitionMatcher imageServiceDefinitionMatcher = new ImageServiceDefinitionMatcher(mendixObjectEntity);
		imageServiceDefinitionMatcher.find(imageServiceDefinitions, requestPath);
		verify(mendixObjectEntity, times(1)).getPath(imageServiceDefinition);
	}

}
