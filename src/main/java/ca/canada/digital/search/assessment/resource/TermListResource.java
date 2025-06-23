package ca.canada.digital.search.assessment.resource;

import ca.canada.digital.search.assessment.api.CreateTermListRequest;
import ca.canada.digital.search.assessment.api.CreateTermRequest;
import ca.canada.digital.search.assessment.model.Term;
import ca.canada.digital.search.assessment.model.TermList;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.service.TermListService;
import io.dropwizard.auth.Auth;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/lists")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TermListResource {
    private final TermListService service;

    public TermListResource(TermListService service) {
        this.service = service;
    }

    @POST
    public Response createList(@Auth UserEntity user,
                               @Valid CreateTermListRequest req) {
        TermList list = service.createTermList(
                user,
                req.getName(),
                req.getLanguageId()
        );
        return Response.created(
                UriBuilder.fromResource(TermListResource.class)
                        .path(list.getId().toString())
                        .build()
        ).entity(list).build();
    }

    @POST
    @Path("/{listId}/terms")
    public Response addTerm(@Auth UserEntity user,
                            @PathParam("listId") Integer listId,
                            @Valid CreateTermRequest req) {
        Term term = service.addTerm(
                user,
                listId,
                req.getTerm(),
                req.getIndex(),
                req.getTargetUrls()
        );
        return Response.created(
                UriBuilder.fromResource(TermListResource.class)
                        .path(listId.toString())
                        .path("terms")
                        .path(term.getId().toString())
                        .build()
        ).entity(term).build();
    }

    @POST
    @Path("/{listId}/terms/bulk")
    public Response addTermsBulk(@Auth UserEntity user,
                                 @PathParam("listId") Integer listId,
                                 @Valid List<CreateTermRequest> requests) {
        List<Term> terms = service.addTermsBulk(user, listId, requests);
        return Response.created(
                UriBuilder.fromResource(TermListResource.class)
                        .path(listId.toString())
                        .path("terms/bulk")
                        .build()
        ).entity(terms).build();
    }

    @GET
    @Path("/department/{deptId}")
    public Response byDepartment(@Auth UserEntity user,
                                 @PathParam("deptId") Integer deptId) {
        List<TermList> lists = service.listByDepartment(user, deptId);
        return Response.ok(lists).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response byUser(@Auth UserEntity user,
                           @PathParam("userId") Integer userId) {
        List<TermList> lists = service.listByUser(user, userId);
        return Response.ok(lists).build();
    }

    @GET
    @Path("/{listId}/terms")
    public Response listTerms(@Auth UserEntity user,
                              @PathParam("listId") Integer listId) {
        List<Term> terms = service.listTerms(user, listId);
        return Response.ok(terms).build();
    }


}
