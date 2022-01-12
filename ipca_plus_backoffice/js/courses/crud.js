
// Get data

// Get Courses
db.collection("course")
  .orderBy("name")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

      document.querySelector("#list").innerHTML += addCourseToList(doc);

    });
  });


// Get all Courses and display in the list
function addCourseToList(doc) {
  let course = doc.data();
  let html = "";

  html += "<li> <div class='item-list'>";
  html += " <div class='row'>";
  html += "  <div class='card-body'>";
  html += " <div class='row'>";
  html += " <div class='col-10'>";
  html += " <div class='item-info'>";
  html += " <strong>" + course.tag + " - " + course.name + "</strong> ";
  html += " </div> </div>";
  html += " <div class='col' style='display: flex; justify-content: flex-end;'>";
  html += '<div class="see-subjects" id="' + doc.id + '" type="button" onClick="forSubjectPage(this.id)"> <i class="fas fa-info-circle"></i> </div>';
  html += " </div>";
  html += " <div class='col' style='display: flex; justify-content: flex-end;'>";
  html += '<div class="edit-item" id="' + doc.id + 'Edit" type="button" onClick="forEdition(this.id)"> <i class="fa fa-edit"></i> </div>';
  html += " </div> </div> </div>";

  
  html += '<div class="delete-item" id="' + doc.id + 'Rem" type="button" onClick="removeCourse(this.id)"> <i class="fa fa-trash"></i> </div>';
  html += " </div> </div> </div> </li>";

  form.reset();
  document.querySelector("#addCourse").value = "new";

  return html;
}


function forSubjectPage(id) {
  console.log(id)
  document.location.href = "subjects.html?id=" + id + "";
}


// Put course data in text boxes
function forEdition(id) {
  let docRef;
  id = id.replace("Edit", "");

  docRef = db.collection("course").doc(id);

  docRef.get().then((doc) => {
    form.value = id;
    form.name.value = doc.data().name;
    form.tag.value = doc.data().tag;

    document.querySelector("#addCourse").value = "edit";

  });
}


// Submit button event
form.addEventListener("submit", (e) => {
  e.preventDefault();
  let button = document.querySelector("#addCourse");

  console.log("submit")

  if (button.value == "new") {
    addCourse();
  } else {
    editCourse(form.value);
    document.querySelector("#addCourse").value = "new";
  }

  // Refresh List
  refreshCourseList()

  form.reset();
});


// Refresh Course List
function refreshCourseList() {

  // Delete Previous List
  let menu = document.getElementById('list');
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  // Add the elements
  db.collection("course")
    .orderBy("name")
    .get()
    .then((snap) => {
      snap.docs.forEach((doc) => {
        document.querySelector("#list").innerHTML += addCourseToList(doc);
      });
    });
}


// Add Course
function addCourse() {

  db.collection("course")
    .add({
      name: form.name.value,
      tag: form.tag.value
    })
    .then((docRef) => {
      console.log("New course successfully added: ", docRef);
    })
    .catch((error) => {
      console.error("Error adding document: ", error);
    });

}


// Edit Course
function editCourse(id) {
  let docRef;

  docRef = db.collection("course").doc(id);
  docRef.set({
      name: form.name.value,
      tag: form.tag.value
    })
    .then((docRef) => {
      console.log("New course successfully edited: ", docRef);
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove Course
function removeCourse(id) {
  id = id.replace("Rem", "");
  docRef = db.collection("course").doc(id);

  docRef
    .delete(() => {
      recursive: true;
    })
    .then(() => {
      console.log("Document successfully deleted!");

      // Refresh List
      refreshCourseList()
    })
    .catch((error) => {
      console.error("Error removing document: ", error);
    });
}