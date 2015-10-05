var serverSettings = new Object();
serverSettings.serverURL = "https://127.0.0.1:8181/";

var speechSettings = new Object();
var activeIntevals = new Object();

//ensure that numResultsToReturn%numResultsPerPage == 0!!!
var numResultsPerPage = 5;
var numResultsToReturn = numResultsPerPage*5;

function toggleVisibility(newSection) {
    $(".section").not("#" + newSection).hide();
    $("#" + newSection).show();
    var inputText = $("#" + newSection).find("input:enabled:first");
    if(inputText){
    	inputText.focus();
    }
    hideSuggestions();
}

function toggleVisibilitySubsection(newSection) {
    $(".subSection").not("#" + newSection).hide();
    $("#" + newSection).show();
    var inputText = $("#" + newSection).find("input:enabled:first");
    if(inputText.length){
    	inputText.focus();
    }
}

function displayError(errorText) {
	if(errorText == ""){
		errorText = "Unknown error occured. Please check whether server is started.";
	}
    $(".errors").show();
    $(".errors").html(errorText);
}

function hideErrors(){
  $(".errors").hide();
}
 
// #####   Searching section   ###
var showSearchPageNumber = 1;
var totalSearchResult = 0;

function showPrevSearchPage(){
	if(showSearchPageNumber > 1){
		showSearchPageNumber--;
		showSearchPage();
	}
}

function showNextSearchPage(){
	if(showSearchPageNumber < Math.ceil(totalSearchResult/numResultsPerPage)){
		showSearchPageNumber++;
		showSearchPage();
	}
}

function jumpOnSearchPage(){
	var pageToShow = $("#searchPageNumber").val();
	if(pageToShow>=1 && pageToShow <= Math.ceil(totalSearchResult/numResultsPerPage)){
		showSearchPageNumber = pageToShow;
		showSearchPage();
	}
}

function showSearchPage(){
  if(totalSearchResult>0 && !$("#searchPage"+showSearchPageNumber).length){
    //initialize search
    startSearchFrom = Math.floor(((showSearchPageNumber*numResultsPerPage)-1)/numResultsToReturn)*numResultsToReturn;
    $("#searchQuery").val(searchQuery);
    
    manualSearch = false;
    $("#searchButton").click();
  } else {
  	$("#searchPageNumber").val(showSearchPageNumber);
    //hide all pages
    $(".searchPage:visible").hide();
    $("#searchPage"+showSearchPageNumber).show();
  }
}

var startSearchFrom = 0;
var searchQuery;
var manualSearch = true;

function displaySearchResults(result) {
  totalSearchResult = result.hits;
  var htmlToDisplay ='<div id="searchPages">';
  if(startSearchFrom == 0){
    htmlToDisplay+= '<div class="searchStats">';
    htmlToDisplay+="Hits: "+totalSearchResult;
    htmlToDisplay+=" Pages: "+Math.ceil(totalSearchResult/numResultsPerPage);
    htmlToDisplay+= " (took "+result.tookInMillis/1000+" seconds.) </div>";
  } else{
    htmlToDisplay +=  $("#searchPages").html();
  }
   
  var numResults = Math.min((totalSearchResult-startSearchFrom),numResultsToReturn);
  var startPage = false;
  var endPage = false;
  for(i=1; i<=numResults ; i++){
    if(i % numResultsPerPage ==1){
      startPage = true;
    }
    if(i % numResultsPerPage == 0){
      endPage = true;
    }
    
    var hitHtml = '';
    if(startPage){
      hitHtml += '<div class="searchPage" id="searchPage'+Math.ceil((startSearchFrom+i)/numResultsPerPage)+'">';
      startPage = false;
    }
    var hit = result["hit"+i];
    hitHtml += '<div class="searchHit">';
    hitHtml += '<h4><a class="searchTitle" title="' + hit.summary + '" onclick="	downloadFile(\''+hit.fileID+'\')">'+hit.documentTitle+'</a></h4>';
    hitHtml += '<div class="rating" id="searchRating' + hit.fileID + '" data-score="' + hit.rating +'"></div>';
    hitHtml += '<div class="searchHitScore"> Score:&nbsp;'+hit.score+'</div>';
    hitHtml += '<div class="searchHitHighlight">'+hit.highlight+'</div>';
    hitHtml+='</div><br/>';
    
    if(endPage || i==numResults){
      hitHtml += '</div>';
      endPage = false;
    }
    htmlToDisplay += hitHtml;
  }
  htmlToDisplay += '</div>'; // close section with stats and pages

  //generate pages list
  htmlToDisplay += '<div id="searchPageLinksSection" class="pageLinksSecion">';
  if(startSearchFrom == 0){
  	htmlToDisplay += '<ul class="pagination">';
    htmlToDisplay += '<li><a  class="pageLink" onclick="showPrevSearchPage()">Prev</a></li>';
    htmlToDisplay += '<li><a><input type="text" size="3" class="pageNumber" id="searchPageNumber"/></a></li>';
    htmlToDisplay += '<li><a class="pageLink" onclick="jumpOnSearchPage()">GO</a></li>';
    htmlToDisplay += '<li><a class="pageLink" onclick="showNextSearchPage()">Next</a></li>';
  } else {
    htmlToDisplay+=$("#searchPageLinksSection").html();
  }
  htmlToDisplay += '</ul>';
  htmlToDisplay += '</div>';
  
  $("#searchResultSection").html(htmlToDisplay);
  bindKeyOnSearchPageNumber();
  
  //show page, that user want, default to 1;
  showSearchPage();
  
  //initialize ratings
  initializeRating();
}

function bindKeyOnSearchPageNumber(){
 // bind keys on search
 $("#searchPageNumber").on("keyup",function(e) {
    switch(e.keyCode) {
     case 13:{ //enter
      jumpOnSearchPage();
      break;
     }
    }
 });
}

function displaySuggestion(result) {
  var suggestHTML='';
  for(i=0;i<result.hits;i++){
  suggestHTML+='<div>';
    suggestHTML += '<input class="suggestion" style="width:30%"  onClick="executSuggestoin(this);" value="'+result.suggest[i]+'" readonly/>'; 
    suggestHTML += '<input value="Search" style="visibility: hidden;" type="button"/>';
  suggestHTML+='</div>';
  }
  $("#autocompleteSection").html(suggestHTML);
}

function executSuggestoin(element){
  $("#searchQuery").val( $(element).val());
  manualSearch = true;
  $("#searchButton").click();
  $("#searchQuery").focus();
  hideSuggestions();
}


function hideSuggestions(){
  $("#autocompleteSection").html('');
}

function toogleEditable(buttonClicked, divId) {
    hideErrors();
    
    if($(buttonClicked).html() == 'Edit'){
      //enable settings
      $("#"+divId).find("input, select").enable();
      
      //focus
      if(divId.indexOf('server') > -1){
      	$("#serverURL").focus();
      	$("#canselServerSettings").show();
      } else if(divId.indexOf('user') > -1){
      	$("#changeUserSettingsToDefaults").hide();
      	$("#canselUserSettings").show();
      }
      
      $(buttonClicked).html('Save');
    } else {
      //validate
      if((divId.indexOf('server') > -1 && !$("#serverSettingsForm")[0].checkValidity()) ||
      	(divId.indexOf('user') > -1 && !$("#speechSettingsForm")[0].checkValidity())){
      	//form is invalid, do nothing
      	return;
      }
      
      
      if(divId.indexOf('server') > -1){        
      	$("#canselServerSettings").hide();
      	
      	//save settings
      	$("#"+divId).find("input, select").enable(false);
        
        //update server settings
        serverSettings.serverURL = $("#serverURL").val();

        //reload upload section
        $("#uploadFileSection").html("<div id='fileuploader'>Upload</div>");
        loadFileUploadForm();
        
        $(buttonClicked).html('Edit');
      } else if(divId.indexOf('user') > -1){
      	//update settings for user
      	
		var newUserSettings = new Object();
      	newUserSettings.Amplitude = $("#speechAmplitude").val();
      	newUserSettings.WordGap = $("#speechWordGap").val();
      	newUserSettings.Capitals = $("#speechCapitals").val();
      	newUserSettings.LineLength = $("#speechLineLength").val();
      	newUserSettings.Pitch = $("#speechPitch").val();
      	newUserSettings.Speed = $("#speechSpeed").val();
      	newUserSettings.Encoding = $("#speechEncoding").val();
      	newUserSettings.Markup = $("#speechMarkup").prop('checked');
      	newUserSettings.NoFinalPause = $("#speechNoFinalPause").prop('checked');
      	newUserSettings.Language = $("#speechLanguage").val();

      	$.ajax({
	      url: serverSettings.serverURL+"management/user",
	      method: "PUT",
	      data: JSON.stringify(newUserSettings),
		  dataType: 'json',
		  contentType: 'application/json; charset=UTF-8',
		  beforeSend: function (xhr) {
        		xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
                },
	      success: function(data) {
			  speechSettings = data.SpeechSettings;
			  
		      //save settings
		      $("#"+divId).find("input, select").enable(false);
		      
      	 	  loadUserSettings();
	      	  
	      	  $("#changeUserSettingsToDefaults").show();
	      	  $("#canselUserSettings").hide();
      		
      		  $(buttonClicked).html('Edit');
		  },
	      error: function( data, textStatus, errorThrown ) {
		      displayError(data.responseText);
		  }
	    });
      } 
      
    } 
}


function resetUserSettingToDefault() {
    hideErrors();

   	$.ajax({
     url: serverSettings.serverURL+"management/user?setDefaults",
     method: "PUT",
	 dataType: 'json',
	 beforeSend: function (xhr) {
   		xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
     },
	 success: function(data) {
	    speechSettings = data.SpeechSettings;
		     
      	loadUserSettings();
	 },
	 error: function( data, textStatus, errorThrown ) {
	    displayError(data.responseText);
	 }
	});
} 

function cancel(buttonClicked, divId) {
  hideErrors();
  
  $("#"+divId).find("input, select").enable(false);
  if(divId.indexOf('server') > -1){      	
    //update server settings
    $("#serverURL").val(serverSettings.serverURL);
        
    $("#changeServerSettings").html('Edit');
  } else if(divId.indexOf('user') > -1){
  	//update settings for user
   
	loadUserSettings();
	
	$("#changeUserSettingsToDefaults").show();
	$("#changeUserSettings").html('Edit');
  }
  $(buttonClicked).hide() 
}

function loadUserSettings(){
    $("#speechAmplitude").val(speechSettings.Amplitude );
    $("#speechWordGap").val(speechSettings.WordGap);
    $("#speechCapitals").val(speechSettings.Capitals);
    $("#speechLineLength").val(speechSettings.LineLength);
    $("#speechPitch").val(speechSettings.Pitch);
    $("#speechSpeed").val(speechSettings.Speed );
    $("#speechEncoding").val(speechSettings.Encoding);
    $("#speechMarkup").prop('checked',speechSettings.Markup );
    $("#speechNoFinalPause").prop('checked',speechSettings.NoFinalPause);
    $("#speechLanguage").val(speechSettings.Language);
}


    
// #### List files section ###
function listFiles() {
 		toggleVisibilitySubsection('listFilesSection');
 		
	   if(manualListing){
	      startFilesFrom=0;
	      showFilePageNumber = 1;
	      manualListing = true;
	   }
	   
	   if(startFilesFrom==0){
	      $("#filesListSection").html('');
	   }
	    hideErrors();
	    $.ajax({
	      url: serverSettings.serverURL+"management/files",
	      method: "GET",
	      data: {
		      start : startFilesFrom,
		      count : numResultsToReturn
		  },
		  dataType: 'json',
		  beforeSend: function (xhr) {
        		xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
                },
	      success: function(data) {
			  displayFiles(data); 
		  },
	      error: function( data, textStatus, errorThrown ) {
		      displayError(data.responseText);
		  }
	    });
  }

var startFilesFrom = 0;
var showFilePageNumber = 1;
var manualListing = true;
var totalFilesCount = 0;

function showPrevFilePage(){
	if(showFilePageNumber > 1){
		showFilePageNumber--;
		showFilePage();
	}
}

function showNextFilePage(){
	if(showFilePageNumber < Math.ceil(totalFilesCount/numResultsPerPage)){
		showFilePageNumber++;
		showFilePage();
	}
}

function jumpOnFilePage(){
	var pageToShow = $("#filePageNumber").val();
	if(pageToShow>=1 && pageToShow <= Math.ceil(totalFilesCount/numResultsPerPage)){
		showFilePageNumber = pageToShow;
		showFilePage();
	}
}

function showFilePage(){
  if(totalFilesCount>0 && !$("#filePage"+showFilePageNumber).length){
    //initialize File
    startFilesFrom = Math.floor(((showFilePageNumber*numResultsPerPage)-1)/numResultsToReturn)*numResultsToReturn;
    manualListing = false;
    listFiles();
  } else {
  	$("#filePageNumber").val(showFilePageNumber);
    //hide all pages
    $(".filePage:visible").hide();
    $("#filePage"+showFilePageNumber).show();
  }
}

function displayFiles(result) {
  totalFilesCount = result.TotalCount;
  
  var htmlToDisplay ='<div id="filePages">';
  if(startFilesFrom == 0){
    htmlToDisplay+= '<div class="fileListStats">';
    htmlToDisplay+=totalFilesCount + " files listed in " + Math.ceil(totalFilesCount/numResultsPerPage) + " pages.";
    htmlToDisplay+= "</div>";
  } else{
    htmlToDisplay +=  $("#filePages").html();
  }
  
  var numResults = Math.min((totalFilesCount-startFilesFrom),numResultsToReturn);
  var startPage = false;
  var endPage = false;
  for(i=1; i<=numResults ; i++){
    if(i % numResultsPerPage ==1){
      startPage = true;
    }
    if(i % numResultsPerPage == 0){
      endPage = true;
    }
    
    var hitHtml = '';
    if(startPage){
      hitHtml += '<div class="filePage" id="filePage'+Math.ceil((startFilesFrom+i)/numResultsPerPage)+'">';
      startPage = false;
    }
    var fileToDisplay = result.FileItem[i-1];
    hitHtml += '<div class="ajax-file-upload-statusbar">';
    hitHtml += '<div><strong>'+(startFilesFrom+i)+". "+fileToDisplay.Name+'</strong></div>';
    hitHtml += '<div class="rating" id="fileLstRatng' + fileToDisplay.id + '" data-score="' + fileToDisplay.Rating +'"></div>';
    hitHtml += '<div class="ajax-file-upload-green" onclick="downloadFile(\''+fileToDisplay.id+'\')">Download</div>';
    hitHtml += '<div class="ajax-file-upload-red" onclick="deleteFile(\''+fileToDisplay.id+'\',this)">Delete</div>';
    hitHtml += '<div class="ajax-file-upload-yellow" onclick="shareFile(\''+fileToDisplay.id+'\')">Share</div>';
    if(fileToDisplay.SpeechBySlidesLocation){
        hitHtml += '<div class="ajax-file-upload-blue" onclick="displaySlidesInNewPage(\''+fileToDisplay.id+'\')">View</div>';
    }
    hitHtml+='</div><br/>';
    
    if(endPage || i==numResults){
      hitHtml += '</div>';
      endPage = false;
    }
    htmlToDisplay += hitHtml;
  }
  htmlToDisplay += '</div>'; // close section with stats and pages

  //generate pages list
  htmlToDisplay += '<div id="filePageLinksSection" class="pageLinksSecion">';
  if(startFilesFrom == 0){
  	htmlToDisplay += '<ul class="pagination">';
    htmlToDisplay += '<li><a  class="pageLink" onclick="showPrevFilePage()">Prev</a></li>';
    htmlToDisplay += '<li><a><input type="text" size="3" class="pageNumber" id="filePageNumber"/></a></li>';
    htmlToDisplay += '<li><a class="pageLink" onclick="jumpOnFilePage()">GO</a></li>';
    htmlToDisplay += '<li><a class="pageLink" onclick="showNextFilePage()">Next</a></li>';
  } else {
    htmlToDisplay+=$("#filePageLinksSection").html();
  }
  htmlToDisplay += '</ul>';
  htmlToDisplay += '</div>';
  
  $("#filesListSection").html(htmlToDisplay);
  bindKeyOnFilePageNumber();
  
  //show page, that user want, default to 1;
  showFilePage();
  
  //initialize ratings
  initializeRating();
}

function initializeRating(){
  $(".rating").raty({
    cancel: true,
    half: true,
    number: 10,
    path: 'scripts/images',
    click:  function(rating, evt){
    	handleFileRating(this, rating, evt);
    },
    score: function() {
	    return $(this).attr('data-score');
	}
  });
}

function handleFileRating(obj, rating, evt){
	hideErrors();
	if(!rating){
		//canceling rating
		rating = 0;
	}
	
	$.ajax({
	  url: serverSettings.serverURL+"management/files/"+obj.id.substring(12), //remove fileLstRatng/searchRating from ID.
	  method: "PUT",
	  context: this,
	  data: {
		rating : rating
	  },
	  dataType: 'text',
	  beforeSend: function (xhr) {
		xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
	  },
	  success: function(data, textStatus, jqXHR) {
		$("#"+obj.id).attr("data-score", data);
		$("#"+obj.id).raty('score',   data);
	  },
	  error: function( data, textStatus, errorThrown ) {
		displayError(data.responseText);
	   }
	});
}
function bindKeyOnFilePageNumber(){
 // bind keys on search
 $("#filePageNumber").on("keyup",function(e) {
    switch(e.keyCode) {
     case 13:{ //enter
      jumpOnFilePage();
      break;
     }
    }
 });
}

function deleteFile(id, obj) {
 $.ajax({
      url: serverSettings.serverURL+"management/files/"+id,
      method: "DELETE",
	  dataType: 'text',
	  beforeSend: function (xhr) {
       		xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
      },
      success: function(resp,textStatus, jqXHR) { 
	    $(obj).parent().append('<div>' + resp + '</div>');
        if(textStatus == 'success'){
	    	$(obj).parent().delay(5000).fadeOut(400,function() { //remove on success (wait 5 seconds for user to see the response) 
	    		$(this).remove();
	    		//reload files list
	    		manualListing=true;
	    		listFiles();
	    	}); 
       	}
	  },
      error: function( data, textStatus, errorThrown) {
	      displayError(data.responseText);
	  }
 });
}

function downloadFile(id) {
 var authUrl;
 if (serverSettings.serverURL.match("^http://")){
	authUrl = "http://" + serverSettings.userMail + ':' + serverSettings.userPass + "@" + serverSettings.serverURL.substring(7);
  }else { 
	authUrl = "https://" + serverSettings.userMail + ':' + serverSettings.userPass + "@" + serverSettings.serverURL.substring(8);
  }
  
 $("#dialog-download").dialog({
 	modal: true,
	buttons: {
		"Speech": function() {
			window.location.href = authUrl +'management/files/'+ id+"?type=speech";
			$( this ).dialog( "close" );
		},
		"Original": function() {
			window.location.href =  authUrl+'management/files/'+ id+"?type=original"; 
			$( this ).dialog( "close" );
		}
	}
 });
}

function shareFile(id) {
 var shareDialog = '<div id="dialog-share" title="Share file with:"><input id="shareWithUsers" placeholder="Mails of users" autofocus="" required type="text"><div class="errors" id="dialog-errors"></div></div>';
 $(shareDialog).dialog({
 	modal: true,
 	hide: 5000,
	buttons: {
		"Share": function() {
 			var usersToShareWith= $("#shareWithUsers").val();
			$.ajax({
			  url: serverSettings.serverURL+"management/files/"+id,
			  method: "POST",
			  context: this,
			  data: {
				shareWith : usersToShareWith
			  },
			  dataType: 'text',
			  beforeSend: function (xhr) {
				xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
			  },
			  success: function(data, textStatus, jqXHR) {
				$("#dialog-share").html(data);
				$(this).dialog("close");
			  },
			  error: function( data, textStatus, errorThrown ) {
				$("#dialog-errors").html(data.responseText);
			  }
		    });
		}
	}
 });
}

function displaySlidesInNewPage(id) {
  var openedWindow = window.open("slides.html");
  var intervalID = newGuid();
  var interval = setInterval(function(){
  	  var message =   new Object();
	  message.serverURL = serverSettings.serverURL;
	  message.userMail = serverSettings.userMail;
	  message.userPass = serverSettings.userPass;
	  message.fileID = id;
	  message.intervalID = intervalID;
	  openedWindow.postMessage(message,"*");
  }, 1000); //each seccond
  activeIntevals[intervalID] = interval;
}

function clearSendingInterval(event) {
  clearInterval(activeIntevals[event.data]);
}

function newGuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
    function(c) {
      var r = Math.random() * 16 | 0,
        v = c == 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    }).toUpperCase();
}

function loadFileUploadForm(){
 $("#fileuploader").uploadFile({
	url:serverSettings.serverURL+"management/files",
	multiple:true,
	fileName:"uploadfile",
	returnType: "json",
	showStatusAfterSuccess: false,
	useAuthentication: true,
	authType: "Authorization",
	authString: 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass)
 });
}

$(document).ready(function() {

 // #### upload files section ###
 loadFileUploadForm();
 
 //hide all errors
 hideErrors();

 //focus login
 toggleVisibility('section-authentication');

 //clean input fields
 $("input:text").val('');

 //initialize settings page
 $("#serverURL").val(serverSettings.serverURL);
 
 // bind keys on search
 $( "#searchQuery" ).keyup(function(e) {
    switch(e.keyCode) {
     case 13:{ //enter
      $("#searchButton").click();
      break;
     }
     case 38:{ //arrow up
      var selectedSuggestion = $("#autocompleteSection").find('.selectedSuggestion');
      if(selectedSuggestion.length){
      	//move to previos suggestion
      	var nextSuggestion = selectedSuggestion.parent().prev().find(".suggestion");
      	if(nextSuggestion.length){
      		selectedSuggestion.removeClass("selectedSuggestion");
      		nextSuggestion.addClass("selectedSuggestion");
      		
      		selectedSuggestion =nextSuggestion;
      	}
      }
      
      if(selectedSuggestion.length){
      	//add the value to searchQuery
      	$( "#searchQuery" ).val(selectedSuggestion.val());
      }
      break;
     }
     case 40:{ //arrow down
      var selectedSuggestion = $("#autocompleteSection").find('.selectedSuggestion');
      if(selectedSuggestion.length){
      	//move to next suggestion
      	var nextSuggestion = selectedSuggestion.parent().next().find(".suggestion");
      	if(nextSuggestion.length){
      		selectedSuggestion.removeClass("selectedSuggestion");
      		nextSuggestion.addClass("selectedSuggestion");
      		
      		selectedSuggestion =nextSuggestion;
      	}
      } else {
      	//select first suggestion
      	selectedSuggestion = $("#autocompleteSection").find('.suggestion').first();
        selectedSuggestion.addClass("selectedSuggestion");
      }
      
      if(selectedSuggestion.length){
      	//add the value to searchQuery
      	$( "#searchQuery" ).val(selectedSuggestion.val());
      }
      break;
     }
     default: {
      hideErrors();
      //check for autocomplete
      if($(this).val().length>1 ){
		//initialize score query
		var suggestQuery= $("#searchQuery").val();
		$.ajax({
		  url: serverSettings.serverURL+"management/search",
		  method: "PUT",
		  data: {
		      action : "suggest",
		      query : suggestQuery
		  },
		  dataType: 'text',
		  beforeSend: function (xhr) {
		   		xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
		  },
		  success: function(data, textStatus, jqXHR) { 
		      var reply = jQuery.parseJSON(data);
		      displaySuggestion(reply);
		  },
		  error: function( data, textStatus, errorThrown ) {
		      displayError(data.responseText);
		  }
	    });
	   } else{
		hideSuggestions();
	   }
	   break;
     }
    }
 });
 
 $(".navigationMenuButton").click(function() {
    if(!$(this).parent().hasClass('active')){
      //remove all class active
      $(this).parents(".navbar:first").find(".active").removeClass("active");
      $(this).parent().addClass("active");
    }
    hideErrors();
 });
 
   // #####   Searching section   ###
   $("#searchButton").click(function() {
   	   hideSuggestions();
	   if(manualSearch){
	      startSearchFrom=0;
	      showSearchPageNumber = 1;
	      manualSearch = true;
	   }
	   
	   searchQuery= $("#searchQuery").val();
	   if(startSearchFrom==0){
	      $("#searchResultSection").html('');
	   }
	   
	   hideErrors();
	   $.ajax({
		  url: serverSettings.serverURL+"management/search",
		  method: "PUT",
		  data: {
		      action : "search",
		      start : startSearchFrom,
		      count : numResultsToReturn,
		      query : searchQuery
		  },
		  dataType: 'text',
		  beforeSend: function (xhr) {
		   		xhr.setRequestHeader( 'Authorization', 'Basic ' + btoa(serverSettings.userMail + ':' + serverSettings.userPass));
		  },
		  success: function(data, textStatus, jqXHR) { 
		      var reply = jQuery.parseJSON(data);
		      displaySearchResults(reply);
		  },
		  error: function(data, textStatus, errorThrown) {
		      displayError(data.responseText);
		  }
	  });
   });

    // #####   Authentication section   ###
   $("#loginButton").click(function() {
       if(!$("#loginForm")[0].checkValidity()){
       	//form is invalid, do nothing
       	return;
       }
       
	   hideErrors();
	   
   	   var usermail =  $("#loginUserEmail").val();
   	   var password =  $("#loginPassword").val();
	   $.ajax({
		  url: serverSettings.serverURL+"users",
		  method: "PUT",
		  data: {
		      usermail : usermail,
		      password : password
		  },
		  dataType: 'json',
		  success: function(data, textStatus, jqXHR) { 
		      //init user settings for other operations
		      serverSettings.userMail = usermail;
			  serverSettings.userPass = password;
			 
			  speechSettings = data.SpeechSettings;
			  loadUserSettings();
			  $("#section-settings-user").show();
			  
			  //reload upload section to introduce new username and password
			  $("#uploadFileSection").html("<div id='fileuploader'>Upload</div>");
			  loadFileUploadForm();
			  
			  //display main section
			  toggleVisibility('section-main');
			  $("#loggedUser").html(usermail);
			  $("#navigationMenuButtonHome").click();
			  $("#navigationMenuButtonLogInOut").html("Log out");
		  },
		  error: function(data, textStatus, errorThrown) {
		      displayError(data.responseText);
		  }
	  });
   });
   
   $("#navigationMenuButtonLogInOut").click(function() {
       if($("#navigationMenuButtonLogInOut").html() == "Log out"){
       	//request user logout - just reload the page
       	window.location.reload();
       }
   });
   
   $("#registerButton").click(function() {
       if(!$("#registrationForm")[0].checkValidity()){
       	//form is invalid, do nothing
       	return;
       }
       
	   hideErrors();
	   
   	   var username =  $("#registrationUserName").val();
   	   var usermail =  $("#registrationUserEmail").val();
   	   var password =  $("#registrationPassword").val();
	   $.ajax({
		  url: serverSettings.serverURL+"users",
		  method: "POST",
		  data: {
		      username : username,
		      usermail : usermail,
		      password : password
		  },
		  dataType: 'json',
		  success: function(data, textStatus, jqXHR) {
		      //init user settings for other operations
		      serverSettings.userMail = usermail;
			  serverSettings.userPass = password;
			  
			  speechSettings = data.SpeechSettings;
			  loadUserSettings();
			  $("#section-settings-user").show();
			  
			  //reload upload section to introduce new username and password
			  $("#uploadFileSection").html("<div id='fileuploader'>Upload</div>");
			  loadFileUploadForm();
			  
			  //display main section
			  toggleVisibility('section-main');
			  $("#loggedUser").html(usermail);
			  $("#navigationMenuButtonHome").click();
			  $("#navigationMenuButtonLogInOut").html("Log out");
		  },
		  error: function(data, textStatus, errorThrown) {
		      displayError(data.responseText);
		  }
	  });
   });
   
   // #####   Settings section   ###
   $("#changeUserSettings").click(function(){
   	  toogleEditable(this,'section-settings-user');
   });
   
   $("#changeServerSettings").click(function(){
   	  toogleEditable(this,'section-settings-server');
   });
   
   
    // #####   Events section   ###
    //messages are received from slides page for stopping intervals sending them file IDs.
 	if (window.addEventListener) {
    	// For standards-compliant web browsers
 		window.addEventListener("message", clearSendingInterval, false);
 	} else {
 		window.attachEvent("onmessage", clearSendingInterval);
 	}   
});