package ca.canada.digital.search.assessment.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ResponseUtil {

	private ResponseUtil() {

	}

	public static Response successResponse(Map<String, Object> results) {
		results.put("status", "success");
		results.put("statusCode", HttpServletResponse.SC_OK);
		return Response.ok().type(MediaType.APPLICATION_JSON).entity(results).build();
	}

	public static Response errorResponse(String errorMessage) {
		Map<String, Object> map = new HashMap<>();
		map.put("status", "fail");
		map.put("statusCode", HttpServletResponse.SC_BAD_REQUEST);
		map.put("message", errorMessage);
		return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(map).build();
	}

}
