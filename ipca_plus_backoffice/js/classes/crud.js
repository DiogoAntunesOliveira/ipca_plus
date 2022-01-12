
// Get data

// Get selected course id
let courseID = getParameterByName("courseId", window.location.href);
courseID = courseID.split('?')[0]
let subjectID = getParameterByName("subjectId", window.location.href);

console.log(courseID)
console.log(subjectID)

// Get Subject Name
db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(subjectID)
  .get()
  .then(doc => {
    if (doc.exists) {
      document.querySelector("#titlePage").innerHTML = "<strong>" + doc.data().name + " - Aulas </strong>";
    }
  });

// Get Class
db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(subjectID)
  .collection("class")
  .orderBy("start_time")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

      document.querySelector("#list").innerHTML += addClassToList(doc);

    });
  });


function getDayByTag(tag) {

  switch(tag) {
    case "seg":
      return "Segunda-Feira";
    case "ter":
      return "Terça-Feira";
    case "qua":
      return "Quarta-Feira";
    case "qui":
      return "Quinta-Feira";
      
    default:
      return "Sexta-Feira";
  }

}


// Get all Class and display in the list
function addClassToList(doc) {
  let aula = doc.data();
  let html = "";

  html += "<li> <div class='item-list'>";
  html += " <div class='row'>";
  html += "  <div class='card-body'>";
  html += " <div class='row'>";
  html += " <div class='col-10'>";
  html += " <div class='item-info'>";
  html += " <strong>" + getDayByTag(aula.day) + " " + aula.start_time + " - " + aula.end_time + "</strong> ";
  html += " </div> </div>";
  html += " <div class='col' style='display: flex; justify-content: flex-end;'>";
  html += '<div class="edit-item" id="' + doc.id + 'Edit" type="button" onClick="forEdition(this.id)"> <i class="fa fa-edit"></i> </div>';
  html += " </div> </div> </div>";
  
  html += '<div class="delete-item" id="' + doc.id + 'Rem" type="button" onClick="removeClass(this.id)"> <i class="fa fa-trash"></i> </div>';
  html += " </div> </div> </div> </li>";

  form.reset();
  document.querySelector("#addClass").value = "new";

  return html;
}


// Put Class data in text boxes
function forEdition(id) {
  let docRef;
  id = id.replace("Edit", "");

  docRef = db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(subjectID)
  .collection("class")
  .doc(id);

  docRef.get().then((doc) => {
    form.value = id;
    form.classroom.value = doc.data().classroom;
    form.day.value = doc.data().day;
    form.start_time.value = doc.data().start_time;
    form.end_time.value = doc.data().end_time;

    document.querySelector("#addClass").value = "edit";

  });
}


// Submit button event
form.addEventListener("submit", (e) => {
  e.preventDefault();
  let button = document.querySelector("#addClass");

  if (button.value == "new") {
    addClass();
  } else {
    editClass(form.value);
    document.querySelector("#addClass").value = "new";
  }

  // Refresh List
  refreshClassList()

  form.reset();
});


// Refresh Class List
function refreshClassList() {

  // Delete Previous List
  let menu = document.getElementById('list');
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  // Add the elements
  db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(subjectID)
  .collection("class")
    .orderBy("start_time")
    .get()
    .then((snap) => {
      snap.docs.forEach((doc) => {
        document.querySelector("#list").innerHTML += addClassToList(doc);
      });
    });
}


// Add Class
function addClass() {

  db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(subjectID)
  .collection("class")
    .add({
      classroom: form.classroom.value,
      day: form.day.value,
      start_time: form.start_time.value,
      end_time: form.end_time.value
    })
    .then((docRef) => {
      console.log("New Class successfully added: ", docRef);
    })
    .catch((error) => {
      console.error("Error adding document: ", error);
    });

}


// Edit Class
function editClass(id) {
  let docRef;

  docRef = db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(subjectID)
  .collection("class")
  .doc(id);

  docRef.set({      
      classroom: form.classroom.value,
      day: form.day.value,
      start_time: form.start_time.value,
      end_time: form.end_time.value
    })
    .then((docRef) => {
      console.log("New Class successfully edited: ", docRef);
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove Class
function removeClass(id) {
  id = id.replace("Rem", "");

  docRef = db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(subjectID)
  .collection("class")
  .doc(id);

  docRef
    .delete(() => {
      recursive: true;
    })
    .then(() => {
      console.log("Document successfully deleted!");

      // Refresh List
      refreshClassList()
    })
    .catch((error) => {
      console.error("Error removing document: ", error);
    });
}