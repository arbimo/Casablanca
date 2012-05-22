

# Project description

This project is part of a Scientific Initiation conducted at the Universidade Federal do Rio de Janeiro.
It aims to provide tools to make Named Entities Disambiguation (NED) easier.


# Current state

## Conception overview

## Dataset

An overview of the datasets that were reviewed in order to choose one
adapted to this project is availbable in the PDF file [ ... ].

### Main dataset : Yago2

The dataset YAGO2 has been choosen for the following reasons :
 - it has a clean schema with a limited number of properties
 - it is centered on words and therefore provides several interesting
predicates for NED

Yago2 comes in two versions, both available
[here](http://www.mpi-inf.mpg.de/yago-naga/yago/downloads.html) :
 - the core version that provides all entities extracted from Wikipedia, types
(obtained by merging Wikipedia categories and WordNet definitions) and several
text predicates (names, label, ...)
 - the full version that comes with additional information, including wikipedia
links and context

### Deployment 

The publicly available version of Yago2 is outdated and it was therefore
necessary to deploy a local version of the dataset.

We choosed [Fuseki] as an easy to deploy SPARQL end point. It provides support
for SPARQL 1.1 and Yago core provides a ready to use TDB format for Yago core

> Fuseki is an Apache incubator project part of [Jena]. His store is based on
Jena TDB format


## Backend/Service

## Web Services

