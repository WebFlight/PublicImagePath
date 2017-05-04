package publicimagepath.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.MendixException;

import publicimagepath.entities.MendixObjectEntity;
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
	}

	@Test
	public void testServe() throws IOException, MendixException {
		List<ImageServiceDefinition> imageServiceDefinitions = new ArrayList<>();
		when(request.getHttpServletRequest()).thenReturn(httpServletRequest);
		when(response.getHttpServletResponse()).thenReturn(httpServletResponse);
		String parameterIdValue = "1";
		String parameterNameValue = "link_3.jpg";
		String requestPath = "testentities/" + parameterIdValue + "/testimages/" + parameterNameValue;
		when(httpServletRequest.getRequestURI()).thenReturn(requestPath);
		when(response.getOutputStream()).thenReturn(outputStream);
		imageServiceDefinitions.add(imageServiceDefinition);
		
		ServeImages serveImages = new ServeImages (request, response, imageServiceDefinitions, mendixObjectEntity, mendixObjectRepository,
				imageServiceDefinitionMatcher, imageServiceDefinitionParser);
		serveImages.serve();
	}

}
