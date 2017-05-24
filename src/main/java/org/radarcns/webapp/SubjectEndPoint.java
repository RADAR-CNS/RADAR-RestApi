package org.radarcns.webapp;

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

import static org.radarcns.webapp.util.BasePath.AVRO;
import static org.radarcns.webapp.util.BasePath.GET_ALL_SUBJECTS;
import static org.radarcns.webapp.util.BasePath.GET_SUBJECT;
import static org.radarcns.webapp.util.BasePath.SUBJECT;
import static org.radarcns.webapp.util.Parameter.STUDY_ID;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.radarcns.avro.restapi.subject.Cohort;
import org.radarcns.avro.restapi.subject.Subject;
import org.radarcns.dao.SubjectDataAccessObject;
import org.radarcns.security.Param;
import org.radarcns.webapp.util.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subject web-app. Function set to access subject information. A subject is a person enrolled for
 *      in a study.
 */
@Api
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
    @ApiOperation(
            value = "Return a list of subjects",
            notes = "Each subject can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return a list of subject.avsc objects")})
    public Response getAllSubjectsJson(
            @PathParam(STUDY_ID) String study
    ) {
        try {
            return ResponseHandler.getJsonResponse(request, getAllSubjectsWorker());
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
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/" + AVRO + "/" + GET_ALL_SUBJECTS + "/{" + STUDY_ID + "}")
    @ApiOperation(
            value = "Return a list of subjects",
            notes = "Each subject can have multiple sourceID associated with him")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing"),
            @ApiResponse(code = 204, message = "No value for the given parameters"),
            @ApiResponse(code = 200, message = "Return a byte array serialising a list of"
                + "subject.avsc objects")})
    public Response getAllSubjectsAvro(
            @PathParam(STUDY_ID) String study
    ) {
        try {
            return ResponseHandler.getAvroResponse(request, getAllSubjectsWorker());
        } catch (Exception exec) {
            LOGGER.error(exec.getMessage(), exec);
            return ResponseHandler.getAvroErrorResponse(request);
        }
    }

    /**
     * Actual implementation of AVRO and JSON getAllSubjects.
     **/
    private Cohort getAllSubjectsWorker() throws ConnectException {
        Cohort cohort = SubjectDataAccessObject.getAllSubjects(context);

        return cohort;
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
    @ApiOperation(
            value = "Return the information related to given subject identifier",
            notes = "Some information are not implemented yet. The returned values are hardcoded.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return the subject.avsc object associated with the "
                + "given subject identifier")})
    public Response getSubjectJson(
            @PathParam(SUBJECT_ID) String subject
    ) {
        try {
            return ResponseHandler.getJsonResponse(request, getSubjectWorker(subject));
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
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/" + AVRO + "/" + GET_SUBJECT + "/{" + SUBJECT_ID + "}")
    @ApiOperation(
            value = "Return the information related to given subject identifier",
            notes = "Some information are not implemented yet. The returned values are hardcoded.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An error occurs while executing, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 204, message = "No value for the given parameters, in the body"
                + "there is a message.avsc object with more details"),
            @ApiResponse(code = 200, message = "Return the subject.avsc object associated with the "
                + "given subject identifier")})
    public Response getSubjectAvro(
            @PathParam(SUBJECT_ID) String subject
    ) {
        try {
            return ResponseHandler.getAvroResponse(request, getSubjectWorker(subject));
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