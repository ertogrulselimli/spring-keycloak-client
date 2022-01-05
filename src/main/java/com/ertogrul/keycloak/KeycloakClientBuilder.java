package com.ertogrul.keycloak;

public class KeycloakClientBuilder {

    private   String clientId;

    private String clientSecret;

    private   String serverUrl;

    private String realm;



    public KeycloakClientBuilder clientId(final String clientId){
        this.clientId=clientId;return this;
    }


    public KeycloakClientBuilder clientSecret(final String clientSecret){
        this.clientSecret=clientSecret;return this;
    }


    public KeycloakClientBuilder serverUrl(final String serverUrl){
        this.serverUrl=serverUrl;
        return this;
    }


    public KeycloakClientBuilder realm(final String realm){
        this.realm=realm;
        return this;
    }


    public static KeycloakClientBuilder builder(){
        return new KeycloakClientBuilder();
    }

    public KeycloakClient build(){
        KeycloakClient client=new KeycloakClient();
        client.setClientId(this.clientId);
        client.setClientSecret(this.clientSecret);
        client.setRealm(this.realm);
        client.setServerUrl(this.serverUrl);
        client.configureKeyCloak();
        return client;
    }

}
