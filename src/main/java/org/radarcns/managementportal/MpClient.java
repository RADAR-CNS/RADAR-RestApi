/*
 * Copyright 2017 King's College London
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

package org.radarcns.managementportal;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.radarcns.listener.managementportal.TokenManagerListener.ACCESS_TOKEN;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.radarcns.config.managementportal.Properties;
import org.radarcns.listener.managementportal.HttpClientListener;
import org.radarcns.listener.managementportal.TokenManagerListener;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.producer.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to interact with the RADAR Management Portal.
 */
public class MpClient {

    private static final Logger logger = LoggerFactory.getLogger(MpClient.class);
    private final OkHttpClient client;

    private static OAuth2AccessTokenDetails token;
    private ArrayList<Subject> subjects;


    /**
     * Client to interact with the RADAR Management Portal.
     * @param context {@link ServletContext} useful to retrieve shared {@link OkHttpClient} and
     *      {@code access token}.
     * @throws IllegalStateException in case the object cannot be created
     * @see HttpClientListener
     * @see TokenManagerListener
     */
    public MpClient(ServletContext context) {
        Objects.requireNonNull(context);

        this.client = HttpClientListener.getClient(context);
        this.token = (OAuth2AccessTokenDetails) context.getAttribute(ACCESS_TOKEN);
        subjects = null;
    }

    /**
     * Retrieves all {@link Subject} from the already computed list of subjects
     * using {@link ArrayList} of {@link Subject} else it calls a method for retrieving
     * the subjects from MP.
     * @return {@link ArrayList} of {@link Subject} if a subject is found
     */
    public ArrayList<Subject> getSubjects() {
        if (subjects == null) {
            subjects = getAllSubjects();
        }
        return subjects;
    }

    /**
     * Retrieves all {@link Subject} from Management Portal using {@link ServletContext} entity.
     * @return {@link ArrayList} of {@link Subject} retrieved from the Management Portal
     */
    private ArrayList<Subject> getAllSubjects() {

        try {
            URL getAllSubjects = new URL(Properties.validateMpUrl(), Properties.getSubjectPath());
            Request getAllSubjectsRequest = this.buildBaseRequest(getAllSubjects);
            Response response = this.client.newCall(getAllSubjectsRequest).execute();
            ArrayList<Subject> allSubjects = Subject
                    .getAllSubjectsFromJson(response.body().string());
            logger.info("Retrieved Subjects from MP.");
            return allSubjects;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            logger.error("Cannot get valid url");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Cannot retrieve subjects");
            return null;
        }
    }

    /**
     * Retrieves a {@link Subject} from the already computed list of subjects
     * using {@link ArrayList} of {@link Subject} entity.
     * @param subjectLogin {@link String} that has to be searched.
     * @return {@link Subject} if a subject is found
     */
    public Subject getSubject(@Nonnull String subjectLogin) {
        if (subjects != null) {
            for (Subject currentSubject : subjects) {
                if (subjectLogin.equals(currentSubject.getLogin())) {
                    return currentSubject;
                }
            }
        } else {
            try {
                return retrieveSubject(subjectLogin);
            } catch (IOException exc) {
                logger.error("Error : ", exc.fillInStackTrace());
            }
        }
        logger.info("Subject could not be retrieved.");
        return null;
    }

    /**
     * Retrieves a {@link Subject} from the Management Portal using {@link ServletContext} entity.
     * @param subjectLogin {@link String} of the Subject that has to be retrieved
     * @return {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    private Subject retrieveSubject(@Nonnull String subjectLogin) throws IOException {
        if (subjects != null) {
            return getSubject(subjectLogin);
        }
        try {
            URL getSubject = new URL(Properties.validateMpUrl(),
                    Properties.getSubjectPath() + "/" + subjectLogin);
            Request getSubjectsRequest = this.buildBaseRequest(getSubject);
            Response response = this.client.newCall(getSubjectsRequest).execute();
            if (response.isSuccessful()) {
                Subject subject = Subject.getObject(response.body().string());
                logger.info("Subject : " + subject.getJsonString());
                return subject;
            }
            if (Integer.valueOf(HTTP_NOT_FOUND).equals(response.code())) {
                logger.info("Subject {} is not present", subjectLogin);
                return null;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Cannot execute request to retrive subject : {}", subjectLogin);
            return null;
        }
    }

    /**
     * Retrieves all {@link Subject} from a study (or project) in the
     * Management Portal using {@link ServletContext} entity.
     * @param studyName {@link String} the study from which subjects to be retrieved
     * @return {@link List} of {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    public List<Subject> getAllSubjectsFromStudy(@Nonnull String studyName) throws
            IOException {

        if (subjects != null) {
            return subjects.stream()
                    .filter(s -> studyName.equals(s.getProject().getProjectName()))
                    .collect(Collectors.toList());
        }

        try {
            URL getSubjectsFromProject = new URL(Properties.validateMpUrl(),
                    Properties.getProjectPath() +"/" + studyName + '/' + SUBJECTS);
            Request getSubjectsRequest = this.buildBaseRequest(getSubjectsFromProject);
            Response response =  this.client.newCall(getSubjectsRequest).execute();
            if (response.isSuccessful()) {
                List<Subject> allSubjects = Subject.getAllSubjectsFromJson(response.body().string());
                logger.info("Retrieved Subjects from MP from Project " + studyName);
                return allSubjects;
            } else if (response.code() == HTTP_NOT_FOUND) {
                logger.info("Subjects for study {} are not present", studyName);
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.error("Cannot execute request to retrieve project : {}" , studyName);
            return null;
        }
        return null;
    }

    /**
     * Retrieves all {@link Project} from Management Portal using {@link ServletContext} entity.
     * @return {@link ArrayList} of {@link Project} retrieved from the Management Portal
     */
    public List<Project> getAllProjects() throws
            MalformedURLException, URISyntaxException {
        URL getSubjectsFromProject = new URL(Properties.validateMpUrl(),
                Properties.getProjectPath());
        Request getAllProjects = this.buildBaseRequest(getSubjectsFromProject);
        try (Response response = this.client.newCall(getAllProjects).execute()) {
            List<Project>  allProjects = Project.getAllObjects(response.body().string());
            logger.info("Retrieved Projects from MP");
            return allProjects;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Cannot retrieve projects");
        }
        return null;
    }

    /**
     * Retrieves a {@link Project} from the Management Portal using {@link ServletContext} entity.
     * @param projectName {@link String} of the Project that has to be retrieved
     * @return {@link Project} retrieved from the Management Portal
     */
    public Project getProject(String projectName) throws IOException {

        try {
            URL getSubjectsFromProject = new URL(Properties.validateMpUrl(),
                    Properties.getProjectPath()+'/'+projectName);
            Request getProject = this.buildBaseRequest(getSubjectsFromProject);
            Response response =  this.client.newCall(getProject).execute();
            String jsonData = RestClient.responseBody(response);
            if (response.isSuccessful()) {
                Project project = Project.getObject(jsonData);
                logger.info("Retrieved project {} from MP", projectName);
                return project;
            } else if (response.code() == HTTP_NOT_FOUND) {
                logger.info("Project {} is not present", projectName);
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.error("Cannot execute request to retrieve project : {}" , projectName);
            return null;
        }
        return null;
    }

    /**
     * Creates a {@link Response} entity from a provided {@link Object}.
     */
    public static javax.ws.rs.core.Response getJsonResponse(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonNode toJson = objectMapper.valueToTree(obj);

        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.OK;
        return javax.ws.rs.core.Response.status(status.getStatusCode()).entity(toJson).build();
    }

    private Request buildBaseRequest(URL url){
        return new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer "+token.getAccessToken())
                .url(url)
                .get()
                .build();
    }
}
