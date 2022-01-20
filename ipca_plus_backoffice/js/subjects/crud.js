// Get data

// Get selected course id
let courseID = getParameterByName("id", window.location.href);
let courseTag = "";
let courseName = "";
let previousTeacher = "";
let previousSubjectName = "";

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

  html += '<div class="delete-item" id="' + doc.id + '-' + doc.data().teacher + '-' + doc.data().name + 'Rem" type="button" onClick="removeSubject(this.id)"> <i class="fa fa-trash"></i> </div>';
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
    previousTeacher = doc.data().teacher;
    previousSubjectName = doc.data().name;

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
        .doc(docRef.id)
        .set({
          id: docRef.id,
          name: subjectName,
          course: courseID
        })
        .then(() => {

          /* ----------------- Create oficial chat ----------------- */
          db.collection("chat")
            .add({
              name: subjectName,
              type: "oficial" + courseTag,
              ox: "q4bEvvaluivDWvXJDNhaI9acCpNXi7dP",
              iv: "7c5afb00aaecb1a1",
              notificationKey: ""
            })
            .then((docRef2) => {

              /* ----------------- Create first message ----------------- */
              db.collection("chat")
                .doc(docRef2.id)
                .collection("message")
                .add({
                  user: "system",
                  message: "AcNj1olXt82HULKQ8Wlgi6cQJ1+mIyZX31zXjTvkY0+n/WJtN5kZp1qccicLsC3YNNyVCQFd1xn/urlBcZuM/g==",
                  time: firebase.firestore.Timestamp.fromDate(new Date()),
                  files: ""

                }).then((docRef3) => {

                  console.log("chat id: " + docRef2.id)
                  console.log("teacher id: " + teacherID)

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
  let selectedTeacher = form.teacher.value;
  let selectedName = form.name.value;

  // get previous teacher
  console.log(previousTeacher)

  docRef = db.collection("course")
    .doc(courseID)
    .collection("subject")
    .doc(id);

  // Change data in course
  docRef.set({
      name: selectedName,
      teacher: selectedTeacher
    })
    .then((docRef) => {

      // Change teacher
      if (selectedTeacher != previousTeacher) {

        console.log("previous teacher: " + previousTeacher);
        console.log("id: " + id);

        // Remove previous teacher in his data
        db.collection("ipca_data")
          .doc(previousTeacher)
          .collection("subject")
          .where("id", "==", id)
          .get()
          .then((snap) => {
            snap.docs.forEach((doc2) => {

                console.log("entrou: 2");

                // Remove previous teacher in his data
                db.collection("ipca_data")
                  .doc(previousTeacher)
                  .collection("subject")
                  .doc(doc2.id)
                  .delete(() => {
                    recursive: true;
                  })
                  .then(() => {
                    console.log("Document successfully deleted!");
                  })
                  .catch((error) => {
                    console.error("Error removing document: ", error);
                  });

                // add new teacher in his data
                db.collection("ipca_data")
                  .doc(selectedTeacher)
                  .collection("subject")
                  .add({
                    id: id,
                    name: selectedName,
                    course: courseID
                  })
                  .then(() => {
                    console.log("Document successfully added!");
                  })
                  .catch((error) => {
                    console.error("Error adding document: ", error);
                  });

                

              });
          })
          .catch((error) => {
            console.error("Error adding document: ", error);
          });

      }

      // Change name in chat as well
      if (selectedName != previousSubjectName) {

        // Get subject chat
        db.collection("chat")
          .where("name", "==", previousSubjectName)
          .get()
          .then((snap) => {
            snap.docs.forEach((doc) => {

              if (doc.data().type == "oficial" + courseTag) {
                db.collection("chat")
                  .doc(doc.id)
                  .set({
                    name: selectedName,
                    type: "oficial" + courseTag,
                    ox: "q4bEvvaluivDWvXJDNhaI9acCpNXi7dP",
                    iv: "7c5afb00aaecb1a1",
                    notificationKey: ""
                  })
                  .then(() => {
                    console.log("Document successfully added!");
                  });
              }

            });
          });

      }

      form.reset();
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove Subject
function removeSubject(id) {
  id = id.replace("Rem", "");
  let selectedTeacher = id.split('-')[1];
  let selectedChatName = id.split('-')[2];
  id = id.split('-')[0]

  console.log(id)

  // Remove the subject in course
  db.collection("course")
    .doc(courseID)
    .collection("subject")
    .doc(id)
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

  // Remove subject in every teacher
  db.collection("ipca_data")
    .doc(selectedTeacher)
    .collection("subject")
    .where("id", "==", id)
    .get()
    .then((snap) => {
      snap.docs.forEach((doc2) => {

        db.collection("ipca_data")
          .doc(selectedTeacher)
          .collection("subject")
          .doc(doc2.id)
          .delete(() => {
            recursive: true;
          })
          .then(() => {
            console.log("Document successfully deleted!");
          })
          .catch((error) => {
            console.error("Error removing document: ", error);
          });

      });
    });

  // Remove oficial chat
  db.collection("chat")
    .where("name", "==", selectedChatName)
    .get()
    .then((snap) => {
      snap.docs.forEach((doc2) => {

        if (doc2.data().type == "oficial" + courseTag) {

          db.collection("chat")
            .doc(doc2.id)
            .delete(() => {
              recursive: true;
            })
            .then(() => {
              console.log("Document successfully deleted!");
            })
            .catch((error) => {
              console.error("Error removing document: ", error);
            });
        }

      });
    });

}