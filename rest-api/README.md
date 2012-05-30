# Rest API

This module provides a Rest Web Service to perform searches.

It uses the JAX-RS API and is based on Jersey and Grizzly



## Run the server

To run the Grizzly HTTP server :

```
mvn exec:java
```

Make sure the whole project has been build before running the server.


## Available services

### Profiles

A list of services can be accessed at `http://localhost:9998/ned/profiles`. This will give the names of every available profile together with their `id`

A specific profile can be seen at : `http://localhost:9998/ned/profiles/{profile-id}`

### Search

A search for Casablanca on the default profile can me made at :`http://localhost:9998/ned/search/Casablanca`

The profile can be choosen by specifying a `profile` query param : `http://localhost:9998/ned/search/Casablanca?profile=2`

