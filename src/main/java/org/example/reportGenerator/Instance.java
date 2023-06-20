package org.example.reportGenerator;

public class Instance {
    String url, method, param, attack, evidence, info;

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getParam() {
        return param;
    }

    public String getAttack() {
        return attack;
    }

    public String getEvidence() {
        return evidence;
    }

    public String getInfo() {
        return info;
    }

    public Instance(){
        this.url = "";
        this.method = "";
        this.param = "";
        this.attack = "";
        this.evidence = "";
        this.info = "";
    }

    public Instance(String url, String method, String param, String attack, String evidence, String info) {
        this.url = url;
        this.method = method;
        this.param = param;
        this.attack = attack;
        this.evidence = evidence;
        this.info = info;
    }
}
