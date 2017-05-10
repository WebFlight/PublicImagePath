package publicimagepath.tests;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.mendix.core.objectmanagement.member.MendixInteger;
import com.mendix.core.objectmanagement.member.MendixString;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.MendixException;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;

import publicimagepath.entities.MendixObjectEntity;
import publicimagepath.helpers.IOUtilsWrapper;
import publicimagepath.helpers.ImageServiceDefinitionMatcher;
import publicimagepath.helpers.ImageServiceDefinitionParser;
import publicimagepath.proxies.ImageServiceDefinition;
import publicimagepath.repositories.MendixObjectRepository;
import publicimagepath.usecases.ServeImages;

public class ServeImagesTest {
	private IMxRuntimeRequest request;
	private IMxRuntimeResponse response;
	private ImageServiceDefinition imageServiceDefinition;
	private MendixObjectEntity mendixObjectEntity;
	private MendixObjectRepository mendixObjectRepository;
	private ImageServiceDefinitionMatcher imageServiceDefinitionMatcher;
	private ImageServiceDefinitionParser imageServiceDefinitionParser;
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	private OutputStream outputStream;
	private IMendixObject imageObject;
	private IOUtilsWrapper iOUtilsWrapper;
	private ImageReader imageReader;
	private ImageInputStream imageInputStream;
	private Iterator<ImageReader> imageReaders;
	private ByteArrayOutputStream byteArrayOutputStream;
	private ByteArrayInputStream byteArrayInputStream;
	private byte[] imageBytes;
	private IDataType iDataType;
	private IMendixObject inputObject;
	private IMendixObjectMember<?> inputMemberId;
	private IMendixObjectMember<?> inputMemberName;
	

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		request = mock(IMxRuntimeRequest.class);
		response = mock(IMxRuntimeResponse.class);
		imageServiceDefinition = mock(ImageServiceDefinition.class);
		mendixObjectEntity = mock(MendixObjectEntity.class);
		mendixObjectRepository = mock(MendixObjectRepository.class);
		httpServletRequest = mock(HttpServletRequest.class);
		httpServletResponse = mock(HttpServletResponse.class);
		outputStream = mock(OutputStream.class);
		imageServiceDefinitionMatcher = mock(ImageServiceDefinitionMatcher.class);
		imageServiceDefinitionParser = mock(ImageServiceDefinitionParser.class);
		imageObject = mock(IMendixObject.class);
		iOUtilsWrapper = mock(IOUtilsWrapper.class);
		imageReader = mock(ImageReader.class);
		imageInputStream = mock(ImageInputStream.class);
		imageReaders = mock(Iterator.class);
		byteArrayOutputStream = mock(ByteArrayOutputStream.class);
		byteArrayInputStream = mock(ByteArrayInputStream.class);
		iDataType = mock(IDataType.class); 
		inputObject = mock(IMendixObject.class);
		inputMemberId = mock(MendixInteger.class);
		inputMemberName = mock(MendixString.class);
	}

	@Test
	public void testServeNoParameter () throws Exception {
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		imageServiceDefinitions.add(imageServiceDefinition);
		String microflowName = "IVK_TestMicroflow";
		String parameterIdValue = "1";
		String parameterNameValue = "link_3.jpg";
		String requestPath = "testentities/" + parameterIdValue + "/testimages/" + parameterNameValue;
		String parameterId = "Id";
		String parameterName = "Name";	
		
		when(request.getHttpServletRequest()).thenReturn(httpServletRequest);
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		when(httpServletRequest.getRequestURI()).thenReturn(requestPath);
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(response.getOutputStream()).thenReturn(outputStream);
		when(imageServiceDefinitionMatcher.find(imageServiceDefinitions, requestPath)).thenReturn(imageServiceDefinition);
		when(mendixObjectEntity.getMicroflowName(imageServiceDefinition)).thenReturn(microflowName);
		when(mendixObjectRepository.execute(microflowName, null)).thenReturn(imageObject);
		when(byteArrayOutputStream.toByteArray()).thenReturn(imageBytes);

		when(iOUtilsWrapper.createImageInputStream(byteArrayInputStream)).thenReturn(imageInputStream);
		when(iOUtilsWrapper.createByteArrayInputStream(any(byte[].class))).thenReturn(byteArrayInputStream);
		when(iOUtilsWrapper.getImageReaders(imageInputStream)).thenReturn(imageReaders);
		when(imageReaders.next()).thenReturn(imageReader);
		
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		when(imageReader.getFormatName()).thenReturn("JPEG");
		when(mendixObjectEntity.getChangedDate(imageObject)).thenReturn(new Date());
		
		ServeImages serveImages = new ServeImages (request, response, imageServiceDefinitions, mendixObjectEntity, mendixObjectRepository,
				imageServiceDefinitionMatcher, imageServiceDefinitionParser, iOUtilsWrapper);
		serveImages.serve();
		
		Date changedDate = new Date();
		when(mendixObjectEntity.getChangedDate(imageObject)).thenReturn(changedDate);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
		Date dateTimeNextYear = calendar.getTime();
		
		verify(mendixObjectRepository, times(1)).execute(microflowName, null);
		verify(iOUtilsWrapper, times(1)).copy(byteArrayInputStream, outputStream);
		verify(outputStream, times(1)).close();
		verify(httpServletResponse, times(6)).setHeader(any(String.class), any(String.class));
		verify(httpServletResponse, times(1)).setHeader("Cache-control", "public, max-age=31536000");
		verify(httpServletResponse, times(1)).setHeader("Content-type", "image/jpeg");
		verify(httpServletResponse, times(1)).setHeader("Expires", dateFormat.format(dateTimeNextYear));
		verify(httpServletResponse, times(1)).setHeader("Last-modified", dateFormat.format(changedDate));
		verify(httpServletResponse, times(1)).setHeader("Content-length", "0");
		verify(httpServletResponse, times(1)).setHeader(eq("ETag"), any(String.class));
	}
	
	@Test
	public void testServeWithParameter () throws Exception {
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		imageServiceDefinitions.add(imageServiceDefinition);
		String microflowName = "IVK_TestMicroflow";
		String parameterIdValue = "1";
		String parameterNameValue = "link_3.jpg";
		String requestPath = "testentities/" + parameterIdValue + "/testimages/" + parameterNameValue;
		String parameterId = "Id";
		String parameterName = "Name";	
		
		HashMap<String, String> parameterMap = new HashMap<>();
		parameterMap.put(parameterId, parameterIdValue);
		parameterMap.put(parameterName, parameterNameValue);
		
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		microflowParameters.put("InputParameters", iDataType);
		
		Map<String, IMendixObjectMember<?>> inputObjectMembers = new HashMap<>();
		inputObjectMembers.put(parameterId, inputMemberId);
		inputObjectMembers.put(parameterName, inputMemberName);
		
		when(iDataType.getObjectType()).thenReturn("InputParameters");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(imageServiceDefinitionParser.getParameters(imageServiceDefinition, requestPath)).thenReturn(parameterMap);
		when(request.getHttpServletRequest()).thenReturn(httpServletRequest);
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		when(httpServletRequest.getRequestURI()).thenReturn(requestPath);
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(response.getOutputStream()).thenReturn(outputStream);
		when(imageServiceDefinitionMatcher.find(imageServiceDefinitions, requestPath)).thenReturn(imageServiceDefinition);
		when(mendixObjectEntity.getMicroflowName(imageServiceDefinition)).thenReturn(microflowName);
		when(mendixObjectRepository.execute(microflowName, inputObject)).thenReturn(imageObject);
		when(mendixObjectRepository.instantiate("InputParameters")).thenReturn(inputObject);
		when(mendixObjectEntity.getMembers(inputObject)).thenReturn(inputObjectMembers);
		when(byteArrayOutputStream.toByteArray()).thenReturn(imageBytes);
		when(inputMemberId.getName()).thenReturn(parameterId);
		when(inputMemberName.getName()).thenReturn(parameterName);

		when(iOUtilsWrapper.createImageInputStream(byteArrayInputStream)).thenReturn(imageInputStream);
		when(iOUtilsWrapper.createByteArrayInputStream(any(byte[].class))).thenReturn(byteArrayInputStream);
		when(iOUtilsWrapper.getImageReaders(imageInputStream)).thenReturn(imageReaders);
		when(imageReaders.next()).thenReturn(imageReader);
		
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		when(imageReader.getFormatName()).thenReturn("JPEG");
	
		Date changedDate = new Date();
		when(mendixObjectEntity.getChangedDate(imageObject)).thenReturn(changedDate);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
		Date dateTimeNextYear = calendar.getTime();
		
		
		ServeImages serveImages = new ServeImages (request, response, imageServiceDefinitions, mendixObjectEntity, mendixObjectRepository,
				imageServiceDefinitionMatcher, imageServiceDefinitionParser, iOUtilsWrapper);
		serveImages.serve();
		
		verify(mendixObjectRepository, times(1)).execute(microflowName, inputObject);
		verify(mendixObjectEntity, times(1)).setValue(inputObject, parameterId, parameterIdValue);
		verify(mendixObjectEntity, times(1)).setValue(inputObject, parameterName, parameterNameValue);
		verify(iOUtilsWrapper, times(1)).copy(byteArrayInputStream, outputStream);
		verify(outputStream, times(1)).close();
		verify(httpServletResponse, times(6)).setHeader(any(String.class), any(String.class));
		verify(httpServletResponse, times(1)).setHeader("Cache-control", "public, max-age=31536000");
		verify(httpServletResponse, times(1)).setHeader("Content-type", "image/jpeg");
		verify(httpServletResponse, times(1)).setHeader("Expires", dateFormat.format(dateTimeNextYear));
		verify(httpServletResponse, times(1)).setHeader("Last-modified", dateFormat.format(changedDate));
		verify(httpServletResponse, times(1)).setHeader("Content-length", "0");
		verify(httpServletResponse, times(1)).setHeader(eq("ETag"), any(String.class));
	}
	
	@Test
	public void testServeWithParameterNotSet () throws Exception {
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		imageServiceDefinitions.add(imageServiceDefinition);
		String microflowName = "IVK_TestMicroflow";
		String parameterIdValue = "1";
		String requestPath = "testentities/" + parameterIdValue;
		String parameterId = "Id";
		String parameterName = "Name";	
		
		HashMap<String, String> parameterMap = new HashMap<>();
		parameterMap.put(parameterId, parameterIdValue);
		
		HashMap<String, IDataType> microflowParameters = new HashMap<>();
		microflowParameters.put("InputParameters", iDataType);
		
		Map<String, IMendixObjectMember<?>> inputObjectMembers = new HashMap<>();
		inputObjectMembers.put(parameterId, inputMemberId);
		inputObjectMembers.put(parameterName, inputMemberName);
		
		when(iDataType.getObjectType()).thenReturn("InputParameters");
		when(mendixObjectRepository.getMicroflowInputParameters(microflowName)).thenReturn(microflowParameters);
		when(imageServiceDefinitionParser.getParameters(imageServiceDefinition, requestPath)).thenReturn(parameterMap);
		when(request.getHttpServletRequest()).thenReturn(httpServletRequest);
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		when(httpServletRequest.getRequestURI()).thenReturn(requestPath);
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{" + parameterId);
		when(response.getOutputStream()).thenReturn(outputStream);
		when(imageServiceDefinitionMatcher.find(imageServiceDefinitions, requestPath)).thenReturn(imageServiceDefinition);
		when(mendixObjectEntity.getMicroflowName(imageServiceDefinition)).thenReturn(microflowName);
		when(mendixObjectRepository.execute(microflowName, inputObject)).thenReturn(imageObject);
		when(mendixObjectRepository.instantiate("InputParameters")).thenReturn(inputObject);
		when(mendixObjectEntity.getMembers(inputObject)).thenReturn(inputObjectMembers);
		when(byteArrayOutputStream.toByteArray()).thenReturn(imageBytes);
		when(inputMemberId.getName()).thenReturn(parameterId);
		when(inputMemberName.getName()).thenReturn(parameterName);

		when(iOUtilsWrapper.createImageInputStream(byteArrayInputStream)).thenReturn(imageInputStream);
		when(iOUtilsWrapper.createByteArrayInputStream(any(byte[].class))).thenReturn(byteArrayInputStream);
		when(iOUtilsWrapper.getImageReaders(imageInputStream)).thenReturn(imageReaders);
		when(imageReaders.next()).thenReturn(imageReader);
		
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		when(imageReader.getFormatName()).thenReturn("JPEG");
	
		Date changedDate = new Date();
		when(mendixObjectEntity.getChangedDate(imageObject)).thenReturn(changedDate);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
		Date dateTimeNextYear = calendar.getTime();
		
		
		ServeImages serveImages = new ServeImages (request, response, imageServiceDefinitions, mendixObjectEntity, mendixObjectRepository,
				imageServiceDefinitionMatcher, imageServiceDefinitionParser, iOUtilsWrapper);
		serveImages.serve();
		
		verify(mendixObjectRepository, times(1)).execute(microflowName, inputObject);
		verify(mendixObjectEntity, times(1)).setValue(inputObject, parameterId, parameterIdValue);
		verify(iOUtilsWrapper, times(1)).copy(byteArrayInputStream, outputStream);
		verify(outputStream, times(1)).close();
		verify(httpServletResponse, times(6)).setHeader(any(String.class), any(String.class));
		verify(httpServletResponse, times(1)).setHeader("Cache-control", "public, max-age=31536000");
		verify(httpServletResponse, times(1)).setHeader("Content-type", "image/jpeg");
		verify(httpServletResponse, times(1)).setHeader("Expires", dateFormat.format(dateTimeNextYear));
		verify(httpServletResponse, times(1)).setHeader("Last-modified", dateFormat.format(changedDate));
		verify(httpServletResponse, times(1)).setHeader("Content-length", "0");
		verify(httpServletResponse, times(1)).setHeader(eq("ETag"), any(String.class));
	}	
	
	@Test
	public void testServeNoImageFound () throws IOException, MendixException, NoSuchAlgorithmException {
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		when(request.getHttpServletRequest()).thenReturn(httpServletRequest);
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		String parameterIdValue = "1";
		String parameterNameValue = "link_3.jpg";
		String requestPath = "testentities/" + parameterIdValue + "/testimages/" + parameterNameValue;
		when(httpServletRequest.getRequestURI()).thenReturn(requestPath);
		String parameterId = "Id";
		String parameterName = "Name";
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(response.getOutputStream()).thenReturn(outputStream);
		imageServiceDefinitions.add(imageServiceDefinition);
		
		ServeImages serveImages = new ServeImages (request, response, imageServiceDefinitions, mendixObjectEntity, mendixObjectRepository,
				imageServiceDefinitionMatcher, imageServiceDefinitionParser, iOUtilsWrapper);
		serveImages.serve();
		
		verify(httpServletResponse, times(1)).setStatus(404);
		verify(outputStream, times(1)).write(new String("404 NOT FOUND: Image not found.").getBytes());
		verify(outputStream, times(1)).close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testServeNoImageServiceDefinitionMatch () throws Exception {
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		when(request.getHttpServletRequest()).thenReturn(httpServletRequest);
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		String parameterIdValue = "1";
		String parameterNameValue = "link_3.jpg";
		String requestPath = "testentities/" + parameterIdValue + "/testimages/" + parameterNameValue;
		when(httpServletRequest.getRequestURI()).thenReturn(requestPath);
		String parameterId = "Id";
		String parameterName = "Name";
		when(mendixObjectEntity.getPath(imageServiceDefinition)).thenReturn("/testentities/{" + parameterId + "}/testimages/{" + parameterName + "}");
		when(response.getOutputStream()).thenReturn(outputStream);
		imageServiceDefinitions.add(imageServiceDefinition);
		when(imageServiceDefinitionMatcher.find(imageServiceDefinitions, requestPath)).thenThrow(Exception.class);
		
		ServeImages serveImages = new ServeImages (request, response, imageServiceDefinitions, mendixObjectEntity, mendixObjectRepository,
				imageServiceDefinitionMatcher, imageServiceDefinitionParser, iOUtilsWrapper);
		serveImages.serve();
		
		verify(httpServletResponse, times(1)).setStatus(404);
		verify(outputStream, times(1)).write(new String("404 NOT FOUND: URL does not match with ImageServiceDefinition.").getBytes());
		verify(outputStream, times(1)).close();
	}
}
