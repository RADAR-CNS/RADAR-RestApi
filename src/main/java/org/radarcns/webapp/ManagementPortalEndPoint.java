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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.security.exception.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.webapp.util.BasePath.PROJECT;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;
import static org.radarcns.webapp.util.Parameter.STUDY_NAME;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import org.radarcns.managementportal.MpClient;
import org.radarcns.managementportal.Project;
import org.radarcns.managementportal.Subject;
import org.radarcns.webapp.util.ResponseHandler;

import static org.radarcns.auth.authorization.Permission.SUBJECT_READ;
import static org.radarcns.auth.authorization.Permission.PROJECT_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.security.utils.SecurityUtils.getJWT;

/**
 *  Management Portal web-app. Function set to access subject and source information from MP.
 *  A subject is a person enrolled for in a study. A source is a device linked to the subject.
 */
@Path("/" + "mp")
public class ManagementPortalEndPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementPortalEndPoint.class);

    private static final String PROJECT_NAME = "projectName";

    @Context
    private ServletContext context;
    @Context
    private HttpServletRequest request;

    //--------------------------------------------------------------------------------------------//
    //                                        SUBJECTS                                            //
    //--------------------------------------------------------------------------------------------//
    /**
     * JSON function that returns all available subject.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SUBJECTS)
    @Operation(summary = "Return a list of subjects from the management portal",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getAllSubjectsJson() {
        try {
            checkPermission(getJWT(request), SUBJECT_READ);
            MpClient mpClient = new MpClient(context);
            Response response = MpClient.getJsonResponse(mpClient.getSubjects());
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact "
                    + "the service administrator.");
        }
    }


    /**
     * JSON function that returns all available subject based on the Study ID (Project ID).
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + PROJECT + "/{" + STUDY_NAME + "}" + "/" + SUBJECTS)
    @Operation(summary = "Return a list of subjects contained within a study",
            description = "Each subject can have multiple sourceID associated with him")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getAllSubjectsJsonFromStudy(
            @PathParam(STUDY_NAME) String studyName
    ) {
        try {
            checkPermissionOnProject(getJWT(request), SUBJECT_READ, studyName);
            MpClient mpClient = new MpClient(context);
            Response response = MpClient.getJsonResponse(
                    mpClient.getAllSubjectsFromStudy(studyName));
            LOGGER.info("Response : " + response.toString());
            return response;
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact "
                    + "the service administrator.");
        }
    }


    /**
     * JSON function that returns all information related to the given subject identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + SUBJECTS + "/{" + SUBJECT_ID + "}")
    @Operation(summary = "Return the information related to given subject identifier",
            description = "Source infomation not present right now")
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
            MpClient mpClient = new MpClient(context);
            Subject subject = mpClient.getSubject(subjectId);
            checkPermissionOnProject(getJWT(request), SUBJECT_READ,
                    subject.getProject().getProjectName());
            Response response = MpClient.getJsonResponse(subject);
            LOGGER.info("Response : " + response.toString());
            return response;
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


    //--------------------------------------------------------------------------------------------//
    //                                       PROJECTS                                             //
    //--------------------------------------------------------------------------------------------//

    /**
     * JSON function that returns all available projects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + PROJECT)
    @Operation(summary = "Return a list of projects",
            description = "Each project can have multiple deviceID associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return a list of subject.avsc objects")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getAllProjectsJson() {
        try {
            checkPermission(getJWT(request), PROJECT_READ);
            MpClient mpClient = new MpClient(context);
            Response response = MpClient.getJsonResponse(mpClient.getAllProjects(context));
            LOGGER.info("Response : " + response.getEntity());
            return response;
        } catch (AccessDeniedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonAccessDeniedResponse(request, exc.getMessage());
        } catch (NotAuthorizedException exc) {
            LOGGER.error(exc.getMessage(), exc);
            return ResponseHandler.getJsonNotAuthorizedResponse(request, exc.getMessage());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getJsonErrorResponse(request, "Your request cannot be"
                    + "completed. If this error persists, please contact"
                    + " the service administrator.");
        }
    }


    /**
     * JSON function that returns all information related to the given project identifier.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/" + PROJECT + "/{" + PROJECT_NAME + "}")
    @Operation(summary = "Return the information related to given project identifier",
            description = "Each project can have multiple deviceID associated with it")
    @ApiResponse(responseCode = "500", description = "An error occurs while executing, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "204", description = "No value for the given parameters, in the body"
            + "there is a message.avsc object with more details")
    @ApiResponse(responseCode = "200", description = "Return the subject.avsc object associated with the "
            + "given subject identifier")
    @ApiResponse(responseCode = "401", description = "Access denied error occured")
    @ApiResponse(responseCode = "403", description = "Not Authorised error occured")
    public Response getProjectJson(
            @PathParam(PROJECT_NAME) String projectName
    ) {
        try {
            checkPermissionOnProject(getJWT(request), PROJECT_READ, projectName);
            MpClient mpClient = new MpClient(context);
            Project project = mpClient.getProject(projectName, context);
            Response response = MpClient.getJsonResponse(project);
            LOGGER.info("Response : " + response.toString());
            return response;
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
}
