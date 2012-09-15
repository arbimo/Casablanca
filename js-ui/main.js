jQuery(function($){

	if(!window.Cas) window.Cas ={}



	/************************* Server ***********************/

	window.ServerModel = Backbone.Model.extend({
		defaults: {
			casablancaServer: "http://localhost:9998/casablanca",
		},

		setServer: function(url){
      if(url[url.length-1] === '/')
        this.set("casablancaServer", url)
      else 
        this.set("casablancaServer", url + '/')
    },

    profilesURL: function(){
      return this.get("casablancaServer") + "profiles/"
    },

    searchURL: function(searchTerm){
      return this.get("casablancaServer") + "search/" + searchTerm + "?profile=" + Cas.Profiles.get("current")
    },

	})

	window.ServerView = Backbone.View.extend({
		el: $("body"),

		events: {
			"click #server-url-but": "updateServer"
		},

		initialize: function(){
			this.render()
		},

		render: function(){
			$("#server-url-text").val(this.model.get("casablancaServer"))
		},

		updateServer: function(){
			this.model.setServer($("#server-url-text").val())
		}
	})

	window.Cas.Server = new ServerModel()
	var mv = new ServerView({model: window.Cas.Server})




	/************************* Profiles ***********************/

	window.ProfilesModel = Backbone.Model.extend({
		defaults: {
			profiles: [],
			current: undefined,
		},

		initialize: function(){
			Cas.Server.bind('change', this.retrieveFromServer)
		},

		setProfiles: function(rawProfiles){

      this.set("profiles", rawProfiles)
      location.hash = "profiles"
    },

    setCurrentProfile: function(id){
    	this.set("current", id)
    	location.hash = "search"
    },

    retrieveFromServer: function(){
			var profilesModel = this
			$.ajax({
        url: Cas.Server.profilesURL(),
        success: function(json){
        	Cas.Profiles.setProfiles(json.profiles.profile)
        },
 //     error: Cas.Error.cantRetrieveProfiles,
        dataType: "json"
      })
		},
	})

	window.ProfilesView = Backbone.View.extend({
		el: $("#profiles"),

		template: Handlebars.compile($("#profilesTableTpl").html()),

		events: {
			"click .profile-details-btn": "showDetails",
			"click .profile-choice-btn": "chooseProfile",
			"click #reload-profiles": "reload",
		},

		initialize: function(){
			_.bindAll(this, "render")

			this.model.bind('change', this.render)
			this.render()
		},

		render: function(){
			$("#profiles-div").html(this.template(this.model.toJSON()))
		},

		reload: function(){
			this.model.retrieveFromServer()
		},
		showDetails: function(event){
			var id = $(event.currentTarget).attr("value")
			location.hash = "profiles/edit/"+id
		},
		
		chooseProfile: function(event){
			this.model.set("current", $(event.currentTarget).attr("value"))
			location.hash = "search"
		},


	})

	window.Cas.Profiles = new ProfilesModel()
	var pv = new ProfilesView({model: window.Cas.Profiles})



	/****************** Search *******************************/

	window.SearchModel = Backbone.Model.extend({

		initialize: function(){
			_.bindAll(this, "search", "receiveResults")
		},

		search: function(searchTerm){
			this.set("currentSearch", searchTerm)
			$.getJSON(Cas.Server.searchURL(searchTerm), this.receiveResults)
		},

		receiveResults: function(data){
			if(data.search.term == this.get("currentSearch")) {
          $.each(data.search["search-result"], function(index, value){
            value.id = index
          })
          this.set("results", data.search)
        } else {
          console.log("Received search result ("+data.search.term+
            ") doesn't match with the current search ("+model.currentSearch+")")
        }
		},

		getResultByID: function(id){
			return this.get("results")["search-result"][id]
		},

	})

	window.SearchView = Backbone.View.extend({
		el: $("#search-view"),

		initialize: function(){
			_.bindAll(this, "render", "search")
			this.model.bind("change", this.render)
		},

		template: Handlebars.compile($("#searchResultsTpl").html()),

		addInfoTemplate: Handlebars.compile($("#candidateDetailsTpl").html()),

		events: {
			"click #search-but": "search",
		},

		render: function(){
			var self = this
			if(this.model.get("results")){
				$("#search-results-div").html(this.template(this.model.get("results")))
				$(".candidate-info").each(function(){
          var elem = $(this)
          elem.popover({
            title: "Additional information",
            content: self.getAddInfo(elem.attr("id")),
            offset: 10,
            html: true,
            placement: "left"
          })
        })
			}
		},

		getAddInfo: function(id){
			return this.addInfoTemplate(this.model.getResultByID(id))
		},

		search: function(){
			this.model.search($("#search-text").val())
		},
	})

	window.Cas.Search = new SearchModel()
	new SearchView({model: Cas.Search})



	/****************** Controller **************************/

	var PageController = Backbone.Router.extend({
		routes: {
			"": "home",
			"server-choice": "serverChoice",
			"profiles": "profiles",
			"profiles/edit/:id": "editProfile",
			"search":  "search",
		},

		initialize: function(){
			location.hash = ""
			Backbone.history.start()
		},

		selectView: function(id){
			$(".active-view").removeClass("active-view");
      $("#"+id).addClass("active-view");
		},

		home: function(){
			this.selectView("home")
		},

		serverChoice: function(){
			this.selectView("server-choice")
		},

		profiles: function(){
			this.selectView("profiles")
		},

		editProfile: function(id){
			$.ajax({
        url: Cas.Server.profilesURL() + id,
        success: function(xml){
        	if(Cas.ProfileEdit)
        		Cas.ProfileEdit.clear()
        	Cas.ProfileEdit = Factory.profileEditFromXML(xml)
        	Cas.Controller.selectView("profile-edit")
        },
 //     error: Cas.Error.cantRetrieveProfiles,
        dataType: "xml"
      })
		},

		search: function(){
			this.selectView("search-view")
		}
	})

	window.Cas.Controller = new PageController()









})