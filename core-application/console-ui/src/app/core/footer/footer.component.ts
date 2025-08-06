import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'fury-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  visible = true;

  currentDate: Date = new Date();

  constructor() {
  }

  ngOnInit() {
    const year = "© " + this.currentDate.getFullYear() + ", ";
    fetch('./assets/custom.json')
      .then(function (response) {
        return response.json();
      })
      .then(function (data) {
        function appendData(data) {
          const footer = document.getElementById("footer");
          if(data.footer) {
            footer.textContent = year;
            footer.insertAdjacentText('beforeend', data.footer);
            footer.insertAdjacentText('beforeend', ". Tous droits réservés. developpé par KISS");
          }
        }
        appendData(data);
      })
      .catch(function(error) {
      });
  }

}
