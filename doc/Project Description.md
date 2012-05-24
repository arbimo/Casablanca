

# Project description

This project is part of a Scientific Initiation conducted at the Universidade Federal do Rio de Janeiro.
It aims to provide tools to make Named Entities Disambiguation (NED) easier.


# Current state

# Conception overview

# Dataset

An overview of the datasets that were reviewed in order to choose one
adapted to this project is availbable in the PDF file [ ... ].

## Main dataset : [Yago]

The dataset YAGO2 has been choosen for the following reasons :

 - it has a clean schema with a limited number of properties
 - it is centered on words and therefore provides several interesting
predicates for NED


Yago2 comes in two versions, both available at
[Yago's downloads](http://www.mpi-inf.mpg.de/yago-naga/yago/downloads.html) :

 - the core version that provides all entities extracted from Wikipedia, types
(obtained by merging Wikipedia categories and WordNet definitions) and several
text predicates (names, label, ...)
 - the full version that comes with additional information, including wikipedia
links and context

## Deployment 

The publicly available version of Yago2[^public-yago] is outdated and it was therefore
necessary to deploy a local version of the dataset.

I choosed [Fuseki] as an easy to deploy SPARQL end point. It provides support
for SPARQL 1.1 and Yago core provides a ready to use TDB format for Yago core

> Fuseki is an Apache incubator project part of [Jena]. His store is based on the
Jena TDB format.

The main problem with Fuseki/TDB is that the time needed to had triples grows with the size of the dataset. Therefore it was nearly impossible to export the whole Yago dataset (operation would have required weeks on a classic computer). 
The solution was to base our self on the Yago core dataset (which is avalable in the TDB format) and extend it with the predicates we are interested in. More information on this operation will be available in the dataset-creation document soon.

[^public-yago]: <http://lod.openlinksw.com/sparql>

## Other dataset

While Yago is the main dataset that we plan to use for this project development. A goal is to stay compatible with other dataset.

Therefore, every dataset specific data should be stored in an external configuration file.


#  Search Service

Implementation is based on [Scala] to be able to use well tested java libraries and frameworks while using a high-level language.

The search service provides mainly a SearchBackend class and a factory to create from a configuration file.

## Configuration 

Configuration comes as an XML file specifying :
 - the SPARQL end point to use
 - how candidates are selected
 - how canditates are ranked
 - (will) which type constraint to apply when selecting candidates


Here is an example configuration file :

```XML
<search-backend>
  <name>Local Fuseki End Point for Yago2</name>
  <end-point>
    <url>http://localhost:3030/Yago/query</url>
  </end-point>
  <search>
    <match>
      <type>exact</type>
    </match>
    <search-predicate uri="http://yago-knowledge.org/resource/hasPreferredName" weight="50"/>
    <search-predicate uri="http://www.w3.org/2000/01/rdf-schema#label" weight="25"/>
    <search-predicate uri="http://yago-knowledge.org/resource/hasFamilyName" weight="25"/>
    <search-predicate uri="http://yago-knowledge.org/resource/hasGivenName" weight="5"/>
  </search>
  <popularity>
    <measure>
      <predicate>http://yago-knowledge.org/resource/hasNumberOfWikipediaLinks</predicate>
      <max>1000</max>
    </measure>
  </popularity>
</search-backend>
```

The `<name/>` node gives the name that will be displayed for the configuration. The `<endpoint><url/></end-point>` node provider the URL used as a SPARQL end point.

Other parts of the configuration is treated in the relevant following parts.

## Search

This part describes the way we select candidates among the dataset.
This is done without taking care of sorting the results

### Search predicates

Datasets usualy come with a lot of predicates with string literal objects that are not relevant for Named Entity Disambiguation. Those might introduce :
 - Higher search time
 - Introduction of wrong match that would need to filter afterwards

Therefore the aproach here is to provide a set of predicate that are relevant for NED through the configuration file.

Each predicate to use should come as the `<uri/>` of a `<search-predicate/>` node as shown her :

```XML
  <search>
    ...
    <search-predicate uri="http://yago-knowledge.org/resource/hasPreferredName" weight="50"/>
    <search-predicate uri="http://www.w3.org/2000/01/rdf-schema#label" weight="25"/>
    <search-predicate uri="http://yago-knowledge.org/resource/hasFamilyName" weight="25"/>
    <search-predicate uri="http://yago-knowledge.org/resource/hasGivenName" weight="5"/>
  </search>
```

The previous example shows a configuration used for Yago and select four predicates that are used to perform the search.

> The weight parameter is used to denote the importance of a predicate and is describe is section *Scores*

The search query would have the following form :

```SPARQL
SELECT ?rdfslabel ?yagoname ... WHERE {
  { ?rdfslabel ... }
  UNION
  { ?yagoname ... }
  UNION ...
}
```

This way a list of entities is retrieved for every predicate that can be processed (for ranking/filtering) later. Every variable match with a predicate.

> TODO : measure performance of the UNION selection method with other candidates

> TODO : use every predicate when none is given


### Search method

With the predicates provided, there still remains different ways to get a match.

For the example `"Casablanca"` we can distinct four literals that might be considered as a match :

 - `"Casablanca"` : an exact match
 - `"A night in Casablanca"` : the string is contained in the candidate
 - `"casablanca"` : the match is identical except for the case
 - a combination of the two above

An effective way to implement the two first exist and is already implemented as *exact match* and *contains match*.
The last two ones could be implemented using regular expression but are integrated yet.

Every configuration currently applies on every search predicates. 

> Idea : Work might be done to make it predicate-specific



#### Exact match

The exact match provides a way to look for string that match exactly the search term.

This configuration is activated by adding the following `<match/>` node to the *search* section :

```XML
<search-backend>
  ...
  <search>
    <match>
      <type>exact</type>
    </match>
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

Note
 ~    an entity might appear several times in the same variable (DISTINCT is not used). This is usefull for ranking (the more times a term appears, the more it is likely to be a match.

> TODO : make it the default method

#### Contains match

The *contains match* aims at providing a way to make an extended search (i.e. if the word is present among others) while relying on full text search capabilities of RDF stores to make the query efficient.

```XML
<search-backend>
  ...
  <search>
    <match>
      <type>contains</type>
      <contains-uri>bif:contains</contains-uri>
    </match>
    ...
  </search>
</search-backend>
```

## Scores

### Match score

### Popularity score

## Type constraint

Not implemented yet





# Web Services



[Fuseki]: http://jena.apache.org/documentation/serving_data/
[Jena]: http://jena.apache.org/
[Yago]: http://www.mpi-inf.mpg.de/yago-naga/yago/index.html
[Scala]: http://www.scala-lang.org/
