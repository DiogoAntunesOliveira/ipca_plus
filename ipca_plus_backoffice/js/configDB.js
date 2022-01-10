const firebaseConfig = {

  apiKey: "AIzaSyDZF70GwG11FSEs8PeOmdOb44RkNtSnJFY",

  authDomain: "ipcaplus.firebaseapp.com",

  projectId: "ipcaplus",

  storageBucket: "ipcaplus.appspot.com",

  messagingSenderId: "209455028652",

  appId: "1:209455028652:web:0fb8ae7e3b3f07bf7f4209",

  measurementId: "G-5KNGLDZ30R"

};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const db = firebase.firestore();
//const storageRef = firebase.storage().ref();

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




//get contactos
//  db.collectionGroup('contactos').get().then((snap) => {
//   snap.docs.forEach((doc) => {
//     //console.log(doc.data());
//   });
// });

//get contactos de aluno
// db.collection("Alunos/Cewntnb5JCxUc4kXi5iz/contactos")
//   .get()
//   .then((snap) => {
//     snap.docs.forEach((doc) => {
//       //console.log(doc.id);
//       //console.log(doc.data());

//     });
//   });


/* 
db.collection("Alunos")
  .where("curso", "==", "LEI")
  .orderBy("name")
  .limit(2)
  .get()
  .then((snap) => {
    snap.forEach((doc) => {
      console.log(doc.id, " => ", doc.data());
    });
  });

*/