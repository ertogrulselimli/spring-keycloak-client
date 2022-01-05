package com.ertogrul.keycloak;

import com.ertogrul.keycloak.exception.KeyCloakClientException;
import com.ertogrul.keycloak.util.Utils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.List;
import java.util.Set;


public class KeycloakClient implements Serializable {

    private   String clientId;

    private String clientSecret;

    private   String serverUrl;

    private String realm;

    private Keycloak keycloak;

    public KeycloakClient() {
    }


    public KeycloakClient(String clientId, String clientSecret, String serverUrl, String realm) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.serverUrl = serverUrl;
        this.realm = realm;
    }



    public void  configureKeyCloak() {
        this.keycloak = KeycloakBuilder.builder().
                clientId(this.clientId).
                clientSecret(this.clientSecret).
                grantType(OAuth2Constants.CLIENT_CREDENTIALS).
                realm(this.realm).
                serverUrl(this.serverUrl).
                build();
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public String getRealm() {
        return realm;
    }


    public void setRealm(String realm) {
        this.realm = realm;
    }



    public UserRepresentation findByUsername(final String username){
      return   this.keycloak.realm(this.realm).users().search(username,true).get(0);//as per realm there can not be 2 user with the the same username
    }



    @Deprecated
    public void newRole(RoleRepresentation roleRepresentation){
        final RealmResource realm = this.keycloak.realm(this.realm);
        final ClientRepresentation clientRepresentation = realm.clients().findByClientId(this.clientId).get(0);
        realm.clients().get(clientRepresentation.getId()).roles().create(roleRepresentation);
    }




    public void updateUser(UserRepresentation userRepresentation){
        if(Utils.isNullOrEmpty(userRepresentation.getId())){
            throw new IllegalArgumentException("in Update user operation id can not be null");
        }
        final UserResource userResource = this.keycloak.realm(this.realm).users().get(userRepresentation.getId());
        userResource.update(userRepresentation);
    }



    public void resetPassword(final String username,final String password){
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        final String id = this.keycloak.realm(this.realm).users().search(username, true).get(0).getId();
        final UserResource userResource = this.keycloak.realm(this.realm).users().get(id);
        userResource.resetPassword(passwordCred);
    }



    @Deprecated
    public void addRole(final String username,final String role){
        final RealmResource realm =getRealmResource();
        final ClientRepresentation clientRepresentation = realm.clients().findByClientId(this.clientId).get(0);
        final RoleRepresentation roleRepresentation = realm.clients().get(clientRepresentation.getId()).roles().get(role).toRepresentation();
        final UserRepresentation userRepresentation = this.findByUsername(username);
        realm.users().get(userRepresentation.getId()).roles().clientLevel(clientRepresentation.getId()).add(List.of(roleRepresentation));
    }



    public void removeRole(final String username,final String role){
        final RealmResource realm = getRealmResource();
        final ClientRepresentation clientRepresentation = realm.clients().findByClientId(this.clientId).get(0);
        final RoleRepresentation roleRepresentation = realm.clients().get(clientRepresentation.getId()).roles().get(role).toRepresentation();
        final UserRepresentation byUsername = this.findByUsername(username);
        realm.users().get(byUsername.getId()).roles().clientLevel(clientRepresentation.getId()).remove(List.of(roleRepresentation));

    }


    //begin user



    public UserRepresentation newUser(UserRepresentation userRepresentation){
        if(!Utils.isNullOrEmpty(userRepresentation.getId())){
            throw new IllegalArgumentException("in new user operation id can not be set");
        }
        Response response = this.keycloak.realm(this.realm).users().create(userRepresentation);
            if (response.getStatus() > 302) {
                final String s = response.readEntity(String.class);
                throw new RuntimeException(s);
            }
         return   this.findByUsername(userRepresentation.getUsername());
    }



    public void addUserRole(final String username,final String role){
        final RealmResource realm = this.keycloak.realm(this.realm);
        final ClientResource clientResource = getClientResource();
        final ClientRepresentation clientRepresentation = clientResource.toRepresentation();
        final RoleRepresentation roleRepresentation = clientResource.roles().get(role).toRepresentation();
        final UserRepresentation byUsername = this.findByUsername(username);
        realm.users().get(byUsername.getId()).roles().clientLevel(clientRepresentation.getId()).add(List.of(roleRepresentation));
    }

     //Currently This admin api library does not support paginated listing  but we will use our database for this purpose


    public List<UserRepresentation> listClientUsers(){
        final RealmResource realmResource = getRealmResource();
         return realmResource.users().list();
    }

    //end user


    public  AccessTokenResponse authenticateUser(final String username,final String password) {
        Keycloak kc = KeycloakBuilder.builder().
                clientId(this.clientId).
                clientSecret(this.clientSecret).
                grantType(OAuth2Constants.PASSWORD).
                realm(this.realm).
                serverUrl(this.serverUrl).
                username(username).
                password(password).build();
        return kc.tokenManager().getAccessToken();
    }

    /*
      @GET
                   @Path("composites/clients/{clientUuid}")
                   @Produces(MediaType.APPLICATION_JSON)
                   Set<RoleRepresentation> getClientRoleComposites(@PathParam("clientUuid") String clientUuid);

                   @POST
                   @Path("composites")
                   @Consumes(MediaType.APPLICATION_JSON)
                   void addComposites(List<RoleRepresentation> rolesToAdd);


                   @DELETE
                   @Path("composites")
                   @Consumes(MediaType.APPLICATION_JSON)
                   void deleteComposites(List<RoleRepresentation> rolesToRemove);


     */



    //common method

    private ClientResource getClientResource(){
        final RealmResource realm = getRealmResource();
        final ClientRepresentation clientRepresentation = realm.clients().findByClientId(this.clientId).get(0);
       return realm.clients().get(clientRepresentation.getId());
    }

    private RealmResource getRealmResource(){
        return this.keycloak.realm(this.realm);
    }


    //end common method


    //Client Roles and permissions
    //note this not adds composite roles (permissions) you should call add permission or remove permission methods
    public RoleRepresentation newClientRole(RoleRepresentation roleRepresentation){

        //here check if role representation is not composite
        if(!roleRepresentation.isComposite())throw new KeyCloakClientException("Role must be set to Composite ture");
        roleRepresentation.setClientRole(true);
        final ClientResource clientResource = getClientResource();
        clientResource.roles().create(roleRepresentation);
        final RoleResource roleResource = clientResource.roles().get(roleRepresentation.getName());
        return roleResource.toRepresentation();
    }



    public void addClientRolePermissions(final String roleName, List<RoleRepresentation> permissions){
        //check if permissions is composite throw exception
        permissions.stream().forEach(p->{
            if (p.isComposite())throw new KeyCloakClientException("Permission can not be composite");
        });
        final ClientResource clientResource = getClientResource();
        clientResource.roles().get(roleName).addComposites(permissions);
    }


    public RoleRepresentation getClientRoleByName(final String name){
        final ClientResource clientResource = getClientResource();
        final RoleResource roleResource = clientResource.roles().get(name);
        return roleResource.toRepresentation();
    }



    public Set<RoleRepresentation> getClientRolePermissions(final String roleName ){
        final ClientResource clientResource = getClientResource();
        final Set<RoleRepresentation> roleComposites = clientResource.roles().get(roleName).getRoleComposites();
        return roleComposites;
    }


    //this brings all roles you should filter composite or not
    public List<RoleRepresentation> getClientRoles(){
        return   getClientResource().roles().list();
    }




}
