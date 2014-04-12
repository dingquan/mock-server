package org.geekvsnerd.mockserver.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerMapping;

/**
 * NOTE: not thread safe, last push request wins!
 * @author quding
 *
 */
@Controller
public class MockServerController {
	private final static Logger logger = LoggerFactory.getLogger(MockServerController.class);

	//default response is used when no matching keyword responses found
	Map<String, Response> defaultResponses = new ConcurrentHashMap<String, Response>();
	Map<String, Map<Set<String>, Response>> keywordResponses = new ConcurrentHashMap<String, Map<Set<String>, Response>>();
	
	Queue<RequestResponse> reqResHistory = new CircularFifoQueue<RequestResponse>(25);

	/**
	 * Put the default response for a HTTP method
	 * @param responseBody
	 * @param method
	 * @param statusCode
	 * @param request
	 */
	@RequestMapping(value = {"/v1/response/{method}/{statusCode}"}, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.CREATED)
	public void putGenericResponse(@RequestBody String responseBody, 
									@PathVariable String method, 
									@PathVariable String statusCode,
									HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("PUT {}, body: {}", restOfTheUrl, responseBody);
		Response response = new Response(Integer.valueOf(statusCode), responseBody);
		defaultResponses.put(method.toUpperCase(), response);
	}

	/**
	 * get the default response for the specified http method
	 * @param method
	 * @param request
	 * @return
	 */
	@RequestMapping(value = {"/v1/response/{method}"}, method = RequestMethod.GET)
	public ResponseEntity<String> getGenericResponse(@PathVariable String method, HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("GET {}", restOfTheUrl);
		try{
			Response response = defaultResponses.get(method.toUpperCase());
			return new ResponseEntity<String>(response.responseBody, HttpStatus.valueOf(response.statusCode));
		}
		catch(NullPointerException e){
			return new ResponseEntity<String>("Response for " + method + " method " + "hasn't be set previously", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Put a response with specific keywords
	 * @param responseBody
	 * @param method
	 * @param statusCode
	 * @param keys comma separated keywords to be matched in the request
	 * @param request
	 */
	@RequestMapping(value = {"/v1/response/{method}/{statusCode}/{keys}"}, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.CREATED)
	public void putKeywordResponse(@RequestBody String responseBody, 
									@PathVariable String method, 
									@PathVariable String statusCode,
									@PathVariable String keys,
									HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("PUT {}, body: {}", restOfTheUrl, responseBody);
		
		Response response = new Response(Integer.valueOf(statusCode), responseBody);
		String keyStrs[] = keys.split(",");
		Set<String> keySet = new HashSet<String>(Arrays.asList(keyStrs));
		
		Map<Set<String>, Response> keyedResponses = keywordResponses.get(method.toUpperCase());
		if (keyedResponses == null){
			keyedResponses = new ConcurrentHashMap<Set<String>, Response>();
			keywordResponses.put(method.toUpperCase(), keyedResponses);
		}
		
		keyedResponses.put(keySet, response);
	}
	
	/**
	 * get the response for the method, keywords combination
	 * @param method
	 * @param request
	 * @return
	 */
	@RequestMapping(value = {"/v1/response/{method}/{keys}"}, method = RequestMethod.GET)
	public ResponseEntity<String> getKeywordResponse(@PathVariable String method,
													@PathVariable String keys,
													HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("GET {}", restOfTheUrl);
		
		String keyStrs[] = keys.split(",");
		Set<String> keySet = new HashSet<String>(Arrays.asList(keyStrs));
		
		Response response = null;
		Map<Set<String>, Response> keyedResponses = keywordResponses.get(method.toUpperCase());
		if (keyedResponses != null)
			response = keyedResponses.get(keySet);
		
		try{
			return new ResponseEntity<String>(response.responseBody, HttpStatus.valueOf(response.statusCode));
		}
		catch(NullPointerException e){
			return new ResponseEntity<String>("Response for " + method + " method " + "hasn't be set previously", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * The request handler for the real POST API calls
	 * @param requestBody
	 * @param request
	 * @return
	 */
	@RequestMapping(value = {"/v1/request"}, method = RequestMethod.POST)
	public ResponseEntity<String> handlePostRequest(@RequestBody String requestBody, HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("POST {}, requestBody: {} ", restOfTheUrl, requestBody);
		
		Response response = searchForResponse("POST", requestBody);
		return new ResponseEntity<String>(response.responseBody, HttpStatus.valueOf(response.statusCode));
	}

	@RequestMapping(value = {"/v1/request"}, method = RequestMethod.PUT)
	public ResponseEntity<String> handlePutRequest(@RequestBody String requestBody, HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("PUT {}, requestBody: {} ", restOfTheUrl, requestBody);
		
		Response response = searchForResponse("PUT", requestBody);
		return new ResponseEntity<String>(response.responseBody, HttpStatus.valueOf(response.statusCode));
	}
	
	@RequestMapping(value = {"/v1/request"}, method = RequestMethod.GET)
	public ResponseEntity<String> handleGetRequest(@RequestBody String requestBody, HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("PUT {}, requestBody: {} ", restOfTheUrl, requestBody);
		
		Response response = searchForResponse("GET", requestBody);
		return new ResponseEntity<String>(response.responseBody, HttpStatus.valueOf(response.statusCode));
	}
	
	@RequestMapping(value = {"/v1/request"}, method = RequestMethod.DELETE)
	public ResponseEntity<String> handleDeleteRequest(@RequestBody String requestBody, HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("DELETE {}, requestBody: {} ", restOfTheUrl, requestBody);
		
		Response response = searchForResponse("DELETE", requestBody);
		return new ResponseEntity<String>(response.responseBody, HttpStatus.valueOf(response.statusCode));
	}
	
	/**
	 * Get the last 20 request/response history
	 * @param requestBody
	 * @param request
	 * @return
	 */
	@RequestMapping(value = {"/v1/request/history"}, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody Queue<RequestResponse> getRequestResponseHistory(HttpServletRequest request) {
		String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		logger.info("GET {}", restOfTheUrl);
		
		return reqResHistory;
	}
	
	private Response searchForResponse(String method, String requestBody) {
		Map<Set<String>, Response> keyedResponses = keywordResponses.get(method.toUpperCase());
		Response resp = null;

		// first to see if there's any keyword responses match
		if (keyedResponses != null && keyedResponses.size() > 0){
			int previousBestMatch = 0;
			for (Set<String> keywords : keyedResponses.keySet()){
				boolean match = true;
				for (String key: keywords){
					if (!requestBody.contains(key)){
						match = false;
						continue;
					}
				}
				// we match as many keywords as possible and update the result with the best match so far
				if (match == true){
					if (keywords.size() > previousBestMatch){
						previousBestMatch = keywords.size();
						resp = keyedResponses.get(keywords);
					}
				}
			}
		}
		
		//if no matching keyward response, return the default response
		if (resp == null)
			resp = defaultResponses.get(method.toUpperCase());

		// if still no matching response, return 400
		if (resp == null)
			resp = new Response(400, "Response for " + method.toUpperCase() + " method hasn't be set previously");
		
		// update history
		RequestResponse reqRes = new RequestResponse(new Request(method.toUpperCase(), requestBody), resp);
		reqResHistory.add(reqRes);
		
		return resp;
	}


	class RequestResponse{
		Request request;
		Response response;
		
		public RequestResponse(Request request, Response response) {
			this.request = request;
			this.response = response;
		}

		public Request getRequest() {
			return request;
		}

		public void setRequest(Request request) {
			this.request = request;
		}

		public Response getResponse() {
			return response;
		}

		public void setResponse(Response response) {
			this.response = response;
		}
	}
	
	class Request{
		String method;
		String requestBody;

		public Request(String method, String requestBody) {
			this.method = method;
			this.requestBody = requestBody;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public String getRequestBody() {
			return requestBody;
		}

		public void setRequestBody(String requestBody) {
			this.requestBody = requestBody;
		}
	}
	
	class Response{
		int statusCode = 200;
		String responseBody = "No response has been set yet";
		
		Response(int statusCode, String responseBody){
			this.statusCode = statusCode;
			this.responseBody = responseBody;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		public String getResponseBody() {
			return responseBody;
		}

		public void setResponseBody(String responseBody) {
			this.responseBody = responseBody;
		}
	}
}