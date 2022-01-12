
// Get data

// Get Contacts
db.collection("contacts")
  .orderBy("name")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

      document.querySelector("#list").innerHTML += addContactsToList(doc);

    });
  });


// Get all Contacts and display in the list
function addContactsToList(doc) {
  let contact = doc.data();
  let html = "";

  html += "<li> <div class='item-list'>";
  html += " <div class='row'>";
  html += "  <div class='card-body'>";
  html += " <div class='row'>";
  html += " <div class='col-10'>";
  html += " <div class='item-info'>";
  html += " <strong>" + contact.name + " - " + contact.desc + "</strong> <br><br> "+ contact.site;
  html += " </div> </div>";
  html += " <div class='col' style='display: flex; justify-content: flex-end;'>";

  html += '<div class="edit-item" id="' + doc.id + 'Edit" type="button" onClick="forEdition(this.id)"><i class="fa fa-edit"></i>';

  html += " </div> </div> </div> </div>";
  html += '<div class="delete-item" id="' + doc.id + 'Rem" type="button" onClick="removeContact(this.id)"><i class="fa fa-trash"></i></div>';
  html += " </div> </div> </div> </li>";


  contactForm.reset();
  document.querySelector("#addContact").value = "new";

  return html;
}


// Put Contact data in text boxes
function forEdition(id) {
  let docRef;
  id = id.replace("Edit", "");

  docRef = db.collection("contacts").doc(id);

  docRef.get().then((doc) => {
    contactForm.value = id;
    contactForm.name.value = doc.data().name;
    contactForm.number.value = doc.data().number;
    contactForm.desc.value = doc.data().desc;
    contactForm.location.value = doc.data().location;
    contactForm.site.value = doc.data().site;
    contactForm.email.value = doc.data().email;

    document.querySelector("#addContact").value = "edit";

  });
}


// Submit button event
contactForm.addEventListener("submit", (e) => {
  e.preventDefault();
  let button = document.querySelector("#addContact");

  console.log("submit")

  if (button.value == "new") {
    addContact();
  } else {
    editContact(contactForm.value);
    document.querySelector("#addContact").value = "new";
  }

  // Refresh List
  refreshContactList()

  contactForm.reset();
});


// Refresh Contact List
function refreshContactList() {

  // Delete Previous List
  let menu = document.getElementById('list');
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  // Add the elements
  db.collection("contacts")
    .orderBy("name")
    .get()
    .then((snap) => {
      snap.docs.forEach((doc) => {
        document.querySelector("#list").innerHTML += addContactsToList(doc);
      });
    });
}


// Add Contact
function addContact() {

  db.collection("contacts")
    .add({
      name: contactForm.name.value,
      desc: contactForm.desc.value,
      site: contactForm.site.value,
      number: contactForm.number.value,
      location: contactForm.location.value,
      email: contactForm.email.value
    })
    .then((docRef) => {
      console.log("New contact successfully added: ", docRef);
    })
    .catch((error) => {
      console.error("Error adding document: ", error);
    });

}


// Edit Contact
function editContact(id) {
  let docRef;

  docRef = db.collection("contacts").doc(id);
  docRef.set({
      name: contactForm.name.value,
      desc: contactForm.desc.value,
      site: contactForm.site.value,
      number: contactForm.number.value,
      location: contactForm.location.value,
      email: contactForm.email.value
    })
    .then((docRef) => {
      console.log("New Contact successfully edited: ", docRef);
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove Contact
function removeContact(id) {
  id = id.replace("Rem", "");
  docRef = db.collection("contacts").doc(id);

  docRef
    .delete(() => {
      recursive: true;
    })
    .then(() => {
      console.log("Document successfully deleted!");

      // Refresh List
      refreshContactList()
    })
    .catch((error) => {
      console.error("Error removing document: ", error);
    });
}