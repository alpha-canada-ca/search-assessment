package ca.canada.digital.search.assessment.resource;

import ca.canada.digital.search.assessment.api.LoginRequest;
import ca.canada.digital.search.assessment.api.LoginResponse;
import ca.canada.digital.search.assessment.service.AuthService;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private final AuthService authService;

    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Logs in a user by verifying credentials and issuing a session token.
     *
     * @param req         the login payload
     * @param httpRequest the servlet request (to extract IP/user-agent)
     * @return LoginResponse containing the bearer token
     */
    @POST
    @UnitOfWork
    @Path("/login")
    public Response login(@Valid LoginRequest req,
                          @Context HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String ua = httpRequest.getHeader("User-Agent");

        String token = authService.login(
                req.getEmail(),
                req.getPassword(),
                ip,
                ua
        );

        return Response.ok(new LoginResponse(token)).build();
    }

    @DELETE
    @UnitOfWork
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length()).trim();
            authService.logout(token);
        }
        // Always return 204, even if token was missing or already invalidated
        return Response.noContent().build();
    }
}
