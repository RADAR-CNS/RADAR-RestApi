/*
 * Copyright 2016 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.webapp;

import static org.radarcns.auth.authorization.Permission.SUBJECT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.security.utils.SecurityUtils.getJWT;
import static org.radarcns.webapp.util.BasePath.*;
import static org.radarcns.webapp.util.Parameter.STUDY_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import java.net.ConnectException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.restapi.subject.Cohort;
import org.radarcns.restapi.subject.Subject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.managementportal.MpClient;
import org.radarcns.security.Param;
import org.radarcns.security.exception.AccessDeniedException;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subject web-app. Function set to access subject information. A subject is a person enrolled for
 *      in a study.
 */
@Path("/" + SUBJECT)
public class SubjectEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectEndPoint.class);

    @Context private ServletContext context;
    @Context private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                        ALL SUBJECTS                                        //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + GET_ALL_SUBJECTS + "/{" + STUDY_ID + "}")
    @Operation(summary = "Return a list of subjects",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
        + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the body"
        + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getAllSubjectsJson(
            @PathParam(STUDY_ID) String study
    ) {
        try {
            checkPermission(getJWT(request), SUBJECT_READ);
            return ResponseHandler.getJsonResponse(request, getAllSubjectsWorker());
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all available subject.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + GET_ALL_SUBJECTS + "/{" + STUDY_ID + "}")
    @Operation(summary = "Return a list of subjects",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters")
    @ApiResponse(responseCode = "200", description = "Return a byte array serialising a list of"
        + "subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getAllSubjectsAvro(
            @PathParam(STUDY_ID) String study
    ) {
        try {
            checkPermission(getJWT(request), SUBJECT_READ);
            return ResponseHandler.getAvroResponse(request, getAllSubjectsWorker());
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllSubjects.
     **/
    private Cohort getAllSubjectsWorker() throws ConnectException {
        return SubjectDataAccessObject.getAllSubjects(context);
    }

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECT INFO                                        //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return the information related to given subject identifier",
            description = "Some information are not implemented yet. The returned values are hardcoded.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
        + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the body"
        + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return the subject.avsc object associated with the "
        + "given subject identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getSubjectJson(
            @PathParam(SUBJECT_ID) String subjectId
    ) {
        try {
            MpClient client = new MpClient(context);
            org.radarcns.managementportal.Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getJWT(request), SUBJECT_READ,
                    sub.getProject().getProjectName());
            return ResponseHandler.getJsonResponse(request, getSubjectWorker(subjectId));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                + "completed. If this error persists, please contact the service administrator.");
        }
    }

    /**
     * AVRO function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(AVRO_BINARY)
    @Path("/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}")
    @Operation(
            summary = "Return the information related to given subject identifier",
            description = "Some information are not implemented yet. The returned values are hardcoded.")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
        + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the body"
        + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return the subject.avsc object associated with the "
        + "given subject identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getSubjectAvro(
            @PathParam(SUBJECT_ID) String subjectId
    ) {
        try {
            MpClient client = new MpClient(context);
            org.radarcns.managementportal.Subject sub = client.getSubject(subjectId);
            checkPermissionOnProject(getJWT(request), SUBJECT_READ,
                    sub.getProject().getProjectName());
            return ResponseHandler.getAvroResponse(request, getSubjectWorker(subjectId));
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getSubject.
     **/
    private Subject getSubjectWorker(String subjectId) throws ConnectException {
        Param.isValidSubject(subjectId);

        Subject subject = new Subject();

        if (SubjectDataAccessObject.exist(subjectId, context)) {
            subject = SubjectDataAccessObject.getSubject(subjectId, context);
        }

        return subject;
    }

}
