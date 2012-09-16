jQuery(function($){

  var MyModel = Backbone.Model.extend({

    /** If an object in the attributes has a toJSON() method, call it.
     * This is only done on direct childs for performance reasons 
     */
    toJSON: function(){
      var attrs = _.clone(this.attributes)
      for (var key in attrs) {
        if (attrs.hasOwnProperty(key) && attrs[key]){
          if(attrs[key].toJSON) {
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
    },

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
      _.bindAll(this, "render", "remove", "refreshModel");
      this.model.bind("change", this.render);
      this.model.bind("destroy", this.remove)

      this.render()
    },

    render: function(){
      $(this.el).html(this.template(this.model.toJSON()))
    },

    remove: function(){
      this.undelegateEvents()
      $(this.el).empty()
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

    toXML: function(){
      return this.xmlTemplate(this.toJSON())
    },

    submit: function(){
      console.log("Submiting profile : ", this.toXML())
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
      "click #new-property": "newProperty"
    },

    initialize: function(){
      _.bindAll(this, "remove")
      this.model.bind("destroy", this.remove)
    },

    remove: function(){
      this.undelegateEvents()
      this.el = undefined
    },

    log: function(){
      console.log("log json", this.model.toJSON())
      console.log("log xml", this.model.toXML())
    },

    submitProfile: function(){
      this.model.submit()
    },

    newSearchPredicate: function(){
      var spm = new SearchPredicateModel()
      this.model.get("searchPredicates").add(spm)
      var spv = new SearchPredicateView({model: spm})
    },

    newProperty: function(){
      var pm = new PropertyModel()
      this.model.get("properties").add(pm)
      var pv = new PropertyView({model: pm})
    },
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
      this.undelegateEvents()
      $(this.el).remove()
    },

    destroy: function(){
      this.model.destroy()
    },
  })

  window.SearchPredicateColl = MyCollection.extend({
    model: SearchPredicateModel,
  })


/************************* Triples ********************************/

  window.TripleModel = MyModel.extend({
    defaults: {
      s: {
        type: "Subject",
      },
      p: {
        type: "Predicate",
        value: "rdfs:label"
      },
      o: {
        type: "Target"
      },
    },

    initialize: function(){
    },

    render: function(){
    }
  })

  window.TripleView = Backbone.View.extend({
    tagName: "div",
    id: "triple",

    template: Handlebars.compile($("#tripleTpl").html()),

    events: {
      "click button": "destroy",
      "change input": "parseDOM",
      "change select": "parseDOM"
    },

    initialize: function(){
      _.bindAll(this, "render", "attach", "remove", "destroy", "parseDOM")
      this.model.bind("destroy", this.remove)
      this.model.bind("change", this.render)
      this.render()
      this.attach()
      this.model.parent.bind("redrawn", this.attach)
    },

    attach: function(){
      this.parseDOM()
      $(".property[cid|="+this.model.parent.cid+"]").find("#triples").append($(this.el))
      this.render()
    },

    render: function(){
      $(this.el).html(this.template(this.model.toJSON()))
    },

    remove: function(){
      this.undelegateEvents()
      $(this.el).remove()
    },

    destroy: function(){
      this.model.destroy()
    },

    parseDOM: function(){
      var triple = {
        s: {
          type: $(this.el).find("#sType").val(),
          value: $(this.el).find("#sValue").val(),
        },
        p: {
          type: $(this.el).find("#pType").val(),
          value: $(this.el).find("#pValue").val(),
        },
        o: {
          type: $(this.el).find("#oType").val(),
          value: $(this.el).find("#oValue").val(),
        }
      }
      this.model.set(triple)
    }

  })

  window.TriplesColl = MyCollection.extend({
    model: TripleModel,
  })


  /****************************** Properties ***********************************/

  window.PropertyModel = MyModel.extend({
    defaults: {
      light: true,
      treatment: "No",
      triples: new TriplesColl()
    },

    initialize: function(){
      _.bindAll(this, "addTriple")
      this.set("triples", new TriplesColl())
    },

    addTriple: function(tripleModel){
      tripleModel.parent = this
      this.get("triples").add(tripleModel)
      this.trigger("change")
    },
  })

  window.PropertyView = Backbone.View.extend({
    tagName: "div",
    className: "property",

    template: Handlebars.compile($("#propertyTpl").html()),

    events: {
      "click #delete-property": "destroy",
      "click #add-triple": "addTriple",
      "change input": "parseDOM",
      "change select": "parseDOM",
    },

    initialize: function(){
      $(this.el).attr("cid", this.model.cid)
      _.bindAll(this, "parseDOM", "render", "destroy", "remove", "removeTriple", "addTriple")
      this.model.bind("change", this.render)
      this.model.bind("destroy", this.remove)
      this.render()
      $("#properties").append($(this.el))
    },

    parseDOM: function(){
      var light = $(this.el).find("#pConf").val() == "light"
      if(light) {
        var label = $(this.el).find("#pLabel").val()
        var predicate = $(this.el).find("#pPred").val()

        this.model.set({
          light: light,
          label: label,
          predicate: predicate,
        })
      }
      else {
        var label = $(this.el).find("#pLabel").val()
        var treatment = $(this.el).find("#pTreatment").val()
        this.model.set({
          light: light,
          label: label,
          treatment: treatment,
        })
      }
    },

    render: function(){
      $(this.el).find("#triples").detach()
      $(this.el).html(this.template(this.model.toJSON()))
      this.model.trigger("redrawn")
    },

    remove: function(){
      this.undelegateEvents()
      $(this.el).remove()
    },

    addTriple: function(){
      var tm = new TripleModel()
      tm.parent = this.model
      new TripleView({model: tm})
      this.model.addTriple(tm)
    },

    removeTriple: function(event){
      var id = $(event.currentTarget).attr("value")
      this.model.removeTriple(id)
    },

    destroy: function(){
      this.model.destroy()
    },

  })

  window.PropertiesColl = MyCollection.extend({
    model: PropertyModel,
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


      
      var searchPredicates = new SearchPredicateColl()
      $.each(json.searchPredicates, function(index, elem){
        var searchPredicate = new SearchPredicateModel(elem)
        new SearchPredicateView({model: searchPredicate})
        searchPredicates.add(searchPredicate)
      })

      

      var properties = new PropertiesColl()
      $.each(json.properties, function(index, elem) {
        var triples = elem.triples
        var prop = new PropertyModel(elem)
        new PropertyView({model: prop})
        properties.add(prop)
        if(triples)
          $.each(triples, function(index, elem){
            var triple = new TripleModel(elem)
            triple.parent = prop
            new TripleView({model: triple})
            prop.addTriple(triple)
          })

      })


      profileEdit.set({
        description: description,
        searchPredicates: searchPredicates,
        properties: properties,
      })

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
        if(elem.nodeName == "light") {
          prop.light = true
          prop.label = $(elem).find("label").text()
          prop.predicate = $(elem).find("predicate").text()
        }
        else if(elem.nodeName == "full") {
          prop.light = false
          prop.label = $(elem).find("label").text()
          prop.treatment = $(elem).find("treatment").text()

          prop.triples = []
          $(elem).find("triple").each(function(index, elem){
            var el = $(elem)
            var triple = {
              s: {
                type: el.find("s").attr("type"),
                value: el.find("s").text()
              },
              p: {
                type: el.find("p").attr("type"),
                value: el.find("p").text()
              },
              o: {
                type: el.find("o").attr("type"),
                value: el.find("o").text()
              }
            }
            prop.triples[index] = triple
          })
        }
      })

      return json
    }
  }







  /********************* Template Helpers ********************************/

  $(Handlebars.registerHelper('equal', function(obj1, obj2, options){
    if(obj1 == obj2)
      return options.fn(this);
  }))

  $(Handlebars.registerHelper('needsValue', function(obj1, options){
    if(obj1 == "Literal" || obj1 == "URI" || obj1 == "Variable")
      return options.fn(this);
  }))

})