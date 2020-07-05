# microservices
This repository is for micro service codings
Chapter 5. Configuring Hot Rod Clients
Data Grid services expose a Hot Rod endpoint at port 11222.

By default, Data Grid requires user authentication for data access and encryption for client connections.

Authentication
Data Grid authorizes data access requests with credentials that you specify with the APPLICATION_USER and APPLICATION_PASSWORD parameters.
Encryption
When Data Grid pods start they generate TLS certificate/key pairs and save them in the service-certs secret. The TLS certificates are signed by the OpenShift certificate authority (CA).
5.1. Configuring Truststores with Hot Rod
Set trustStorePath to the location of a valid certificate in PEM format in your Hot Rod client configuration. The Hot Rod Java client builds an in-memory Java keystore with all certificates found in the path.

On OpenShift

Specify the OpenShift certificate authority (CA) bundle.
/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt
Outside OpenShift

Get tls.crt from the service-certs secret.

$ oc get secret service-certs \
  -o jsonpath='{.data.tls\.crt}' \
  | base64 -d > tls.crt
Specify the path to tls.crt in your client configuration.
5.2. Client Intelligence
Client intelligence refers to mechanisms the Hot Rod protocol provides so that clients can locate and send requests to Data Grid pods.

On OpenShift

Clients can access the internal IP addresses for pods so you can use any client intelligence. The default intelligence, HASH_DISTRIBUTION_AWARE, is recommended because it allows clients to route requests to primary owners, which improves performance.

Outside OpenShift

Use BASIC intelligence only.

Reference

org.infinispan.client.hotrod.configuration.ClientIntelligence
5.3. Creating External Routes for Hot Rod
Hot Rod clients running outside OpenShift access Data Grid pods through routes with passthrough termination.

Procedure

Create a route with passthrough termination.

$ oc create route passthrough ${ROUTE_NAME} \
  --port=hotrod \
  --service ${APPLICATION_NAME}
For example:

$ oc create route passthrough cache-service-hotrod-route \
  --port=hotrod \
  --service cache-service
Get the Hot Rod route hostname from .spec.host.

$ oc get route cache-service-hotrod-route -o jsonpath="{.spec.host}"

cache-service-hotrod-route-rhdg-helloworld.192.0.2.0.nip.io
5.4. Hostnames for Data Grid Services
Use the hostname for Data Grid that corresponds to the location of your Hot Rod client.

In the Same OpenShift Namespace

Use APPLICATION_NAME.

For example:

.host("cache-service")
In Different OpenShift Namespaces

Use the internal service DNS name in this form:
APPLICATION_NAME.SERVICE_NAMESPACE.svc

For example:

.host("cache-service.rhdg-helloworld.svc")
Outside OpenShift

Use the Hot Rod route hostname.

For example:

.host("cache-service-hotrod-route-rhdg-helloworld.192.0.2.0.nip.io")
5.5. Configuring Hot Rod Clients Programmatically
Use the ConfigurationBuilder class to programmatically configure Hot Rod clients to access Data Grid clusters.

Call the create() method to create a configuration bean that you can pass to the RemoteCacheManager.
Use the authentication() and ssl() methods to configure authentication and encryption.
Reference

org.infinispan.client.hotrod.configuration.ConfigurationBuilder
5.5.1. Hot Rod Configuration Builder On OpenShift
Configuration bean for Hot Rod clients running on OpenShift:

ConfigurationBuilder builder = new ConfigurationBuilder();
builder.addServer()
	// Connection
	.host("${APPLICATION_NAME}.${SERVICE_NAMESPACE}.svc").port(11222)
	.security()
        // Authentication
        .authentication().enable()
        .username("${USERNAME}")
        .password("${PASSWORD}")
        .serverName("${APPLICATION_NAME}")
        .saslQop(SaslQop.AUTH)
        // Encryption
        .ssl()
        .trustStorePath​(/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt);
5.5.2. Hot Rod Configuration Builder Outside OpenShift
Configuration bean for Hot Rod clients running outside OpenShift:

ConfigurationBuilder builder = new ConfigurationBuilder();
builder.addServer()
	// Connection
	.host("${HOTROD_ROUTE_HOSTNAME}").port(443)
	// Use BASIC client intelligence.
	.clientIntelligence(ClientIntelligence.BASIC)
	.security()
        // Authentication
        .authentication().enable()
        .username("${USERNAME}")
        .password("${PASSWORD}")
        .serverName("${APPLICATION_NAME}")
        .saslQop(SaslQop.AUTH)
        // Encryption
        .ssl()
        .sniHostName("${HOTROD_ROUTE_HOSTNAME}")
        .trustStorePath​(path/to/tls.crt);
5.6. Setting Hot Rod Client Properties
Use Hot Rod client configuration properties to specify Data Grid hostnames and ports, authentication details, and TLS certificates.

Procedure

Create a hotrod-client.properties file that contains your Hot Rod client configuration.
Add hotrod-client.properties to the classpath.
Reference

org.infinispan.client.hotrod.configuration
5.6.1. Hot Rod Configuration Properties On OpenShift
Configuration properties for Hot Rod clients running on OpenShift:

# Connection
infinispan.client.hotrod.server_list=${APPLICATION_NAME}.${SERVICE_NAMESPACE}.svc:11222

# Authentication
infinispan.client.hotrod.use_auth=true
infinispan.client.hotrod.auth_username=${USERNAME}
infinispan.client.hotrod.auth_password=${PASSWORD}
infinispan.client.hotrod.auth_server_name=${APPLICATION_NAME}
infinispan.client.hotrod.sasl_properties.javax.security.sasl.qop=auth

# Encryption
infinispan.client.hotrod.trust_store_path=/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt
5.6.2. Hot Rod Configuration Properties Outside OpenShift
Configuration properties for Hot Rod clients running outside OpenShift:

# Connection
infinispan.client.hotrod.server_list=${HOTROD_ROUTE_HOSTNAME}:443

# Use BASIC client intelligence.
infinispan.client.hotrod.client_intelligence=BASIC

# Authentication
infinispan.client.hotrod.use_auth=true
infinispan.client.hotrod.auth_username=${USERNAME}
infinispan.client.hotrod.auth_password=${PASSWORD}
infinispan.client.hotrod.auth_server_name=${APPLICATION_NAME}
infinispan.client.hotrod.sasl_properties.javax.security.sasl.qop=auth

# Encryption
infinispan.client.hotrod.sni_host_name=${HOTROD_ROUTE_HOSTNAME}
infinispan.client.hotrod.trust_store_path=path/to/tls.crt
