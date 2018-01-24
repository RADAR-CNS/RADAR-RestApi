package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.radarcns.domain.managementportal.Project;

public class Subject {

    @JsonProperty
    private String subjectId;

    @JsonProperty
    private String status;

    @JsonProperty
    private String humanReadableId;

    @JsonProperty
    private Project project;

    @JsonProperty
    private List<Source> sources;

    @JsonProperty
    private String lastSeen;

    public Subject subjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Subject status(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Subject humanReadableId(String humanReadableId) {
        this.humanReadableId = humanReadableId;
        return this;
    }

    public String getHumanReadableId() {
        return humanReadableId;
    }

    public void setHumanReadableId(String humanReadableId) {
        this.humanReadableId = humanReadableId;
    }

    public Subject project(Project project) {
        this.project = project;
        return this;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }


    public Subject sources(List<Source> sources) {
        this.sources = sources;
        return this;
    }

    public Subject addSource(Source source) {
        this.sources.add(source);
        return this;
    }


    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public Subject lastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}
