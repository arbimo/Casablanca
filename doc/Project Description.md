%Project overview
%Arthur Bit-Monnot


# Project description

This project is part of a Scientific Initiation conducted at the Universidade Federal do Rio de Janeiro.

It aims to provide tools to make Named Entities Disambiguation (NED) easier.


# Conception overview



# Dataset

An overview of the datasets that were reviewed in order to choose one
adapted to this project is available in the PDF file [ ... ].

## Main dataset : [Yago]

The dataset Yago2 has been chosen for the following reasons :

 - it has a clean schema with well defined types and a limited number of properties
 - it is centered on words and therefore provides several interesting
predicates for NED


Yago2 comes in two versions, both available at
[Yago's downloads](http://www.mpi-inf.mpg.de/yago-naga/yago/downloads.html) :

 - the core version that provides all entities extracted from Wikipedia, types
(obtained by merging Wikipedia categories and WordNet definitions) and several
"text" predicates (names, label, ...)
 - the full version that comes with additional information, including Wikipedia
links and context

## Deployment 

The publicly available (through SPARQL) version of Yago2[^public-yago] is outdated and it was therefore necessary to deploy a local version of the dataset.

I chose [Fuseki] as an easy to deploy SPARQL end point. It provides support for SPARQL 1.1 and Yago core provides a ready to use TDB format for Yago core

> **Note** : Fuseki is an Apache incubator project part of [Jena]. His store is based on the Jena [TDB] format.

The main problem with Fuseki/TDB is that the time needed to had triples grows with the size of the dataset. Therefore it was nearly impossible to export the whole Yago dataset (operation would have required weeks on a classic computer). 
The solution was to base our self on the Yago core dataset (which is available in the TDB format) and extend it with the predicates we are interested in. More information on this operation will be available in a dataset-creation document soon.

[^public-yago]: <http://lod.openlinksw.com/sparql>

## Other dataset

While Yago is the main dataset that we plan to use for this project development. A goal is to stay generic in order to be compatible with other datasets.

Therefore, every dataset specific data should be stored in an external configuration file.

## RDF stores

Every RDF store has its own limitations on several aspects :

 - SPARQL : different versions supported, home made extensions
 - request : might be more or less tolerant on request sizes
 - support for full text search

Right now the Search Service works pretty well on Fuseki but might have unexpected behaviour on other stores (it, at least, has on [Virtuoso] when it comes to big requests).


#  Search Service

Implementation is based on [Scala] to be able to use well tested java libraries and frameworks while using a high-level language.

The search service provides mainly a SearchBackend class and a factory to create it from a configuration file.

A BackendManager class is also provided to make loading configurations and switching between them easier.

The API is rather Scala-centric right now but it shouldn't be difficult to make it Java friendly (since Scala is a JVM language that doesn't break compatibility with Java).

As dependencies, it uses [Jena] to handle SPARQL queries and [Logula] as logging system

## Configuration 

Configuration comes as an XML file specifying :

 - the SPARQL end point to use
 - how candidates are selected
 - how candidates are ranked
 - which type constraints to apply when selecting candidates


Here is an example configuration file :

```XML
<search-backend>
  <name>A DBPedia search backend for populated places</name>
  <end-point>
    <url>http://dbpedia.org/sparql</url>
  </end-point>
  <search>
    <search-predicate>
      <uri>http://xmlns.com/foaf/0.1/name</uri>
      <weight>25</weight>
      <language>en</language>
    </search-predicate>
  </search>
  <popularity>
    <measure>
      <predicate>http://dbpedia.org/ontology/populationTotal</predicate>
    </measure>
  </popularity>
  <type-constraint>
    <type>http://dbpedia.org/ontology/PopulatedPlace</type>
  </type-constraint>
</search-backend>
```

The `<name/>` node gives the name that will be displayed for the configuration. The `<endpoint><url/></end-point>` node provides the URL used as a SPARQL end point.

Other parts of the configuration is treated in the relevant following parts.

## Search

This part describes the way we select candidates among the dataset.
This is done without taking care of sorting the results

### Search predicates

Datasets usually come with a lot of predicates with string literal objects that are not relevant for Named Entity Disambiguation. Looking for those might introduce :

 - Higher search time
 - Introduction of wrong match that would need to be filtered afterwards

Therefore the approach here is to provide a set of predicate that are relevant for NED through the configuration.

Each predicate to use should come as the `<uri/>` of a `<search-predicate/>` node as shown here :

```XML
  <search>
    <search-predicate>
      <uri>http://yago-knowledge.org/resource/hasPreferredName</uri>
      <weight>50</weight>
    </search-predicate>
    <search-predicate>
      <uri>http://www.w3.org/2000/01/rdf-schema#label</uri>
      <weight>25</weight>
    </search-predicate>
    <search-predicate>
      <uri>http://yago-knowledge.org/resource/hasFamilyName</uri>
      <weight>25</weight>
    </search-predicate>
    <search-predicate>
      <uri>http://yago-knowledge.org/resource/hasGivenName</uri>
      <weight>5</weight>
    </search-predicate>
  </search>
```

The previous example shows a configuration used for Yago and select four predicates that are used to perform the search.

> **Note** : The weight parameter is used to denote the importance of a predicate and is describe is section *Scores*

The generated search query would have the following form :

```SPARQL
SELECT ?rdfslabel ?yagoname ... WHERE {
  { ?rdfslabel ... }
  UNION
  { ?yagoname ... }
  UNION ...
}
```

This way a list of entities is retrieved for every predicate that can be processed (for ranking/filtering) later. Every variable match with a predicate.

> **TODO** : measure performance of the UNION selection method with other candidates

> **TODO** : use every predicate when none is given. Current implementation won't give any result if no predicate is given).


### Search method

Even with the predicates provided, there still remains different ways to get a match.

For the example `"Casablanca"` we can distinct four literals that might be considered as a match :

 - `"Casablanca"` : an exact match
 - `"A night in Casablanca"` : the string is contained in the candidate
 - `"casablanca"` : the match is identical except for the case
 - `"A night in casablanca"` : a combination of the two above

An effective way to implement the two first exists and is already implemented as *exact match* and *contains match*.
The last two ones could be implemented using regular expression but are not integrated yet.

A different search method can be provided for every *search predicate*. The configuration is done by specifying a search method for every predicate. If none is specified, it will default to an *exact match*.


#### Exact match

The exact match provides a way to look for a string that match exactly the search term.

This configuration is activated by adding the following `<method>exact</method>` node to a *search-predicate* section :

```XML
<search-backend>
  ...
  <search>
    <search-predicate>
      <uri>http://xmlns.com/foaf/0.1/name</uri>
      <weight>25</weight>
      <method>exact</method>
      <language>en</language>
    </search-predicate>
    ...
  </search>
  ...
</search-backend>
```

The SPARQL schema is the following :

```SPARQL
?variable <search-predicate> "search-term"
```

Therefore a complete request with two search predicates looking for *Casablanca* would be :

```SPARQL
SELECT ?rdfslabel ?yagoname  WHERE {
  { ?rdfslabel rdfs:label "Casablanca" }
  UNION
  { ?yagoname yago:name "Casablanca" }
}
```

##### Language tags

Some datasets (such as DBPedia) use language tags in order to indicate to which language correspond a specific literal. 
The *exact match* presented aboveapproach wouldn't give any result on tagged literals.

Therefore, an optional [language tag](http://www.w3.org/TR/rdf-sparql-query/#matchLangTags) can be used to add a suffix to the search term. The configuration above would give the following request :

```SPARQL
?foafname foaf:name "search-term"@en
```

If no language is provided, no tag suffix is added to the request (matching only untagged literals !)




#### Contains match

The *contains match* aims at providing a way to make an extended search (i.e. if the word is present among others) while relying on full text search capabilities of RDF stores to make the query efficient.

The configuration to provide in order to get a *contains match* search is the following :

```XML
<search-backend>
  ...
  <search>
    <search-predicate>
      <uri>http://www.w3.org/2000/01/rdf-schema#label</uri>
      <weight>25</weight>
      <method>contains</method>
      <contains-uri>bif:contains</contains-uri>
    </search-predicate>
    ...
  </search>
</search-backend>
```

Note that both `<method/>` and `<contains-uri/>` are necessary to make it work. `<method/>` is simply a marker to specify which method to use. `<contains-uri/>` specifies which predicate to use for full text search.

The SPARQL schema is the following :

```SPARQL
?variable <search-predicate> ?text .
?text <contains-uri> "search-term" .
```

Let's say we are using `bif:contains` as a search predicate (which is the full-text search predicate for [Virtuoso] instances). Therefore a complete request with two search predicates looking for *Casablanca* would be :

```SPARQL
SELECT ?rdfslabel ?yagoname  WHERE {
  { ?rdfslabel rdfs:label ?text .
    ?text <bif:contains> "Casablanca" }
  UNION
  { ?yagoname yago:name  ?text .
    ?text <bif:contains> "Casablanca" }
}
```


> **Note** : an entity might appear several times in the same variable (DISTINCT is not used). This is useful for ranking (the more times a term appears, the more it is likely to be a match).

> **Note** : Performance might increase by moving `?text <bif:contains> "Casablanca"` to the root of the search.

> **Note** : Virtuoso's `bif:contains` doesn't support spaces in search term. ("New York" would need to be : `'"New" AND "York"'`)



## Type constraint

Type constraint is used to restrict the results to entities of a particular type.

This can especially be useful to eliminate all non named entities from a result (i.e. classes, predicates, ...).

Configuration example : 

```XML
<search-backend>
  ...
  <type-constraint>
    <type>http://dbpedia.org/ontology/PopulatedPlace</type>
  </type-constraint>
</search-backend>
```

The generated request will have the following form :

```SPARQL
SELECT  ?httpxmlnscomfoaf01name
WHERE
  { { ?httpxmlnscomfoaf01name foaf:name: ?containsText .
      ?containsText <bif:contains> "Toulouse" .
      ?httpxmlnscomfoaf01name rdf:type yago:PopulatedPlace
    }
  }
```

Right now, every type in the configuration is a requirement for the entity. In the future, plan would be to provide a more flexible schema :

```XML
<type-constraint>
    <type>Person</type>
</type-constraint>
<type-constraint>
    <type>Singer</type>
    <type>Guitarist</type>
</type-constraint>
```

Would yield results where each entity is a `(Person AND (Singer OR Guitarist))`

This feature would be useful in order to add a type constraint to an existing configuration (for example while doing a request on a web service).



## Scores

Score aims to provide way to sort results according their relevance.
Here relevance is not deterministic because a word - away from his context - might refer to several entities.

Therefore, the score should link to the probability of a word pointing to a specific entity.

Some methods have been described in the literature [ references ] on that subject but they are all dependant on a specific dataset. For example [DBPedia Spotlight] measures the number of occurrences of an anchor text to refer to an entity. While this methods provides good results, it relies on metadata that is usually excluded from dataset.

In this project we focus on two measures that are easily adaptable on a large number of datasets.

### Match score

A dataset usually contains various "text" predicates. Among them some are more relevant to a search that aims at selecting a candidate.

For example a match in a `dbpedia:abstract` is far less interesting than a match on a `foaf:name` property.

Distinction is done by attributing a weight to each *search predicate*. Whenever a match is found for an entity, the weight of the predicate is added to the *match-score* of the entity.

```XML
  <search>
    ...
    <search-predicate 
      uri="http://yago-knowledge.org/resource/hasPreferredName" 
      weight="50"/>
    <search-predicate 
      uri="http://www.w3.org/2000/01/rdf-schema#label" 
      weight="25"/>
  </search>
```

This way when doing a search for `"Paris"` :

 - an entity `e1` with `e1 yago:hasPreferredName "Paris"` and `e1 rdfs:label "Paris"` would have a score of 75
 -  an entity `e2` with `e2 rdfs:label "Paris"` would have a score of 25

> **Note** : right now the weight is added multiple times if there is multiple matches for a same predicate.

### Popularity score

The popularity score provides a way to measure how likely an entity his to be referenced. 

The measurement will be different for each type of entity and the predicate is likely to change for every dataset. An example of this is the *DBPedia cities* backend where a the popularity measure uses the population of the place :

```XML
<search-backend>
  ...
  <popularity>
    <measure>
      <predicate>http://dbpedia.org/ontology/populationTotal</predicate>
    </measure>
  </popularity>
  ...
</search-backend>
```

Popularity measurement is done in a second request of the following form :

```SPARQL
SELECT  * WHERE   { 
OPTIONAL { yago:Stephen_Toulouse yago:hasNumberOfWikipediaLinks ?0 } 
OPTIONAL { yago:Toulouse yago:hasNumberOfWikipediaLinks ?1 }
OPTIONAL { yago:Toulouse_Olympique yago:hasNumberOfWikipediaLinks ?2 }
}
```

As it can be seen in this example, we retrieve the value (which is expected to be an `xsd:int` or an `xsd:float`) of each entity. The variable (`?0`, `?1`, ...) is a number corresponding to the index of the entity in the search results.

The `OPTIONAL` keyword is here to make sure that the query doesn't fail if a popularity is missing.

A missing popularity is simply set to `0`, assuming that an entity that has no measured popularity is probably not that popular. A drawback of that is for entities which URIs are not conform (i.e. containing quotes or accents). The backend won't be able to formulate a query for those entities and they will end up with a null popularity.

> **Note** : In Yago search backend, the number of incoming Wikipedia links is used as a generic popularity measurement. This idea might also be applied inside the RDF graph itself on some dataset.


### Combining scores

Right now, the global score is calculated by simply multiplying the *match* and the *popularity* scores.

What is needed for the (future) global score :

 - normalization : every score should fit between 0 an 1
 - the popularity should not be linear : the "popularity" gap between a 50000 and a 100000 inhabitants is more important than between 1M and 2M. A log(popularity) seems more adapted.
 - there should not be a situation where every popularity is equal to `0` (bringing all scores to `0` too).





# Web Services

The Web Service is right now mostly a proof of concept that replies an XML file to a search term.

It uses JAX-RS (with the [Jersey] implementation) on top of [Jetty].

More details when more work achieved =)


# References

Still to integrate




[Jena]: http://jena.apache.org/
[TDB]: http://jena.apache.org/documentation/tdb/index.html
[Fuseki]: http://jena.apache.org/documentation/serving_data/

[Virtuoso]: http://virtuoso.openlinksw.com/

[Yago]: http://www.mpi-inf.mpg.de/yago-naga/yago/index.html
[DBPedia Spotlight]: http://spotlight.dbpedia.org/

[Scala]: http://www.scala-lang.org/
[Logula]: https://github.com/codahale/logula

[Jersey]: http://jersey.java.net/
[Jetty]: http://jetty.codehaus.org/jetty/