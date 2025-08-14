package ca.canada.digital.search.assessment.resource;

import ca.canada.digital.search.assessment.api.CreateDepartmentRequest;
import ca.canada.digital.search.assessment.model.Department;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.service.DepartmentService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
public class DepartmentResource {
    private static final int MAX_LIMIT = 100;
    private final DepartmentService departmentService;

    public DepartmentResource(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * POST /departments
     */
    @POST
    @UnitOfWork
    public Response createDepartment(@Auth UserEntity admin,
                                     @Valid CreateDepartmentRequest req) {
        Department dept = departmentService.createDepartment(
                admin,
                req.getNameEn(),
                req.getNameFr(),
                req.getAcronymEn(),
                req.getAcronymFr(),
                req.getSearchUrlEn(),
                req.getSearchUrlFr()
        );
        return Response.created(
                UriBuilder.fromResource(DepartmentResource.class)
                        .path(dept.getId().toString())
                        .build()
        ).entity(dept).build();
    }


    @GET
    @UnitOfWork
    public Response listDepartments(@QueryParam("limit") @DefaultValue("" + MAX_LIMIT) int limit) {
        int safeLimit = Math.min(limit, MAX_LIMIT);
        List<Department> depts = departmentService.listDepartments(0, safeLimit);
        return Response.ok(depts).build();
    }

    /**
     * DELETE /departments/{id}
     */
    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public Response deleteDepartment(@Auth UserEntity admin,
                                     @PathParam("id") Integer id) {
        departmentService.deleteDepartment(admin, id);
        return Response.noContent().build();
    }
}
