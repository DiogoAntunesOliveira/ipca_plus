
function myFunction() {

    var input, filter, ul, li, a, i, txtValue;
    input = document.getElementById("myInput");
    filter = input.value.toUpperCase();
    ul = document.getElementById("list");
    li = ul.getElementsByTagName("li");
    for (i = 0; i < li.length; i++) {
        a = li[i].getElementsByClassName("item-info")[0];
        txtValue = a.textContent || a.innerText;
        if (txtValue.toUpperCase().indexOf(filter) > -1) {
            li[i].style.display = "";
        } else {
            li[i].style.display = "none";
        }
    }
}

function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

function login(id) {

    modal = document.getElementById(id);
    pass = document.getElementById("pass");
    email = document.getElementById("email");
    modal.style.display = "block";

    setTimeout(function(){

        if (pass.value == "!Root1793" && email.value == "admin@ipca.pt") 
            document.location.href = "pages/dashboard.html";
        else {
            pass.value = "none";
            email.value = "none";
            modal.style.display = "none";
        }

    }, 2000);

}

// Get the modal
var modal;

// When the user clicks the button, open the modal 
function openModal(id){
    modal = document.getElementById(id);
    modal.style.display = "block";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
    if (event.target == modal) {
    modal.style.display = "none";
    }
}