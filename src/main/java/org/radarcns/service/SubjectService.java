package org.radarcns.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.radarcns.domain.managementportal.Subject;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);

    private ManagementPortalClient managementPortalClient;

    private SourceService sourceService;

    @Inject
    public SubjectService(ManagementPortalClient managementPortalClient, SourceService
            sourceService) {
        this.managementPortalClient = managementPortalClient;
        this.sourceService = sourceService;
    }

    public static boolean checkSourceAssignedToSubject(Subject subject, String sourceId) {
        if (subject.getSources().stream().filter(p -> p.getSourceId().equals(sourceId))
                .collect(Collectors.toList()).isEmpty()) {
            LOGGER.error("Cannot find source-id " + sourceId + "for subject" + subject.getId());
            throw new BadRequestException(
                    "Source-id " + sourceId + " is not available for subject " +
                            subject.getId());
        }
        return true;
    }

    private org.radarcns.domain.restapi.Subject buildSubject(
            org.radarcns.domain.managementportal.Subject subject) {
        return new org.radarcns.domain.restapi.Subject()
                .subjectId(subject.getId())
                .projectName(subject.getProject().getProjectName())
                .status(subject.getStatus())
                .humanReadableId(subject.getHumanReadableIdentifier())
                .sources(this.sourceService.buildSources(subject.getId(), subject.getSources()));
    }

    public List<org.radarcns.domain.restapi.Subject> getAllSubjectsFromProject(String projectName)
            throws IOException, NotFoundException {
        // returns NotFound if a project is not available
        this.managementPortalClient.getProject(projectName);
        return this.managementPortalClient.getAllSubjectsFromProject(projectName).stream()
                .map(this::buildSubject).collect(Collectors.toList());
    }

    public org.radarcns.domain.restapi.Subject getSubjectBySubjectId(String projectName,
            String subjectId) throws IOException, NotFoundException {
        this.managementPortalClient.getProject(projectName);
        return this.buildSubject(this.managementPortalClient.getSubject(subjectId));
    }
}