// Add all courses to select object
function addCoursesToSelect(doc) {
  let course = doc.data();
  let html = "";

  html += '<option value="' + course.tag + '"> ' + course.name + ' </option>';

  console.log(doc.data())

  return html;
}


// Get all Users and display in the list
function addUserToList(doc) {
  let user = doc.data();
  let html = "";

/*
  storageRef.child('profilePictures/' + doc.id + '.png').getDownloadURL().then(function(url) {
    var test = url;

    console.log("entrou ----> " + test);

  })*/

  html += "<li> <a>"; // <img src='../images/defaultUser.png' width='35' height='35'> style="padding-left:40px"
  html += user.student_number + " - " + user.name;
  html +=
    '<button id="' +
    doc.id +
    'Edit" class="edit" type="button" onClick="forEdition(this.id)"><i class="fa fa-edit"></i></button>';
  html +=
    '<button id="' +
    doc.id +
    'Rem" class="rem" type="button" onClick="removeUser(this.id)"><i class="fa fa-trash"></i></button>';
  html += '<p> Curso: ' + user.course + "</p>";

  html += "</a></li>";
  html += "<hr>";

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
    userForm.number.value = doc.data().student_number;
    userForm.contact.value = doc.data().contact;
    userForm.age.value = doc.data().age;
    userForm.courses.value = doc.data().course_tag;
    userForm.role.value = doc.data().role;
    userForm.email.value = doc.data().email;
    userForm.gender.value = doc.data().gender;

    document.querySelector("#addUser").value = "edit";

  });
}


// Submit button event
userForm.addEventListener("submit", (e) => {
  e.preventDefault();
  let button = document.querySelector("#addUser");

  console.log("submit")

  if (button.value == "new") {
    addUser();
  } else {
    editUser(userForm.value);
    document.querySelector("#addUser").value = "new";
  }

  // Refresh List
  refreshUserList()

  userForm.reset();
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

  var e = userForm.courses;
  var selected_tag = e.options[e.selectedIndex].value;
  var selected_course = e.options[e.selectedIndex].text;

  db.collection("ipca_data")
    .add({
      name: userForm.name.value,
      student_number: userForm.number.value,
      contact: userForm.contact.value,
      age: userForm.age.value,
      course: selected_course,
      course_tag: selected_tag,
      role: userForm.role.value,
      email: userForm.email.value,
      gender: userForm.gender.value
    })
    .then((docRef) => {
      console.log("New user successfully added: ", docRef);
    })
    .catch((error) => {
      console.error("Error adding document: ", error);
    });

}


// Edit User
function editUser(id) {
  let docRef;

  var e = userForm.courses;
  var selected_tag = e.options[e.selectedIndex].value;
  var selected_course = e.options[e.selectedIndex].text;

  docRef = db.collection("ipca_data").doc(id);
  docRef.set({
      name: userForm.name.value,
      student_number: userForm.number.value,
      contact: userForm.contact.value,
      age: userForm.age.value,
      course: selected_course,
      course_tag: selected_tag,
      role: userForm.role.value,
      email: userForm.email.value,
      gender: userForm.gender.value
    })
    .then((docRef) => {
      console.log("New user successfully edited: ", docRef);
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}


// Remove User
function removeUser(id) {
  id = id.replace("Rem", "");
  docRef = db.collection("ipca_data").doc(id);

  docRef
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