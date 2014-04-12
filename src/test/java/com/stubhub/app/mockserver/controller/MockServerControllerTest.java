package org.geekvsnerd.mockserver.controller;

import junit.framework.Assert;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MockServerControllerTest {

	MockServerController controller = new MockServerController();
	private static final String AEG_RESP = "this is an AEG response";
	private static final String TDC_RESP = "this is a TDC response";
	private static final String TMI_RESP = "this is a TMI response";
	private static final String TM_RESP = "this is a tm response";
	private static final String GENERIC_RESP = "this is a generic response";
	/*
	@BeforeMethod
	public void setup()
	{
		controller.putAegResponse(AEG_RESP);
		controller.putTdcResponse(TDC_RESP);
		controller.putTmiResponse(TMI_RESP);
		controller.putTmResponse(TM_RESP);
		controller.putGenericResponse(GENERIC_RESP);
	}
	
	@Test
	public void getAegResponse() {
		Assert.assertEquals(AEG_RESP, controller.getAegResponse());
	}

	@Test
	public void getGenericResponse() {
		Assert.assertEquals(GENERIC_RESP, controller.getGenericResponse());
	}

	@Test
	public void getTdcResponse() {
		Assert.assertEquals(TDC_RESP, controller.getTdcResponse());
	}

	@Test
	public void getTmResponse() {
		Assert.assertEquals(TM_RESP, controller.getTmResponse());
	}

	@Test
	public void getTmiResponse() {
		Assert.assertEquals(TMI_RESP, controller.getTmiResponse());
	}

	@Test
	public void handleAegRequest() {
		Assert.assertEquals(AEG_RESP, controller.handleAegRequest("foobar"));
	}

	@Test
	public void handleGenericRequest() {
		Assert.assertEquals(GENERIC_RESP, controller.handleGenericRequest("foobar"));
	}

	@Test
	public void handleTdcRequest() {
		Assert.assertEquals(TDC_RESP, controller.handleTdcRequest("foobar"));
	}

	@Test
	public void handleTmRequest() {
		Assert.assertEquals(TM_RESP, controller.handleTmRequest("foobar"));
	}

	@Test
	public void handleTmiRequest() {
		Assert.assertEquals(TMI_RESP, controller.handleTmiRequest("foobar"));
	}
*/
}
