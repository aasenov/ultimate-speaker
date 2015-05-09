
function toggleVisibility(newSection) {
    $(".section").not("#" + newSection).hide();
    $("#" + newSection).show();
    $("#" + newSection).find("input:text").focus();
    hideSuggestions();
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
    hitHtml += '<h4><a class="searchTitle" title="' + hit.summary + '" href="'+hit.pageURL+'">'+hit.pageTitle+'</a></h4>';
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

        $(document).ready(function() {
 //hide all errors
 hideErrors();

//focus main
toggleVisibility('section-main');

//clean input fields
$("input:text").val('');

// bind enter key
        $( "#searchQuery" ).keyup(function(e) { 
    if(e.keyCode==13){ 
$("#searchButton").click();
             } else {
      hideErrors();
      //check for autocomplete
      if($(this).val().length>1 ){
//initialize score query
var suggestQuery= $("#searchQuery").val();
 $.post("search", { 
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
             }
});
 

 $(".navigationMenuButton").click(function() {
    if(!$(this).parent().hasClass('active')){
      //remove all class active
      $(".active").removeClass("active");
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
    $.post("search", { 
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
});