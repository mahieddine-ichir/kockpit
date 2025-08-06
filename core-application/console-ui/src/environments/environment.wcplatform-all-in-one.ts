// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `angular-cli.json`.

export const environment = {
  setCookie: function (name,value,days) {
    let expires = '';
    if (days) {
      const date = new Date();
      date.setTime(date.getTime() + (days*24*60*60*1000));
      expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
  },

  production: false,
  backend: 'http://localhost:8082/ihm_api', // Put your backend here
  autoDomainDiscovery: false,
  initFn: function () {
    console.log('Local dev specific hook to init JWT Cookie with local JWT.');
    // Special case for local dev (auto login with local JWT)
    const jwtXss = "eyJhbGciOiJSUzI1NiJ9.eyJjdXN0b206YWRncm91cHMiOlsidXNlcjEiLCJYU1MiXSwic3ViIjoiYm9iLmxvY2FsQGFjY29yLWRldi5sb2NhbCIsImNvZ25pdG86Z3JvdXBzIjoiZXUtd2VzdC0xX3ZXWDBZM3dNZF9BY3RpdmVEaXJlY3RvcnkiLCJST0xFUyI6WyJST0xFX1VTRVIiXSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImlzcyI6Imh0dHA6Ly9mYWtland0aXNzdWVyOjgwODMvaWhtX2FwaS9wdWJsaWMiLCJnaXZlbl9uYW1lIjoiQm9iIFhTUyIsImV4cCI6MjA4MzU3NTU3NywiZmFtaWx5X25hbWUiOiJMb2NhbCIsImVtYWlsIjoiYm9iLmxvY2FsQGFjY29yLWRldi5sb2NhbCIsInVzZXJuYW1lIjoiQWN0aXZlRGlyZWN0b3J5Ym9iLmxvY2FsQGFjY29yLWRldi5sb2NhbCJ9.i2jbWDIszUFKTHWRmOYU1I87QVYR0ir5kZ0eWUQ7rL5iw7vZzsMVvRu_Vf4gur6ETSCMYInvTD6F8Hf9WEdnYOX41GZwzSv_7GdtESwmiPrM3kqevcpNKT0NUn2z75iHOAwBr0HYYaRsuDwOYk6uiHxq1LkOlsH9xbEbMb8GfK4oOT8dhms9imKhoKeuLPtRxp2Il1It8oo-izWw4fYsJZ3-Am6blFDmq1hffMwcLsHLHxnUYduQcGKid3YduRjC3D1T4H0pXtXYwlqmDsVup6Netg38k33XYR2PapWBqlZ_BBCJq74QC8m8kWCENU10PmjFk5SDY9Jq69sIAcxE6g"
    const jwtWcc = "eyJhbGciOiJSUzI1NiJ9.eyJjdXN0b206YWRncm91cHMiOlsidXNlcjEiLCJXQ0MiXSwic3ViIjoiYm9iLmxvY2FsQGFjY29yLWRldi5sb2NhbCIsImNvZ25pdG86Z3JvdXBzIjoiZXUtd2VzdC0xX3ZXWDBZM3dNZF9BY3RpdmVEaXJlY3RvcnkiLCJST0xFUyI6WyJST0xFX1VTRVIiXSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4My9paG1fYXBpL3B1YmxpYyIsImdpdmVuX25hbWUiOiJCb2IgV0NDIiwiZXhwIjoyMDcxNjQ4NjY2LCJmYW1pbHlfbmFtZSI6IkxvY2FsIiwiZW1haWwiOiJib2IubG9jYWxAYWNjb3ItZGV2LmxvY2FsIiwidXNlcm5hbWUiOiJBY3RpdmVEaXJlY3Rvcnlib2IubG9jYWxAYWNjb3ItZGV2LmxvY2FsIn0.qoSk8Kda3FEsh9cSXMP5naayo2mwGH2ldpG8b0WTG9yRkXkWd1K6EbG0PvphlC75-4YNby1G7Vvyx1k_O8iFt3JjC89M1QKICUTcYkLeHVYpQ54vai2MafAmvhgfg8SS4p79hqjvB2_EQmaVrLyRvCc7BoQrBbzyH6o2E-g7S6iFC_lwnwdk_WfgfCuAgsmPhURurQnR-n9NGWdnzRk0kISJe72aQzyd_A8-6NJuC1RBiwIIcgjakYW8kk2VsNkhjCJ2xp2_1cGTyaHnLpuyiYfaPtMK1y3hTgWcsRU7cHXAlF7Bv2zxKNXw9tKZRN2-d7VUf7P8r3oGR9Kbef9t-w";

    let jwt = jwtXss;
    console.log('window.location.href: ', window.location.href);
    if (window.location.href.indexOf('wcc') > 0) {
      jwt = jwtWcc;
    }
    this.setCookie('fake.accessToken', jwt, 10);
    this.setCookie('fake.idToken', jwt, 10);
  }
};

