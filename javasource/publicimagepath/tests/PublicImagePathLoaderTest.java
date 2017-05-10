package publicimagepath.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixObjectRepository;
import publicimagepath.usecases.PublicImagePathLoader;

public class PublicImagePathLoaderTest {
	
	private MendixObjectRepository mendixObjectRepository;
	private MendixObjectEntity mendixObjectEntity;
	private ILogNode logger;
	private ImageServiceDefinition imageServiceDefitinion;
	List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
	private IDataType iDataType;
	private IMetaObject iMetaObject;
	private IMetaPrimitive iMetaPrimitiveId;
	private IMetaPrimitive iMetaPrimitiveName;

	@Before
	public void setUp() throws Exception {
		mendixObjectRepository = mock(MendixObjectRepository.class);
		mendixObjectEntity = mock(MendixObjectEntity.class);
		logger = mock(ILogNode.class);
		imageServiceDefinitions.add(imageServiceDefitinion);
		iDataType = mock(IDataType.class);
		iMetaObject = mock(IMetaObject.class);
		iMetaPrimitiveId = mock(IMetaPrimitive.class);
		iMetaPrimitiveName = mock(IMetaPrimitive.class);
	}

	@Test
	public void testLoad() throws CoreException {
		when(mendixObjectRepository.getImageServiceDefinitions()).thenReturn(imageServiceDefinitions);
		
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		List<ImageServiceDefinition> acutalImageServiceDefinitions = publicImagePathLoader.load();
		assertSame(imageServiceDefinitions, acutalImageServiceDefinitions);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testValidateTrue() throws CoreException {
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		String microflowName = "IVK_TestMicroflow";
		String parameterId = "Id";
		String parameterName = "Name";
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		String inputObjectType = "InputParameter";
		microflowParameters.put(inputObjectType, iDataType);
		@SuppressWarnings("rawtypes")
		Collection metaPrimitives = new ArrayList<>();
		metaPrimitives.add(iMetaPrimitiveId);
		metaPrimitives.add(iMetaPrimitiveName);
		
		when(mendixObjectEntity.getMicroflowName(imageServiceDefitinion)).thenReturn(microflowName);
		when(mendixObjectRepository.microflowExists(microflowName)).thenReturn(true);
		when(mendixObjectEntity.getPath(imageServiceDefitinion)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(iDataType.getObjectType()).thenReturn(inputObjectType);
		when(mendixObjectRepository.getMetaObject(inputObjectType)).thenReturn(iMetaObject);
		when(mendixObjectEntity.getMetaPrimitives(iMetaObject)).thenReturn(metaPrimitives);
		when(iMetaPrimitiveId.getName()).thenReturn(parameterId);
		when(iMetaPrimitiveName.getName()).thenReturn(parameterName);
		
		when(iDataType.isMendixObject()).thenReturn(true);
		assertTrue(publicImagePathLoader.validate(imageServiceDefinitions));
		verify(mendixObjectEntity, times(3)).getMicroflowName(imageServiceDefitinion);
		verify(mendixObjectRepository, times(1)).microflowExists(microflowName);
		verify(mendixObjectRepository, times(2)).getMicroflowInputParameters(microflowName);
		verify(mendixObjectEntity, times(2)).getPath(imageServiceDefitinion);
		verify(iDataType, times(1)).isMendixObject();
		verify(mendixObjectRepository, times(1)).getMetaObject(inputObjectType);
		verify(mendixObjectEntity, times(1)).getMetaPrimitives(iMetaObject);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testValidateFalseMicroflowNotExists() throws CoreException {
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		String microflowName = "IVK_TestMicroflow";
		String parameterId = "Id";
		String parameterName = "Name";
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		String inputObjectType = "InputParameter";
		microflowParameters.put(inputObjectType, iDataType);
		@SuppressWarnings("rawtypes")
		Collection metaPrimitives = new ArrayList<>();
		metaPrimitives.add(iMetaPrimitiveId);
		metaPrimitives.add(iMetaPrimitiveName);
		
		when(mendixObjectEntity.getMicroflowName(imageServiceDefitinion)).thenReturn(microflowName);
		when(mendixObjectRepository.microflowExists(microflowName)).thenReturn(false);
		when(mendixObjectEntity.getPath(imageServiceDefitinion)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(iDataType.getObjectType()).thenReturn(inputObjectType);
		when(mendixObjectRepository.getMetaObject(inputObjectType)).thenReturn(iMetaObject);
		when(mendixObjectEntity.getMetaPrimitives(iMetaObject)).thenReturn(metaPrimitives);
		when(iMetaPrimitiveId.getName()).thenReturn(parameterId);
		when(iMetaPrimitiveName.getName()).thenReturn(parameterName);
		
		when(iDataType.isMendixObject()).thenReturn(true);
		assertFalse(publicImagePathLoader.validate(imageServiceDefinitions));
		verify(logger, times(1)).error("Microflow " + microflowName + " does not exist for ImageServiceDefinition.");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testValidateFalsePathEmpty() throws CoreException {
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		String microflowName = "IVK_TestMicroflow";
		String parameterId = "Id";
		String parameterName = "Name";
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		String inputObjectType = "InputParameter";
		microflowParameters.put(inputObjectType, iDataType);
		@SuppressWarnings("rawtypes")
		Collection metaPrimitives = new ArrayList<>();
		metaPrimitives.add(iMetaPrimitiveId);
		metaPrimitives.add(iMetaPrimitiveName);
		
		when(mendixObjectEntity.getMicroflowName(imageServiceDefitinion)).thenReturn(microflowName);
		when(mendixObjectRepository.microflowExists(microflowName)).thenReturn(true);
		when(mendixObjectEntity.getPath(imageServiceDefitinion)).thenReturn("");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(iDataType.getObjectType()).thenReturn(inputObjectType);
		when(mendixObjectRepository.getMetaObject(inputObjectType)).thenReturn(iMetaObject);
		when(mendixObjectEntity.getMetaPrimitives(iMetaObject)).thenReturn(metaPrimitives);
		when(iMetaPrimitiveId.getName()).thenReturn(parameterId);
		when(iMetaPrimitiveName.getName()).thenReturn(parameterName);
		
		when(iDataType.isMendixObject()).thenReturn(true);
		assertFalse(publicImagePathLoader.validate(imageServiceDefinitions));
		verify(logger, times(1)).error("Path for ImageServiceDefinition is not specified.");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testValidateFalsePathNull() throws CoreException {
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		String microflowName = "IVK_TestMicroflow";
		String parameterId = "Id";
		String parameterName = "Name";
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		String inputObjectType = "InputParameter";
		microflowParameters.put(inputObjectType, iDataType);
		@SuppressWarnings("rawtypes")
		Collection metaPrimitives = new ArrayList<>();
		metaPrimitives.add(iMetaPrimitiveId);
		metaPrimitives.add(iMetaPrimitiveName);
		
		when(mendixObjectEntity.getMicroflowName(imageServiceDefitinion)).thenReturn(microflowName);
		when(mendixObjectRepository.microflowExists(microflowName)).thenReturn(true);
		when(mendixObjectEntity.getPath(imageServiceDefitinion)).thenReturn(null);
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(iDataType.getObjectType()).thenReturn(inputObjectType);
		when(mendixObjectRepository.getMetaObject(inputObjectType)).thenReturn(iMetaObject);
		when(mendixObjectEntity.getMetaPrimitives(iMetaObject)).thenReturn(metaPrimitives);
		when(iMetaPrimitiveId.getName()).thenReturn(parameterId);
		when(iMetaPrimitiveName.getName()).thenReturn(parameterName);
		
		when(iDataType.isMendixObject()).thenReturn(true);
		assertFalse(publicImagePathLoader.validate(imageServiceDefinitions));
		verify(logger, times(1)).error("Path for ImageServiceDefinition is not specified.");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testValidateFalseMoreThanOneMFInput() throws CoreException {
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		String microflowName = "IVK_TestMicroflow";
		String parameterId = "Id";
		String parameterName = "Name";
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		String inputObjectType = "InputParameter";
		microflowParameters.put(inputObjectType, iDataType);
		microflowParameters.put("AnotherInputParameter", iDataType);
		@SuppressWarnings("rawtypes")
		Collection metaPrimitives = new ArrayList<>();
		metaPrimitives.add(iMetaPrimitiveId);
		metaPrimitives.add(iMetaPrimitiveName);
		
		when(mendixObjectEntity.getMicroflowName(imageServiceDefitinion)).thenReturn(microflowName);
		when(mendixObjectRepository.microflowExists(microflowName)).thenReturn(true);
		when(mendixObjectEntity.getPath(imageServiceDefitinion)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(iDataType.getObjectType()).thenReturn(inputObjectType);
		when(mendixObjectRepository.getMetaObject(inputObjectType)).thenReturn(iMetaObject);
		when(mendixObjectEntity.getMetaPrimitives(iMetaObject)).thenReturn(metaPrimitives);
		when(iMetaPrimitiveId.getName()).thenReturn(parameterId);
		when(iMetaPrimitiveName.getName()).thenReturn(parameterName);
		
		when(iDataType.isMendixObject()).thenReturn(true);
		assertFalse(publicImagePathLoader.validate(imageServiceDefinitions));
		verify(logger, times(1)).error("Microflow " + microflowName + " has more than 1 input parameter.");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testValidateFalseNoIMendixObjectInput() throws CoreException {
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		String microflowName = "IVK_TestMicroflow";
		String parameterId = "Id";
		String parameterName = "Name";
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		String inputObjectType = "InputParameter";
		microflowParameters.put(inputObjectType, iDataType);
		@SuppressWarnings("rawtypes")
		Collection metaPrimitives = new ArrayList<>();
		metaPrimitives.add(iMetaPrimitiveId);
		metaPrimitives.add(iMetaPrimitiveName);
		
		when(mendixObjectEntity.getMicroflowName(imageServiceDefitinion)).thenReturn(microflowName);
		when(mendixObjectRepository.microflowExists(microflowName)).thenReturn(true);
		when(mendixObjectEntity.getPath(imageServiceDefitinion)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(iDataType.getObjectType()).thenReturn(inputObjectType);
		when(mendixObjectRepository.getMetaObject(inputObjectType)).thenReturn(iMetaObject);
		when(mendixObjectEntity.getMetaPrimitives(iMetaObject)).thenReturn(metaPrimitives);
		when(iMetaPrimitiveId.getName()).thenReturn(parameterId);
		when(iMetaPrimitiveName.getName()).thenReturn(parameterName);
		
		when(iDataType.isMendixObject()).thenReturn(false);
		assertFalse(publicImagePathLoader.validate(imageServiceDefinitions));
		verify(logger, times(1)).error("Input object of microflow " + microflowName + " is not an object.");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testValidateFalseParameterNotFound() throws CoreException {
		PublicImagePathLoader publicImagePathLoader = new PublicImagePathLoader(mendixObjectRepository, mendixObjectEntity, logger);
		String microflowName = "IVK_TestMicroflow";
		String parameterId = "Id";
		String parameterName = "Name";
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		String inputObjectType = "InputParameter";
		microflowParameters.put(inputObjectType, iDataType);
		@SuppressWarnings("rawtypes")
		Collection metaPrimitives = new ArrayList<>();
		metaPrimitives.add(iMetaPrimitiveId);
		metaPrimitives.add(iMetaPrimitiveName);
		
		when(mendixObjectEntity.getMicroflowName(imageServiceDefitinion)).thenReturn(microflowName);
		when(mendixObjectRepository.microflowExists(microflowName)).thenReturn(true);
		when(mendixObjectEntity.getPath(imageServiceDefitinion)).thenReturn("/testentities/{FalseParameter}/testimages/{" + parameterName + "}");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(iDataType.getObjectType()).thenReturn(inputObjectType);
		when(mendixObjectRepository.getMetaObject(inputObjectType)).thenReturn(iMetaObject);
		when(mendixObjectEntity.getMetaPrimitives(iMetaObject)).thenReturn(metaPrimitives);
		when(iMetaPrimitiveId.getName()).thenReturn(parameterId);
		when(iMetaPrimitiveName.getName()).thenReturn(parameterName);
		
		when(iDataType.isMendixObject()).thenReturn(true);
		assertFalse(publicImagePathLoader.validate(imageServiceDefinitions));
		verify(logger, times(1)).error("Path variable FalseParameter not defined in input object of microflow " + microflowName + ".");
	}
}
