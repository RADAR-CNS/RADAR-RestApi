package org.radarcns.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import org.radarcns.catalog.SourceCatalog;
import org.radarcns.domain.managementportal.Source;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.domain.managementportal.SourceTypeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectService.class);
    private SourceMonitorService sourceMonitorService;

    private SourceCatalog sourceCatalog;

    public SourceService(SourceMonitorService sourceMonitorService, SourceCatalog sourceCatalog) {
        this.sourceMonitorService = sourceMonitorService;
        this.sourceCatalog = sourceCatalog;
    }


    public List<org.radarcns.domain.restapi.Source> buildSources(String subjectId,
            List<Source> sources) {
        return sources.stream().map(p -> buildSource(subjectId, p)).collect(Collectors.toList());
    }

    public org.radarcns.domain.restapi.Source buildSource(String subjectId, Source source) {
        SourceType sourceType = null;
        try {
            sourceType = this.sourceCatalog.getSourceType(source
                    .getSourceTypeProducer(), source.getSourceTypeModel(), source
                    .getSourceTypeCatalogVersion());
        } catch (NotFoundException | IOException e) {

            LOGGER.error("Cannot retrieve sourceType-type for given sourceType " + source.getSourceId());
            throw new IllegalStateException("Cannot retrive sourceType-type for given sourceType " + source
                    .getSourceId());
        }

        return new org.radarcns.domain.restapi.Source()
                .sourceId(source.getSourceId())
                .assigned(source.getAssigned())
                .sourceName(source.getSourceName())
                .sourceTypeCatalogVersion(source.getSourceTypeCatalogVersion())
                .sourceTypeProducer(source.getSourceTypeProducer())
                .sourceTypeModel(source.getSourceTypeModel())
                .effectiveTimeFrame(this.sourceMonitorService
                        .getEffectiveTimeFrame(subjectId, source.getSourceId(), sourceType));
    }

    public static SourceTypeIdentifier getSourceTypeIdFromSource(Source source) {
        return new SourceTypeIdentifier(source.getSourceTypeProducer() , source
                .getSourceTypeModel() ,source.getSourceTypeCatalogVersion());
    }

}
