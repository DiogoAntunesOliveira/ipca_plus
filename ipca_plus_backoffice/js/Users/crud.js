
// Add all courses to select object
function addCoursesToSelect(doc) {
  let course = doc.data();
  let html = "";

  html += '<option value="' + course.tag +'"> ' + course.name + ' </option>';

  console.log(doc.data())

  return html;
}


// Get all Users and display in the list
function addUserToList(doc) {
  let user = doc.data();
  let html = "";

  html += "<li><a>";
  html += user.student_number + " - " + user.name;
  html +=
    '<button id="' +
    doc.id +
    'Edit" class="edit" type="button" onClick="paraEdicao(this.id)"><i class="fa fa-edit"></i></button>';
  html +=
    '<button id="' +
    doc.id +
    'Rem" class="rem" type="button" onClick="removeAluno(this.id)"><i class="fa fa-trash"></i></button>';
  html += '<p class = "descPar"> Curso: ' + user.course + "</p>";
  html += '<p id="' + doc.id + 'Contacto" class = "descPar"></p>';

  html += "</a></li>";
  html += "<hr>";

  userForm.reset();
  document.querySelector("#addUser").value = "new";

  return html;
}


// Put user data in text boxes
function paraEdicao(id) {
  let docRef;
  id = id.replace("Edit", "");

  docRef = db.collection("ipca_data").doc(id);

  docRef.get().then((doc) => {
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
    editAluno(userForm.value);
    document.querySelector("#addUser").value = "new";
  }

  let menu = document.getElementById('list');
  while (menu.firstChild) {
      menu.removeChild(menu.firstChild);
  }

  db.collection("ipca_data")
  .get()
  .then((snap) => {
    snap.docs.forEach((doc) => {

  console.log("entrou")
      document.querySelector("#list").innerHTML += addUserToList(doc);

    });
  });

  userForm.reset();
});


// Add User
function addUser() {

  db.collection("ipca_data")
    .add({
      name: userForm.name.value,
      student_number: userForm.number.value,
      contact: userForm.contact.value,
      age: userForm.age.value,
      course: userForm.courses.innerText,
      course_tag: userForm.courses.value,
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

//editar um aluno - carrega formulário com dados
/*
function editUser(id) {
  let docRef;
  let i = 0;

  const array = [
    {
      type: "email",
      value: form.email.value,
    },
    {
      type: "mobile",
      value: form.mobile.value,
    },
  ];

  docRef = db.collection("ipca_data").doc(id);
  docRef
    .set({
      number: form.number.value,
      name: form.name.value,
      curso: form.curse.value,
    })
    docRef.collection("contactos").get().then((snap) => {
      snap.docs.forEach((doc) => {
        console.log(doc.data());
        docRef.collection("contactos").doc(doc.id).set({
          type: array[i].type,
          value: array[i].value,
        })
        i++;
      });
      console.log("Document successfully saved!");
    })
    .catch((error) => {
      console.error("Error saving document: ", error);
    });

}
*/


/*
//remove um aluno
function removeUser(id) {
  id = id.replace("Rem", "");
  docRef = db.collection("ipca_data").doc(id);

  docRef
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
 */