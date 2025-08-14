package ca.canada.digital.search.assessment.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.hibernate.exception.ConstraintViolationException;

public class DatabaseConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        String constraint = e.getConstraintName();

        String msg = (constraint != null && !constraint.isBlank())
                ? String.format("Database constraint violated: %s", constraint)
                : "A database constraint was violated.";

        return Response
                .status(Response.Status.CONFLICT)
                .entity(new ErrorMessage(msg))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public static class ErrorMessage {
        public final String message;
        public ErrorMessage(String message) { this.message = message; }
    }
}

