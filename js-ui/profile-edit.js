jQuery(function($){

  var MyModel = Backbone.Model.extend({

    /** If an object in the attributes has a toJSON() method, call it.
     * This is only done on direct childs for performance reasons 
     */
    toFullJSON: function(){
      var attrs = _.clone(this.attributes)
      for (var key in attrs) {
        if (attrs.hasOwnProperty(key)){
          if(attrs[key].toFullJSON) {
            attrs[key] = attrs[key].toFullJSON()
          } else if(attrs[key].toJSON) {
            attrs[key] = attrs[key].toJSON()
          }
        }
      }
      return attrs
    },

    clear: function(){
      var attrs = this.attributes
      for(var key in attrs) {
        if(attrs[key] instanceof MyModel || attrs[key] instanceof MyCollection)
          attrs[key].clear()
      }
      this.destroy()
    },
    
  })

  var MyCollection = Backbone.Collection.extend({
    clear: function(){
      while(this.length > 0) {
        this.pop().clear()
      }
    }
  })




  /*****************    Profile Description  ***********************/


  window.ProfileDescriptionModel = MyModel.extend({
    defaults: {
      name: "Default name",
      endPoint: {url: ""},
    },

    initialize: function(){
    }
  })

  window.ProfileDescriptionView = Backbone.View.extend({
    el: $("#prof-desc-tab"),

    template: Handlebars.compile($("#profileDescriptionTpl").html()),

    events: {
      "change input": "refreshModel",
    },

    initialize: function() {
      _.bindAll(this, 'render');
      this.model.bind('change', this.render);

      this.render()
    },

    render: function(){
      $(this.el).html(this.template(this.model.toFullJSON()))
    },

    refreshModel: function(){
      var newName = $(this.el).find(("#pName")).val()
      
      var newURL = $(this.el).find(("#pEndPoint")).val()
      var newEndPoint = this.model.get("endPoint")
      newEndPoint.url = newURL

      this.model.set({name: newName})
      this.model.set("endPoint", newEndPoint)
    }

  })







  /***************************  Profile Edit   ******************************/

  window.ProfileEditModel = MyModel.extend({

    xmlTemplate: Handlebars.compile($("#profileXMLTpl").html()),

    initialize: function(){

    },

    newSearchPredicate: function(){
      var spm = new SearchPredicateModel()
      this.get("searchPredicates").add(spm)
      var spv = new SearchPredicateView({model: spm})
    },


    toXML: function(){
      return this.xmlTemplate(this.toFullJSON())
    },

    submit: function(){
      $.ajax({
        url: Cas.Server.profilesURL() + "add",
        type: "POST",
        data: this.toXML(),
        contentType: "text/xml",
        dataType: "text xml",
        success: function(res){
          console.log("submit success", res)
        },
        error : function (xhr, ajaxOptions, thrownError){  
          console.log(xhr.status); 
          console.log(ajaxOptions)         
          console.log(thrownError);
      },
      })
    }
  })

  window.ProfileEditView = Backbone.View.extend({
    el: $("#profile-edit"),

    events: {
      "click #show-profile": "log",
      "click #submit-profile": "submitProfile",
      "click #new-search-predicate": "newSearchPredicate",
    },

    log: function(){
      console.log("log xml", this.model.toXML())
    },

    submitProfile: function(){
      this.model.submit()
    },

    newSearchPredicate: function(){
      this.model.newSearchPredicate()
    }
  })




  /****************************** Search Predicate  ******************************/

  window.SearchPredicateModel = MyModel.extend({
    defaults: {
      uri: "",
      method: "exact",
    },
  })

  window.SearchPredicateView = Backbone.View.extend({
    tagName: "div",
    id: "search-predicate",

    template: Handlebars.compile($("#searchPredicateTpl").html()),

    events: {
      "click #delete-search-predicate": "destroy",
      "change input": "refreshModel",
      "change select": "refreshModel",
    },

    initialize: function(){
      _.bindAll(this, 'remove', 'render')
      this.model.bind('destroy', this.remove)
      this.render()
    },

    render: function(){
      $(this.el).html(this.template(this.model.toJSON()))
      $("#search-predicates").append($(this.el))
    },

    refreshModel: function(){
      var elem = $(this.el)

      newPredicate = elem.find("#pPred").val()
      newWeight = elem.find("#pWeight").val()
      newMethod = elem.find("#pSelMeth").val()
      newLanguage = elem.find("#pLang").val()

      this.model.set("uri", newPredicate)
      this.model.set("method", newMethod)
      this.model.set("weight", newWeight)
      this.model.set("language", newLanguage)
    },

    remove: function(){
      $(this.el).remove()
    },

    destroy: function(){
      this.model.destroy()
    },
  })

  window.SearchPredicateColl = MyCollection.extend({
    model: SearchPredicateModel,
  })





  /************************** Factory *******************************/


  window.Factory = {
    profileEditFromXML: function(xml){
      return Factory.profileEditFromJSON(Factory.profileXmlToJson(xml))
    },

    profileEditFromJSON: function(json){
      var profileEdit = new ProfileEditModel()

      var description = new ProfileDescriptionModel(json.description)
      new ProfileDescriptionView({model: description})
      profileEdit.set("description", description)

      
      var searchPredicates = new SearchPredicateColl()
      $.each(json.searchPredicates, function(index, elem){
        var searchPredicate = new SearchPredicateModel(elem)
        new SearchPredicateView({model: searchPredicate})
        searchPredicates.add(searchPredicate)
      })
      profileEdit.set("searchPredicates", searchPredicates)
      new ProfileEditView({model: profileEdit})

      return profileEdit
    },

    profileXmlToJson: function(xml){
      var xml = $(xml)
      var json = {
        description: {endPoint: {}},
        searchPredicates: [],
        popularities: {},
        constraints: {},
        properties: {}
      }
      json.description.name = xml.find("name").text()
      json.description.endPoint.url = xml.find("end-point").find("url").text()

      xml.find("search-predicate").each(function(index, elem){
        var pred = json.searchPredicates[index] = {}
        pred.uri = $(elem).find("uri").text()
        pred.method = $(elem).find("method").text()
        pred.weight = $(elem).find("weight").text()
        pred.language = $(elem).find("language").text()
      })

      xml.find("properties").children().each(function(index, elem){
        var prop = json.properties[index] = {}
        console.log(elem)
        console.log(elem.nodeName)
        if(elem.nodeName == "light") {
          prop.light = true
          prop.label = $(elem).find("label").text()
          prop.predicate = $(elem).find("predicate").text()
        }
        else if(elem.nodeName == "full") {
          prop.light = false
          prop.label = $(elem).find("label").text()
          prop.treatment = $(elem).find("treatment").text()
        }
        console.log(prop)
      })

      return json
    }
  }




  


  
  var profile = {
    name: 'DBPedia sqd',
    endPoint: {
      url: "http://localhost:9998/casablanca"
    },
    searchPredicates: [{
      uri: "rdfs:label",
      weight: 25,
      method: "Custom",
      language: "fr"
    },{
      uri: "foaf:name"
    }]
  }



  /********************* Template Helpers ********************************/

  $(Handlebars.registerHelper('equal', function(obj1, obj2, options){
    if(obj1 == obj2)
      return options.fn(this);
  }))

})