var settings = new Object();
settings.serverURL = "http://127.0.0.1:8181/";

function toggleVisibility(newSection) {
    $(".section").not("#" + newSection).hide();
    $("#" + newSection).show();
    var inputText = $("#" + newSection).find("input:text");
    if(inputText && !inputText.prop("disabled")){
    	inputText.focus();
    }
    hideSuggestions();
}

function toggleVisibilitySubsection(newSection) {
    $(".subSection").not("#" + newSection).hide();
    $("#" + newSection).show();
    var inputText = $("#" + newSection).find("input:text");
    if(inputText.length && !inputText.prop("disabled")){
    	inputText.focus();
    }
}

function displayError(errorText) {
    $(".errors").show();
    $(".errors").html(errorText);
}

function hideErrors(){
  $(".errors").hide();
}
 
   // #####   Searching section   ###
   
var showPageNumber = 1;
function showPage(){
  if((showPageNumber*numResultsPerPage)>(startSearchFrom+numResultsToReturn)){
    //initialize search
    startSearchFrom = Math.floor(((showPageNumber*numResultsPerPage)-1)/numResultsToReturn)*numResultsToReturn;
    $("#searchQuery").val(searchQuery);
    
    manualSearch = false;
    $("#searchButton").click();
  } else {
    //hide all pages
    $(".page:visible").hide();
    $("#page"+showPageNumber).show();
    $("#pageLink"+showPageNumber).css("color", "red");
  }
}


var numResultsToReturn = 100;
var numResultsPerPage = 10;
var startSearchFrom = 0;
var searchQuery;
var manualSearch = true;
var maxPagesToDisplay = 30;

function displaySearchResults(result) {
var htmlToDisplay ='<div id="searchPages">';
  if(startSearchFrom == 0){
    htmlToDisplay+= '<div class="searchStats">';
    htmlToDisplay+="Hits: "+result.hits;
    htmlToDisplay+= " (took "+result.tookInMillis/1000+" seconds.) </div>";
  } else{
    htmlToDisplay +=  $("#searchPages").html();
  }
   
  var numResults = Math.min((result.hits-startSearchFrom),numResultsToReturn);
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
      hitHtml += '<div class="page" id="page'+Math.ceil((startSearchFrom+i)/numResultsPerPage)+'">';
      startPage = false;
    }
        var hit = result["hit"+i];
    hitHtml += '<div class="searchHit">';
    hitHtml += '<h4><a class="searchTitle" title="' + hit.summary + '" href="'+hit.documentID+'">'+hit.documentTitle+'</a></h4>';
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
  htmlToDisplay += '<div id="pageLinksSection" class="pageLinks">';
  if(startSearchFrom ==0){
    var numPagesToShow = Math.min(maxPagesToDisplay,Math.ceil(result.hits/numResultsPerPage));
    var displayMorePages = false;
    if(maxPagesToDisplay < Math.ceil(result.hits/numResultsPerPage)){
      displayMorePages = true;
    }
    for(i=1;i<=numPagesToShow;i++){
      htmlToDisplay += '<a id="pageLink'+i+'" class="pageLink" onclick="showPageNumber='+i+'; showPage()">'+i+'&nbsp;</a>';
    }
    if(displayMorePages){
      htmlToDisplay += '<span class="pageLink"> ....&nbsp;</span>';
    }
  } else {
    htmlToDisplay+=$("#pageLinksSection").html();
  }
  htmlToDisplay += '</div>';
  
  $("#searchResultSection").html(htmlToDisplay);
  
  //show page, that user want, default to 1;
  showPage();
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

function toogleEditable(buttonClicked, inputName) { 
    if($("#" + inputName).prop("disabled")){
      $("#" + inputName).prop("disabled", false);
      $(buttonClicked).val('Save');
      $("#" + inputName).focus();
    } else {
      $("#" + inputName).prop("disabled", true);
      $(buttonClicked).val('Edit');
      
      //update settings
      settings[inputName] = $("#" + inputName).val();
      
      //reload upload section
      $("#uploadFileSection").html("<div id='fileuploader'>Upload</div>");
      loadFileUploadForm();
    } 
  }

var startListFrom = 0;

function displayFiles(result) {
 var htmlToDisplay ='<div id="filePages">';
  var numResults = Math.min(result.length);
  var startPage = false;
  var endPage = false;
  for(i=0; i<numResults ; i++){
    if(i % numResultsPerPage ==1){
      startPage = true;
    }
    if(i % numResultsPerPage == 0){
      endPage = true;
    }
    
    var hitHtml = '';
    if(startPage){
      hitHtml += '<div class="page" id="page'+Math.ceil((startListFrom+i)/numResultsPerPage)+'">';
      startPage = false;
    }
    var fileToDisplay = result[i];
    hitHtml += '<div class="fileDiv">';
    hitHtml += '<h4><a class="documentTitle" title="' + fileToDisplay.Name + '" href="'+fileToDisplay.id+'">'+fileToDisplay.Name+'</a></h4>';
    hitHtml+='</div><br/>';
    
    if(endPage || i==numResults){
      hitHtml += '</div>';
      endPage = false;
    }
    htmlToDisplay += hitHtml;
  }
  htmlToDisplay += '</div>'; // close section with stats and pages

  //generate pages list
  htmlToDisplay += '<div id="pageLinksSection" class="pageLinks">';
  if(startListFrom ==0){
    var numPagesToShow = Math.min(maxPagesToDisplay,Math.ceil(result.length/numResultsPerPage));
    var displayMorePages = false;
    if(maxPagesToDisplay < Math.ceil(result.length/numResultsPerPage)){
      displayMorePages = true;
    }
    for(i=1;i<=numPagesToShow;i++){
      htmlToDisplay += '<a id="pageLink'+i+'" class="pageLink" onclick="showPageNumber='+i+'; showPage()">'+i+'&nbsp;</a>';
    }
    if(displayMorePages){
      htmlToDisplay += '<span class="pageLink"> ....&nbsp;</span>';
    }
  } else {
    htmlToDisplay+=$("#pageLinksSection").html();
  }
  htmlToDisplay += '</div>';
  
  $("#filesListSection").html(htmlToDisplay);
  
  //show page, that user want, default to 1;
  showPage();
}

function loadFileUploadForm(){
 $("#fileuploader").uploadFile({
	url:settings.serverURL+"files",
	multiple:true,
	fileName:"uploadfile"
 });
}
$(document).ready(function() {

 // #### upload files section ###
 loadFileUploadForm();
 
 //focus subsection
 toggleVisibilitySubsection('listFilesSection');

 //hide all errors
 hideErrors();

 //focus main
 toggleVisibility('section-main');

 //clean input fields
 $("input:text").val('');

 //initialize settings page
 $("#serverURL").val(settings.serverURL);
 
 
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
 		$.post(settings.serverURL+"search", { 
		    action : "suggest",
		    searchQuery : suggestQuery},
			function(data) {
			  var reply = jQuery.parseJSON(data);
			  if(reply.error){
			    displayError(reply.errorMessage);
			  } else {
			    displaySuggestion(reply);
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
	      showPageNumber = 1;
	      manualSearch = true;
	   }
	   
	   searchQuery= $("#searchQuery").val();
	   if(startSearchFrom==0){
	      $("#searchResultSection").html('');
	   }
	    hideErrors();
	    $.post(settings.serverURL+"search", { 
	      action : "StartSearching",
	      startFrom : startSearchFrom,
	      size : numResultsToReturn,
	      searchQuery : searchQuery},
	    function(data) {
	      var reply = jQuery.parseJSON(data);
	      if(reply.error){
	    displayError(reply.errorMessage);
	      } else {
	        displaySearchResults(reply);
	      }
	    });
   });
    
    // #### List files section ###
    $("#refreshFileListButton").click(function() {
	    startListFrom=0;
	    showPageNumber = 1;
	   
	   searchQuery= $("#searchQuery").val();
	   if(startSearchFrom==0){
	      $("#filesListSection").html('');
	   }
	    hideErrors();
	    $.get(settings.serverURL+"files", {
	      start : startListFrom,
	      count : numResultsToReturn,
	      out : "json"},
	    function(data) { 
	      var reply =  data;//we should receive json object
	      if(reply.error){
	    	displayError(reply.errorMessage);
	      } else {
	        displayFiles(reply);
	      }
	    });
    });
});