package ca.canada.digital.search.assessment.resource;

import ca.canada.digital.search.assessment.api.CreateUserRequest;
import ca.canada.digital.search.assessment.api.ResetPayload;
import ca.canada.digital.search.assessment.dao.PasswordResetTokenDao;
import ca.canada.digital.search.assessment.dao.UserEntityDao;
import ca.canada.digital.search.assessment.model.PasswordResetToken;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.service.UserService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Tag(name = "User Services")
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    private static final int MAX_LIMIT = 100;
    private final UserEntityDao userDao;
    private final PasswordResetTokenDao tokenDao;
    private final UserService userService;

    public UserResource(UserEntityDao userDao, PasswordResetTokenDao tokenDao, UserService userService) {
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.userService = userService;
    }

    @GET
    @UnitOfWork
    @Path("/me")
    public UserEntity getProfile(@Auth UserEntity user) {
        return userService.getUser(user.getId());
    }

    @GET
    @UnitOfWork
    public Response listUsers(@Auth UserEntity requester) {
        List<UserEntity> users = userService.listUsers(requester, 0, MAX_LIMIT);
        return Response.ok(users).build();
    }

    @POST
    @UnitOfWork
    public UserEntity create(@Auth UserEntity admin,
                             @Valid CreateUserRequest req) {
        return userService.createUser(
                admin,
                req.getEmail(),
                req.getPassword(),
                req.getFirstName(),
                req.getLastName(),
                req.getDepartmentId(),
                req.getAdmin()
        );
    }

    @DELETE
    @UnitOfWork
    @Path("/{id}")
    public Response deleteUser(@Auth UserEntity admin,
                               @PathParam("id") Integer userId) {
        userService.deleteUser(admin, userId);
        return Response.noContent().build();
    }

    // TODO Configure emailing reset token to users
    @POST
    @UnitOfWork
    @Path("/reset-password")
    public Response reset(@Valid ResetPayload p) {
        PasswordResetToken t = tokenDao.findValid(p.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));
        UserEntity u = t.getUser();
        u.setPassword(p.getNewPassword());
        userDao.save(u);
        tokenDao.markConsumed(t);
        return Response.ok().build();
    }

}
