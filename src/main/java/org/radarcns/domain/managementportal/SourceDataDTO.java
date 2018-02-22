package org.radarcns.domain.managementportal;


import java.io.Serializable;
import java.util.Objects;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the SourceData entity.
 */
public class SourceDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //Source data type.
    @NotNull
    private String sourceDataType;


    private String sourceDataName;

    //Default data frequency
    private String frequency;

    //Measurement unit.
    private String unit;

    // Define if the samples are RAW data or instead they the result of some computation
    private String processingState;

    //  the storage
    private String dataClass;

    private String keySchema;

    private String valueSchema;

    private String topic;

    private String provider;

    private boolean enabled = true;

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getProcessingState() {
        return processingState;
    }

    public void setProcessingState(String processingState) {
        this.processingState = processingState;
    }

    public String getDataClass() {
        return dataClass;
    }

    public void setDataClass(String dataClass) {
        this.dataClass = dataClass;
    }

    public String getKeySchema() {
        return keySchema;
    }

    public void setKeySchema(String keySchema) {
        this.keySchema = keySchema;
    }

    public String getValueSchema() {
        return valueSchema;
    }

    public void setValueSchema(String valueSchema) {
        this.valueSchema = valueSchema;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSourceDataName() {
        return sourceDataName;
    }

    public void setSourceDataName(String sourceDataName) {
        this.sourceDataName = sourceDataName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SourceDataDTO sourceDataDto = (SourceDataDTO) o;
        if (sourceDataDto.sourceDataName == null || sourceDataName == null) {
            return false;
        }
        return Objects.equals(sourceDataName, sourceDataDto.sourceDataName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceDataName);
    }

    @Override
    public String toString() {
        return "SourceDataDTO{"
                + ", sourceDataType='" + sourceDataType + '\''
                + ", sourceDataName='" + sourceDataName + '\''
                + ", frequency='" + frequency + '\''
                + ", unit='" + unit + '\''
                + ", processingState=" + processingState
                + ", dataClass=" + dataClass
                + ", keySchema='" + keySchema + '\''
                + ", valueSchema='" + valueSchema + '\''
                + ", topic='" + topic + '\''
                + ", provider='" + provider + '\''
                + ", enabled=" + enabled
                + '}';
    }
}