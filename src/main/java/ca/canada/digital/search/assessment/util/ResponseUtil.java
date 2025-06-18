package ca.canada.digital.search.assessment.util;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

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
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(map).build();
    }

}
