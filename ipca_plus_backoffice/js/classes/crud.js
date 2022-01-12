
// Get data

// Get selected course id
let courseID = getParameterByName("courseId", window.location.href);
let subjectID = getParameterByName("subjectId", window.location.href);

console.log(courseID)
console.log(subjectID)

// Get Course Name
db.collection("course")
  .doc(courseID)
  .get()
  .then(doc => {
    if (doc.exists) {
      document.querySelector("#titlePage").innerHTML = "<strong>" + doc.data().name + " - Disciplinas </strong>";
    }
  });

// Get Subjects
db.collection("course")
  .doc(courseID)
  .collection("subject")
  .orderBy("name")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

      document.querySelector("#list").innerHTML += addSubjectToList(doc);

    });
  });


// Get all Subjects and display in the list
function addSubjectToList(doc) {
  let subject = doc.data();
  let html = "";

  html += "<li> <div class='item-list'>";
  html += " <div class='row'>";
  html += "  <div class='card-body'>";
  html += " <div class='row'>";
  html += " <div class='col-10'>";
  html += " <div class='item-info'>";
  html += " <strong>" + subject.name + "</strong> ";
  html += " </div> </div>";
  html += " <div class='col' style='display: flex; justify-content: flex-end;'>";
  html += '<div class="see-classes" id="' + doc.id + '" type="button" onClick="forClassesPage(this.id)"> <i class="far fa-calendar-alt"></i></i> </div>';
  html += " </div>";
  html += " <div class='col' style='display: flex; justify-content: flex-end;'>";
  html += '<div class="edit-item" id="' + doc.id + 'Edit" type="button" onClick="forEdition(this.id)"> <i class="fa fa-edit"></i> </div>';
  html += " </div> </div> </div>";

  
  html += '<div class="delete-item" id="' + doc.id + 'Rem" type="button" onClick="removeCourse(this.id)"> <i class="fa fa-trash"></i> </div>';
  html += " </div> </div> </div> </li>";

  form.reset();
  document.querySelector("#addSubject").value = "new";

  return html;
}




// Put Subject data in text boxes
function forEdition(id) {
  let docRef;
  id = id.replace("Edit", "");

  docRef = db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(id);

  docRef.get().then((doc) => {
    form.value = id;
    form.name.value = doc.data().name;
    form.teacher.value = doc.data().teacher;

    document.querySelector("#addSubject").value = "edit";

  });
}


// Submit button event
form.addEventListener("submit", (e) => {
  e.preventDefault();
  let button = document.querySelector("#addSubject");

  console.log("submit")

  if (button.value == "new") {
    addSubject();
  } else {
    editSubject(form.value);
    document.querySelector("#addSubject").value = "new";
  }

  // Refresh List
  refreshSubjectList()

  form.reset();
});


// Refresh Subject List
function refreshSubjectList() {

  // Delete Previous List
  let menu = document.getElementById('list');
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  // Add the elements
  db.collection("course")
  .doc(courseID)
  .collection("subject")
    .orderBy("name")
    .get()
    .then((snap) => {
      snap.docs.forEach((doc) => {
        document.querySelector("#list").innerHTML += addSubjectToList(doc);
      });
    });
}


// Add Subject
function addSubject() {

  db.collection("course")
  .doc(courseID)
  .collection("subject")
    .add({
      name: form.name.value,
      teacher: form.teacher.value
    })
    .then((docRef) => {
      console.log("New Subject successfully added: ", docRef);
    })
    .catch((error) => {
      console.error("Error adding document: ", error);
    });

    /*
    
      create oficial chat
    
    */

}


// Edit Subject
function editSubject(id) {
  let docRef;

  docRef = db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(id);

  docRef.set({      
      name: form.name.value,
      teacher: form.teacher.value
    })
    .then((docRef) => {
      console.log("New Subject successfully edited: ", docRef);
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove Subject
function removeSubject(id) {
  id = id.replace("Rem", "");

  docRef = db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(id);

  docRef
    .delete(() => {
      recursive: true;
    })
    .then(() => {
      console.log("Document successfully deleted!");

      // Refresh List
      refreshSubjectList()
    })
    .catch((error) => {
      console.error("Error removing document: ", error);
    });
}