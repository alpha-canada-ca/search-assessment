package ca.canada.digital.search.assessment.resource;

import ca.canada.digital.search.assessment.api.CreateDepartmentRequest;
import ca.canada.digital.search.assessment.model.Department;
import ca.canada.digital.search.assessment.model.Language;
import ca.canada.digital.search.assessment.model.UserEntity;
import ca.canada.digital.search.assessment.service.DepartmentService;
import ca.canada.digital.search.assessment.service.LanguageService;
import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.Optional;

@Path("/languages")
@Produces(MediaType.APPLICATION_JSON)
public class LanguageResource {
    private static final int MAX_LIMIT = 100;
    private final LanguageService languageService;

    public LanguageResource(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GET
    @UnitOfWork
    public Response listLanguages(@Auth UserEntity user) {
        List<Language> langs = languageService.listLanguages();
        return Response.ok(langs).build();
    }

    @GET
    @Path("/{langId}")
    @UnitOfWork
    public Response getLanguage(@Auth UserEntity user, @PathParam("langId") Integer langId) {
        Optional<Language> lang = languageService.getLanguage(langId);
        return Response.ok(lang).build();
    }


}
