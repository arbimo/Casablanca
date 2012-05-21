The intented query should look like :

```SPARQL
SELECT * WHERE { 
{ ?s1 <http://yago-knowledge.org/resource/hasPreferredName> "Casablanca" . } 
UNION 
{ ?s3  <http://www.w3.org/2000/01/rdf-schema#label> "Casablanca" . } 
UNION 
{ ?s4  <http://yago-knowledge.org/resource/hasFamilyName> "Casablanca" . } 
UNION 
{ ?s2 <http://yago-knowledge.org/resource/hasGivenName> "Casablanca" . }

 } LIMIT 100
```

Example config file :

```
%%Config 
name := "My example end point"

%%EndPoint
url := <http://localhost:3030/Yago/query>
 
%%Predicates
<http://yago-knowledge.org/resource/hasPreferredName>  50
<http://www.w3.org/2000/01/rdf-schema#label>	25
<http://yago-knowledge.org/resource/hasFamilyName>	25
<http://yago-knowledge.org/resource/hasGivenName>	5
```

Grammar :
```
Config ::= EndPoint Predicates
EndPoint ::= "%%EndPoint" EndPointBody 
EndPointBody ::= "url ":=" URL

Predicates ::= "%%Predicate" PredicateBody
PredicateBody ::= URI num PredicateBody
```