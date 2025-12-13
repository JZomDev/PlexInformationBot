package kekolab.javaplex;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.StringListDeserializer;

public class PlexClient extends PlexBaseItem {
    private String name;
    private String host;
    private String address;
    private Integer port;
    private String machineIdentifier;
    private String version;
    private String protocol;
    private String product;
    private String deviceClass;
    private String protocolVersion;
    @JsonDeserialize(using = StringListDeserializer.class)
    private List<String> protocolCapabilities;

    public PlexClient() {
        protocolCapabilities = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public String getMachineIdentifier() {
        return machineIdentifier;
    }
    public void setMachineIdentifier(String machineIdentifier) {
        this.machineIdentifier = machineIdentifier;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public String getDeviceClass() {
        return deviceClass;
    }
    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }
    public String getProtocolVersion() {
        return protocolVersion;
    }
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    public List<String> getProtocolCapabilities() {
        return protocolCapabilities;
    }
    public void setProtocolCapabilities(List<String> protocolCapabilities) {
        this.protocolCapabilities = protocolCapabilities;
    }

    
    
}
