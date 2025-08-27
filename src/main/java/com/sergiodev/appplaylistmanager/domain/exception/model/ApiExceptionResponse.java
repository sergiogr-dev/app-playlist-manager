package com.sergiodev.appplaylistmanager.domain.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(description = "This model is used to return errors in RFC 7807 which created a generalized error-handling schema composed by five parts")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiExceptionResponse(
    @Schema(description = "The unique uri identifier that categorizes the error", name = "type",
        requiredMode = Schema.RequiredMode.REQUIRED, example = "/errors/authentication/not-authorized")
    String type,

    @Schema(description = "A brief, human-readable message about the error", name = "title",
        requiredMode = Schema.RequiredMode.REQUIRED, example = "The user does not have autorization")
    String title,

    @Schema(description = "The unique error code", name = "code",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "192")
    String code,

    @Schema(description = "A human-readable explanation of the error", name = "detail",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String[] detail,

    @Schema(description = "A URI that identifies the specific occurrence of the error", name = "instance",
        requiredMode = Schema.RequiredMode.REQUIRED, example = "/errors/authentication/not-authorized/01")
    String instance
) {
    public ApiExceptionResponse(String type, String title, String code, String[] detail) {
        this(type, title, code, detail, null);
    }

    @Override
    public String toString() {
        return "ApiExceptionResponse{" +
               "type='" + type + '\'' +
               ", title='" + title + '\'' +
               ", code='" + code + '\'' +
               ", detail=" + Arrays.toString(detail) +
               ", instance='" + instance + '\'' +
               '}';
    }
}
