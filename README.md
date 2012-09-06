# UFRJ NED Parent module 


This maven module is the parent for the `rest-api` and the `search-service` module.

## Build instructions

To build the whole project : 

```
mvn clean install
```

## Run instructions

Specific run instructions are provided in each module's README.

## Configuration 

For both sub-modules to work properly, the environment variable `CASABLANCA_PROFILES_DIR` should point to a directory containing the different profiles.

Some are provided in `search-service/src/main/resources/profiles`

```
$ export CASABLANCA_PROFILES_DIR={PROJECT-DIR}/search-service/src/main/resources/profiles
```
## Further documentation

Further documentation about the project can be found in `doc/Project Description.md`
