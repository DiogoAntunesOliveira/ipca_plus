
// Get data

// Get Users
db.collection("ipca_data")
  .orderBy("student_number")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

      document.querySelector("#list").innerHTML += addUserToList(doc);

    });
  });


// Get Courses
db.collection("course")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

      document.querySelector("#courses").innerHTML += addCoursesToSelect(doc);

    });
  });


var select = document.querySelector('#role'),
input = document.querySelector('input[type="button"]');

select.addEventListener('change', function(){

  if (select.value == "Professor") {
    document.getElementById("coursesDiv").style.display = "none";
  }
  else {
    document.getElementById("coursesDiv").style.display = "";
  }

});


// Add all courses to select object
function addCoursesToSelect(doc) {
  let course = doc.data();
  let html = "";

  html += '<option value="' + course.tag + '"> ' + course.name + ' </option>';

  return html;
}


// Get all Users and display in the list
function addUserToList(doc) {
  let user = doc.data();
  let html = "";

  html += "<li> <div class='item-list'>";
  html += " <div class='row'>";
  html += "  <div class='card-body'>";
  html += " <div class='row'>";
  html += " <div class='col-10'>";
  html += " <div class='item-info'>";
  html += " <strong>" + user.student_number + " - " + user.name + "</strong> <br><br> "+ user.role;
  html += " </div> </div>";
  html += " <div class='col' style='display: flex; justify-content: flex-end;'>";

  html += '<div class="edit-item" id="' + doc.id + 'Edit" type="button" onClick="forEdition(this.id)"><i class="fa fa-edit"></i>';

  html += " </div> </div> </div> </div>";
  html += '<div class="delete-item" id="' + doc.id + 'Rem" type="button" onClick="removeUser(this.id)"><i class="fa fa-trash"></i></div>';
  html += " </div> </div> </div> </li>";

  userForm.reset();
  document.querySelector("#addUser").value = "new";

  return html;
}


// Put user data in text boxes
function forEdition(id) {
  let docRef;
  id = id.replace("Edit", "");

  docRef = db.collection("ipca_data").doc(id);

  docRef.get().then((doc) => {
    userForm.value = id;
    userForm.name.value = doc.data().name;
    userForm.contact.value = doc.data().contact;
    userForm.age.value = doc.data().age;
    userForm.courses.value = doc.data().course_tag;
    userForm.role.value = doc.data().role;
    userForm.email.value = doc.data().email;
    userForm.gender.value = doc.data().gender;

    if (userForm.role.value == "Professor") {
      document.getElementById("coursesDiv").style.display = "none";
    }
    else {

      // Get course of the student
      db.collection("ipca_data")
      .doc(id)
      .collection("course")
      .get()
      .then((snap) => {

        snap.docs.forEach((doc2) => {

          document.getElementById("coursesDiv").style.display = "";
          userForm.courses.value = doc2.data().tag;

        });

      });
      
    }

    document.querySelector("#addUser").value = "edit";

  });
}


// Submit button event
userForm.addEventListener("submit", (e) => {
  e.preventDefault();
  let button = document.querySelector("#addUser");

  if (button.value == "new") {
    addUser();
  } else {
    editUser(userForm.value);
    document.querySelector("#addUser").value = "new";
  }

  // Refresh List
  refreshUserList()

});


// Refresh User List
function refreshUserList() {

  // Delete Previous List
  let menu = document.getElementById('list');
  while (menu.firstChild) {
    menu.removeChild(menu.firstChild);
  }

  // Add the elements
  db.collection("ipca_data")
    .orderBy("student_number")
    .get()
    .then((snap) => {
      snap.docs.forEach((doc) => {
        document.querySelector("#list").innerHTML += addUserToList(doc);
      });
    });
}


// Add User
function addUser() {

  let e = userForm.courses;
  let selected_tag = e.options[e.selectedIndex].value;
  let selected_course = e.options[e.selectedIndex].text;
  let selected_role = userForm.role.value;

  db.collection("ipca_data")
    .add({
      name: userForm.name.value,
      student_number: userForm.email.value.split('@')[0], 
      contact: userForm.contact.value,
      age: userForm.age.value,
      role: selected_role,
      email: userForm.email.value,
      gender: userForm.gender.value
    })
    .then((docRef) => {

      uniqueid = docRef.id;

      console.log("role: " + selected_role);

      // Add course if its a student or course director
      if (selected_role == "Aluno" || selected_role == "Diretor de Curso") {
        
        console.log("tag: " + selected_tag);

        db.collection("ipca_data").doc(uniqueid).collection("course").add({
          name: selected_course,
          tag: selected_tag
        })
        .then(() => {
          userForm.reset();

          // Get course Id by course Tag
          db.collection("course")
          .where("tag", "==", selected_tag)
          .get()
            .then((snap3) => {
              snap3.docs.forEach((doc4) => {

                let courseID = doc4.id
                console.log("courseID: " + courseID)

                // Add user to user list in all subjects oficial chats
                /*
                db.collection("course")
                .doc(courseID)
                .collection("subject")
                .get()
                .then((snap) => {
                  snap.docs.forEach((doc) => {
                    
                    let subjectName = doc.data().name;
                    console.log("subjectName: " + subjectName)

                    db.collection("chat")
                    .where("name", "==", subjectName)
                    .get()
                    .then((snap2) => {

                      snap2.docs.forEach((doc2) => {

                        let isAdmin = false;
                        if (doc2.data().role == "Diretor de Curso") {
                          isAdmin = true;
                        }

                        console.log("data: " + doc2.data())

                        db.collection("chat")
                        .doc(doc2.id)
                        .collection("user")
                        .doc(uniqueid)
                        .set({
                          admin: isAdmin
                        })
                        .then(() => {
                          console.log("New user successfully added to oficial chat");
                        });

                      });
                    });

                  });
                }); */
                
              });
            });
          });

      }

    })
    .catch((error) => {
      console.error("Error adding document: ", error);
    });

}


// Edit User <--- Missing change if user change student to teacher and vice versa
function editUser(id) {
  let docRef;

  var e = userForm.courses;
  var selected_tag = e.options[e.selectedIndex].value;
  var selected_course = e.options[e.selectedIndex].text;

  docRef = db.collection("ipca_data").doc(id);
  docRef.set({
      name: userForm.name.value,
      student_number: userForm.email.value.split('@')[0],
      contact: userForm.contact.value,
      age: userForm.age.value,
      role: userForm.role.value,
      email: userForm.email.value,
      gender: userForm.gender.value
    })
    .then((docRef) => {

      // Add course if its a student or course director
      if (userForm.role.value == "Aluno") {

        db.collection("ipca_data").doc(id).collection("course").add({
          name: selected_course,
          tag: selected_tag
        })
        .then(() => {
          userForm.reset();
        });

      }

      console.log("New user successfully edited: ", docRef);
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove User
function removeUser(id) {
  id = id.replace("Rem", "");

  // get user course tag
  db.collection("ipca_data")
  .doc(id)
  .collection("course")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

        let courseTag = doc.data().tag;

        // remove user from course official chats
        db.collection("course")
        .where("tag", "==", courseTag)
        .get()
          .then((snap3) => {
            snap3.docs.forEach((doc4) => {

              let courseID = doc4.id
              console.log("courseID: " + courseID)

              // Remove user from user list in all subjects oficial chats
              /*
              db.collection("course")
              .doc(courseID)
              .collection("subject")
              .get()
                .then((snap) => {
                  snap.docs.forEach((doc) => {
                    
                    let subjectName = doc.data().name;
                    console.log("subjectName: " + subjectName)

                    db.collection("chat")
                    .where("name", "==", subjectName)
                    .get()
                    .then((snap2) => {

                      snap2.docs.forEach((doc2) => {

                        console.log("data: " + doc2.data())

                        db.collection("chat")
                        .doc(doc2.id)
                        .collection("user")
                        .doc(id)
                        .delete(() => {
                          recursive: true;
                        })
                        .then(() => {
                          console.log("New user successfully removed from oficial chat");
                        })
                        .catch((error) => {
                          console.error("Error removing document: ", error);
                        });

                      });
                    });
                }); 

              });*/

            });
            
          });
    });
  });

  // remove data from ipca_data
  db.collection("ipca_data").doc(id)
    .delete(() => {
      recursive: true;
    })
    .then(() => {
      console.log("Document successfully deleted!");

      // Refresh List
      refreshUserList()
    })
    .catch((error) => {
      console.error("Error removing document: ", error);
    });
}