
 
 
## Predicates

Every one of this predicate has :
 * a string literal range
 * a domain including named entities
 * is used in Yago2Core

```
<http://yago-knowledge.org/resource/hasPreferredName>
<http://yago-knowledge.org/resource/hasPreferredMeaning> ??
<http://www.w3.org/2000/01/rdf-schema#label>
<http://yago-knowledge.org/resource/hasFamilyName>
<http://yago-knowledge.org/resource/hasGivenName>
```


### List of predicates with a yagoWord range.

```
SELECT * WHERE { 
?p <http://www.w3.org/2000/01/rdf-schema#range> <http://yago-knowledge.org/resource/yagoWord> } 
```

```
<http://yago-knowledge.org/resource/hasFamilyName>
<http://yago-knowledge.org/resource/isCalled>
<http://yago-knowledge.org/resource/hasGivenName>
```

### List of predicates with a yagoString range. 

```
SELECT * WHERE { 
?p <http://www.w3.org/2000/01/rdf-schema#range> <http://yago-knowledge.org/resource/yagoString> } 
```

```
<http://yago-knowledge.org/resource/hasContextPrecedingAnchor>
<http://yago-knowledge.org/resource/hasWikipediaArticleText>
<http://yago-knowledge.org/resource/hasWikipediaAbstract>
<http://yago-knowledge.org/resource/hasPreferredName>
<http://yago-knowledge.org/resource/hasWikipediaAnchorText>
<http://yago-knowledge.org/resource/hasThreeLetterLanguageCode>
<http://yago-knowledge.org/resource/hasContextSucceedingAnchor>
<http://yago-knowledge.org/resource/hasCitationTitle>
<http://yago-knowledge.org/resource/hasMotto>
<http://yago-knowledge.org/resource/hasAnchorText>
<http://yago-knowledge.org/resource/_witness>
<http://yago-knowledge.org/resource/hasGloss>
<http://yago-knowledge.org/resource/hasTitleText>
<http://yago-knowledge.org/resource/hasWikipediaCategory>
<http://yago-knowledge.org/resource/hasContext>
<http://yago-knowledge.org/resource/_hasComment>
<http://yago-knowledge.org/resource/_hasTypeCheckPattern>
<http://yago-knowledge.org/resource/_wikiSplit>
<http://yago-knowledge.org/resource/_wikiReplace>
<http://yago-knowledge.org/resource/_wikiKeep>
<http://yago-knowledge.org/resource/_wikiBrackets>
<http://yago-knowledge.org/resource/hasLanguagecode>
```



### List of Yago2 predicates :
```
<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
<http://yago-knowledge.org/resource/wasBornIn>
<http://yago-knowledge.org/resource/hasGender>
<http://yago-knowledge.org/resource/hasFamilyName>
<http://yago-knowledge.org/resource/isMarriedTo>
<http://yago-knowledge.org/resource/hasPreferredName>
<http://yago-knowledge.org/resource/actedIn>
<http://yago-knowledge.org/resource/wroteMusicFor>
<http://yago-knowledge.org/resource/hasWebsite>
<http://yago-knowledge.org/resource/hasGivenName>
<http://yago-knowledge.org/resource/hasWikipediaUrl>
<http://yago-knowledge.org/resource/influences>
<http://yago-knowledge.org/resource/hasMusicalRole>
<http://yago-knowledge.org/resource/hasPreferredMeaning>
<http://yago-knowledge.org/resource/created>
<http://yago-knowledge.org/resource/hasWonPrize>
<http://yago-knowledge.org/resource/directed>
<http://yago-knowledge.org/resource/diedOnDate>
<http://yago-knowledge.org/resource/wasBornOnDate>
<http://www.w3.org/2000/01/rdf-schema#label>
<http://www.w3.org/2000/01/rdf-schema#subClassOf>
<http://yago-knowledge.org/resource/hasGloss>
<http://www.w3.org/2000/01/rdf-schema#domain>
<http://www.w3.org/2000/01/rdf-schema#range>
<http://www.w3.org/2000/01/rdf-schema#subPropertyOf>
<http://yago-knowledge.org/resource/isLocatedIn>
<http://yago-knowledge.org/resource/hasLongitude>
<http://yago-knowledge.org/resource/hasLatitude>
<http://yago-knowledge.org/resource/hasGeonamesId>
<http://yago-knowledge.org/resource/hasGeoCoordinates>
<http://yago-knowledge.org/resource/produced>
<http://yago-knowledge.org/resource/hasSynsetId>
<http://yago-knowledge.org/resource/hasArea>
<http://yago-knowledge.org/resource/hasPopulation>
<http://yago-knowledge.org/resource/hasMotto>
<http://yago-knowledge.org/resource/owns>
<http://yago-knowledge.org/resource/hasLength>
<http://yago-knowledge.org/resource/wasCreatedOnDate>
<http://yago-knowledge.org/resource/hasCapital>
<http://yago-knowledge.org/resource/hasDuration>
<http://yago-knowledge.org/resource/isKnownFor>
<http://yago-knowledge.org/resource/hasAcademicAdvisor>
<http://yago-knowledge.org/resource/graduatedFrom>
<http://yago-knowledge.org/resource/worksAt>
<http://yago-knowledge.org/resource/diedIn>
<http://yago-knowledge.org/resource/hasChild>
<http://yago-knowledge.org/resource/isAffiliatedTo>
<http://yago-knowledge.org/resource/participatedIn>
<http://yago-knowledge.org/resource/hasHeight>
<http://yago-knowledge.org/resource/hasWeight>
<http://yago-knowledge.org/resource/livesIn>
<http://yago-knowledge.org/resource/hasThreeLetterLanguageCode>
<http://yago-knowledge.org/resource/imports>
<http://yago-knowledge.org/resource/hasNeighbor>
<http://yago-knowledge.org/resource/hasHDI>
<http://yago-knowledge.org/resource/hasInflation>
<http://yago-knowledge.org/resource/hasImport>
<http://yago-knowledge.org/resource/hasUTCOffset>
<http://yago-knowledge.org/resource/hasEconomicGrowth>
<http://yago-knowledge.org/resource/hasExpenses>
<http://yago-knowledge.org/resource/hasTLD>
<http://yago-knowledge.org/resource/exports>
<http://yago-knowledge.org/resource/hasExport>
<http://yago-knowledge.org/resource/hasRevenue>
<http://yago-knowledge.org/resource/hasGini>
<http://yago-knowledge.org/resource/hasUnemployment>
<http://yago-knowledge.org/resource/hasOfficialLanguage>
<http://yago-knowledge.org/resource/hasPages>
<http://yago-knowledge.org/resource/hasISBN>
<http://yago-knowledge.org/resource/hasCurrency>
<http://yago-knowledge.org/resource/hasPoverty>
<http://yago-knowledge.org/resource/dealsWith>
<http://yago-knowledge.org/resource/hasGDP>
<http://yago-knowledge.org/resource/isInterestedIn>
<http://yago-knowledge.org/resource/startedOnDate>
<http://yago-knowledge.org/resource/happenedIn>
<http://yago-knowledge.org/resource/endedOnDate>
<http://yago-knowledge.org/resource/hasLanguageCode>
<http://yago-knowledge.org/resource/hasNumberOfPeople>
<http://yago-knowledge.org/resource/hasICD10>
<http://yago-knowledge.org/resource/isCitizenOf>
<http://yago-knowledge.org/resource/wasDestroyedOnDate>
<http://yago-knowledge.org/resource/edited>
<http://yago-knowledge.org/resource/hasGenre>
<http://yago-knowledge.org/resource/hasBudget>
<http://yago-knowledge.org/resource/isPoliticianOf>
<http://yago-knowledge.org/resource/holdsPoliticalPosition>
<http://yago-knowledge.org/resource/isLeaderOf>
<http://yago-knowledge.org/resource/happenedOnDate>
<http://yago-knowledge.org/resource/hasImdb>
<http://yago-knowledge.org/resource/playsFor>
<http://yago-knowledge.org/resource/hasPopulationDensity>
```