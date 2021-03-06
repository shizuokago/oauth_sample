package kneetenzero

import (
	"encoding/gob"
	"fmt"
	"github.com/gorilla/sessions"
	"golang.org/x/oauth2"
	"google.golang.org/appengine"
	"html/template"
	"io/ioutil"
	"net/http"
)

var store = sessions.NewCookieStore([]byte("something-very-secret"))

func init() {
	http.HandleFunc("/", indexHandler)
	http.HandleFunc("/request", requestHandler)
	http.HandleFunc("/callback", callbackHandler)
	gob.Register(&oauth2.Config{})
}

func indexHandler(w http.ResponseWriter, r *http.Request) {

	tmpl, err := template.ParseFiles("./templates/index.tmpl")
	if err != nil {
		errorHandler(w, 500, err.Error())
		return
	}

	err = tmpl.Execute(w, nil)
	if err != nil {
		errorHandler(w, 500, err.Error())
	}
}

func requestHandler(w http.ResponseWriter, r *http.Request) {

	r.ParseForm()
	config := &oauth2.Config{
		ClientID:    r.FormValue("cid"),
		RedirectURL: "https://" + r.Host + "/callback",
		Scopes:      []string{"https://www.googleapis.com/auth/gmail.readonly"},
		Endpoint: oauth2.Endpoint{
			AuthURL:  "https://accounts.google.com/o/oauth2/auth",
			TokenURL: "https://accounts.google.com/o/oauth2/token",
		},
	}

	url := config.AuthCodeURL("ramdam", oauth2.AccessTypeOnline)

	config.ClientSecret = r.FormValue("csecret")
	session, _ := store.Get(r, "oauth")
	session.Values["config"] = config
	session.Save(r, w)

	http.Redirect(w, r, url, http.StatusTemporaryRedirect)
}

func callbackHandler(w http.ResponseWriter, r *http.Request) {

	context := appengine.NewContext(r)
	code := r.FormValue("code")

	session, _ := store.Get(r, "oauth")
	config, _ := session.Values["config"].(*oauth2.Config)

	token, err := config.Exchange(context, code)
	if err != nil {
		errorHandler(w, 500, err.Error())
		return
	}

	client := config.Client(context, token)

	resp, err := client.Get("https://www.googleapis.com/gmail/v1/users/me/labels")
	if err != nil {
		errorHandler(w, 500, err.Error())
		return
	}

	defer resp.Body.Close()
	b, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		errorHandler(w, 500, err.Error())
		return
	}
	fmt.Fprint(w, string(b))
}

func errorHandler(w http.ResponseWriter, status int, msg string) {
	w.WriteHeader(status)
	fmt.Fprint(w, msg)
}
