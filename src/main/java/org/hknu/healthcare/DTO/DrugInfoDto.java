package org.hknu.healthcare.DTO;

public class DrugInfoDto {
    private String name;
    private String usage;
    private String precautions;
    private String sideEffects;

    public DrugInfoDto() {}

    public DrugInfoDto(String name, String usage, String precautions, String sideEffects) {
        this.name = name;
        this.usage = usage;
        this.precautions = precautions;
        this.sideEffects = sideEffects;
    }

    public String getName() { return name; }
    public String getUsage() { return usage; }
    public String getPrecautions() { return precautions; }
    public String getSideEffects() { return sideEffects; }

    public void setName(String name) { this.name = name; }
    public void setUsage(String usage) { this.usage = usage; }
    public void setPrecautions(String precautions) { this.precautions = precautions; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }
}