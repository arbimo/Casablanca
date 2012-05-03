package br.ufrj.ner;


case class EndPoint(name : String, url : String)


object DBPedia extends EndPoint("DBPedia", "http://localhost:3030/Yago/query")