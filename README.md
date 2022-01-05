## keycloak-user-management


#Bean configuration

``assume you have configured following keycloak properties in your application.yml``

```````
keycloak:
  resource: testresource
  realm: testrealm
  auth-server-url: http://localhost:8080/auth
  bearer-only: true
  credentials:
    secret: your-keycloak-secret

```````
then you can configure your bean like the following
```````

  @Autowired
  KeycloakSpringBootProperties properties;
    

  @Bean
  public KeycloakClient  keycloakClient(){
          return   KeycloakClientBuilder.builder().
                  clientId(properties.getResource()).
                  clientSecret((String) properties.getCredentials().get("secret")).
                  realm(properties.getRealm()).serverUrl(properties.getAuthServerUrl()).build();
  } 


```````