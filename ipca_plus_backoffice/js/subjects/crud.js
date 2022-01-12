
// Get data

// Get selected course id
let courseID = getParameterByName("id", window.location.href);
let courseTag = "";
let courseName = "";

// Get Course data
db.collection("course")
  .doc(courseID)
  .get()
  .then(doc => {
    if (doc.exists) {
      courseTag = doc.data().tag;
      courseName = doc.data().name;
      document.querySelector("#titlePage").innerHTML = "<strong>" + courseName + " - Disciplinas </strong>";
    }
  });

// Get All teachers
db.collection("ipca_data")
  .where("role", "==", "Professor")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

      document.querySelector("#teacher").innerHTML += addTeachersToSelect(doc);

    });
  });


// Add all Teachers to select object
function addTeachersToSelect(doc) {
  let teacher = doc.data();
  let html = "";

  html += '<option value="' + doc.id + '"> ' + teacher.name + ' </option>';

  return html;
}


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

  html += '<div class="delete-item" id="' + doc.id + 'Rem" type="button" onClick="removeSubject(this.id)"> <i class="fa fa-trash"></i> </div>';
  html += " </div> </div> </div> </li>";

  form.reset();
  document.querySelector("#addSubject").value = "new";

  return html;
}

function forClassesPage(id) {
  console.log(id)
  document.location.href = "classes.html?courseId=" + courseID + "?subjectId=" + id;
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

  var e = form.teacher;
  var teacherID = e.options[e.selectedIndex].value;
  var subjectName = form.name.value

  db.collection("course")
  .doc(courseID)
  .collection("subject")
    .add({
      name: subjectName,
      teacher: teacherID
    })
    .then((docRef) => {
      
      /* Add subject to teacher */
      db.collection("ipca_data")
        .doc(teacherID)
        .collection("subject")
        .add({
          id: docRef.id
        })
        .then(() => {

          /* ----------------- Create oficial chat ----------------- */
          db.collection("chat")
          .add({
            name: subjectName,
            type: "oficial",
            ox: "q4bEvvaluivDWvXJDNhaI9acCpNXi7dP"
          })
          .then((docRef2) => {

            /* ----------------- Create first message ----------------- */
            db.collection("chat")
            .doc(docRef2.id)
            .collection("message")
            .add({
              user: "system",
              message: "AcNj1olXt82HULKQ8Wlgi6cQJ1+mIyZX31zXjTvkY0+n/WJtN5kZp1qccicLsC3YNNyVCQFd1xn/urlBcZuM/g==",
              time: Date.now(),
              files: ""

            }).then((docRef3) => {
              
              console.log("chat id: " + docRef2.id)
              console.log("teacher id: " + teacherID)

              /* ----------------- Add teacher admin ----------------- */
              db.collection("chat")
              .doc(docRef2.id)
              .collection("user")
              .doc(teacherID)
              .set({
                admin: true
              }).then((docRef4) => {
                form.reset();
              });

              /* ----------------- Add all users in the course ----------------- */

              db.collection("ipca_data")
                .get()
                .then((snap) => {
                  snap.docs.forEach((doc) => {

                    db.collection("ipca_data")
                    .doc(doc.id)
                    .collection("course")
                    .where("tag", "==", courseTag)
                    .get()
                    .then((snap2) => {
                      snap2.docs.forEach(() => {
                        
                        db.collection("chat")
                        .doc(docRef2.id)
                        .collection("user")
                        .doc(doc.id)
                        .set({
                          admin: false
                        }).then(() => {
                          console.log("user adicionado: " + doc.data());
                        });

                      });
                    });
                  });
                });

              /* The users are added durind the user creation or edition */

            });

            form.reset();
          });
          
        });

    })
    .catch((error) => {
      console.error("Error adding document: ", error);
    });


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

      // Edit teacher

      // remover teacher antigo nos dados dele

      // remover teacher antigo como admin

      // add teacher nos dados dele

      // add teacher no chat como admin

      console.log("New Subject successfully edited: ", docRef);
      
      form.reset();
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove Subject
function removeSubject(id) {
  id = id.replace("Rem", "");

  // Remove the subject in course
  docRef = db.collection("course")
  .doc(courseID)
  .collection("subject")
  .doc(id);

  // Remove subject in every student and teacher

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