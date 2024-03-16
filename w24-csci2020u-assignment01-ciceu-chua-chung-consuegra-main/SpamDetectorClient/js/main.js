
function requestDataFromServer(url){

  fetch(url,{
    method: 'GET',
    headers: {
      'Accept': 'application/json'

    }
  }).then(response => response.json())
    .then(data => {
      let tableRef = document.getElementById("result-list");
      // Clear existing content
      tableRef.innerHTML = '';
      // Create table if not already created
      let table = document.createElement('table');
      table.classList.add('data-table');
      // Add table header
      let headerRow = table.insertRow();
      let headers = ["File", "Spam Probability Rounded", "Spam Probability", "Actual Class"];
      headers.forEach(headerText => {
        let header = document.createElement("th");
        header.textContent = headerText;
        headerRow.appendChild(header);
      });

      // Populate table rows with data
      data.forEach(item => {
        let row = table.insertRow();
        let cellFile = row.insertCell();
        cellFile.textContent = item.file;
        let cellSpamProbRounded = row.insertCell();
        cellSpamProbRounded.textContent = item.spamProbRounded;
        let cellSpamProbability = row.insertCell();
        cellSpamProbability.textContent = item.spamProbability;
        let cellActualClass = row.insertCell();
        cellActualClass.textContent = item.actualClass;
      });

      tableRef.appendChild(table);
    })
    .then(fetch("http://localhost:8080/spamDetector-1.0/api/spam/accuracy", {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      }).then(response => response.json())
        .then(data => {
          console.log(data)
          let div = document.getElementById('accuracy');
          div.innerHTML = data;
        })
    )
    .then( fetch("http://localhost:8080/spamDetector-1.0/api/spam/accuracy",{
      method:'GET',
      headers:{
        'Accept': 'application/json'
      }
    }).then(response => response.json())
      .then(data => {
        console.log(data)
        let div = document.getElementById('accuracy');
        div.innerHTML= data;
      })

    )
    .then( fetch("http://localhost:8080/spamDetector-1.0/api/spam/precision",{
        method:'GET',
        headers:{
          'Accept': 'application/json'
        }
      }).then(response => response.json())
        .then(data => {
          console.log(data)
          let div = document.getElementById('precision');
          div.innerHTML= data;
        })

    )
    .catch(err =>{
      console.error("Error ", err );
    });
    //.then(response => console.log(JSON.stringify(response)))
}


(function (){
  let api ='http://localhost:8080/spamDetector-1.0/api/spam';

  requestDataFromServer(api);

})();
