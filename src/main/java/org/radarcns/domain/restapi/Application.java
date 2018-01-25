package org.radarcns.domain.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Application {

    /**
     * Hardware identifier of client application.
     */
    @JsonProperty
    private java.lang.String ipAddress;
    /**
     * Time since last app start (s).
     */
    @JsonProperty
    private double uptime;
    /**
     * Server connection status.
     */
    @JsonProperty
    private ServerStatus serverStatus;
    /**
     * Number of records currently being cached.
     */
    @JsonProperty
    private int recordsCached;
    /**
     * Number of records sent since application start.
     */
    @JsonProperty
    private int recordsSent;
    /**
     * Number of unsent records.
     */
    @JsonProperty
    private int recordsUnsent;

    /**
     * Default constructor.  Note that this does not initialize fields to their default values from
     * the schema.  If that is desired then one should use <code>newBuilder()</code>.
     */
    public Application() {
    }

    /**
     * All-args constructor.
     *
     * @param ipAddress Hardware identifier of client application.
     * @param uptime Time since last app start (s).
     * @param serverStatus Server connection status.
     * @param recordsCached Number of records currently being cached.
     * @param recordsSent Number of records sent since application start.
     * @param recordsUnsent Number of unsent records.
     */
    public Application(java.lang.String ipAddress, java.lang.Double uptime,
            ServerStatus serverStatus,
            java.lang.Integer recordsCached, java.lang.Integer recordsSent,
            java.lang.Integer recordsUnsent) {
        this.ipAddress = ipAddress;
        this.uptime = uptime;
        this.serverStatus = serverStatus;
        this.recordsCached = recordsCached;
        this.recordsSent = recordsSent;
        this.recordsUnsent = recordsUnsent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public double getUptime() {
        return uptime;
    }

    public void setUptime(double uptime) {
        this.uptime = uptime;
    }

    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(ServerStatus serverStatus) {
        this.serverStatus = serverStatus;
    }

    public int getRecordsCached() {
        return recordsCached;
    }

    public void setRecordsCached(int recordsCached) {
        this.recordsCached = recordsCached;
    }

    public int getRecordsSent() {
        return recordsSent;
    }

    public void setRecordsSent(int recordsSent) {
        this.recordsSent = recordsSent;
    }

    public int getRecordsUnsent() {
        return recordsUnsent;
    }

    public void setRecordsUnsent(int recordsUnsent) {
        this.recordsUnsent = recordsUnsent;
    }
}