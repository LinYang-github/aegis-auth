package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"github.com/coreos/go-oidc/v3/oidc"
	"golang.org/x/oauth2"
)

var (
	clientID     = "demo-go-client"
	clientSecret = "demo-go-secret"
	redirectURL  = "http://localhost:8081/callback"
	issuerURL    = "http://localhost:9000" // Aegis Auth
)

func main() {
	ctx := context.Background()

	provider, err := oidc.NewProvider(ctx, issuerURL)
	if err != nil {
		log.Fatalf("Failed to query provider: %v", err)
	}

	oauth2Config := oauth2.Config{
		ClientID:     clientID,
		ClientSecret: clientSecret,
		RedirectURL:  redirectURL,

		// "openid" is a required scope for OpenID Connect flows.
		Scopes: []string{oidc.ScopeOpenID, "profile"},

		Endpoint: oauth2.Endpoint{
			AuthURL:   provider.Endpoint().AuthURL,
			TokenURL:  provider.Endpoint().TokenURL,
			AuthStyle: oauth2.AuthStyleInHeader,
		},
	}

	verifier := provider.Verifier(&oidc.Config{ClientID: clientID})

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/html; charset=utf-8")
		fmt.Fprintf(w, `<h1>Aegis Auth Go Demo</h1><a href="/login"><button>Login with Aegis Auth</button></a>`)
	})

	http.HandleFunc("/login", func(w http.ResponseWriter, r *http.Request) {
		http.Redirect(w, r, oauth2Config.AuthCodeURL("state", oauth2.AccessTypeOffline), http.StatusFound)
	})

	http.HandleFunc("/callback", func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Query().Get("state") != "state" {
			http.Error(w, "state did not match", http.StatusBadRequest)
			return
		}

		oauth2Token, err := oauth2Config.Exchange(ctx, r.URL.Query().Get("code"))
		if err != nil {
			http.Error(w, "Failed to exchange token: "+err.Error(), http.StatusInternalServerError)
			return
		}

		rawIDToken, ok := oauth2Token.Extra("id_token").(string)
		if !ok {
			http.Error(w, "No id_token field in oauth2 token.", http.StatusInternalServerError)
			return
		}

		idToken, err := verifier.Verify(ctx, rawIDToken)
		if err != nil {
			http.Error(w, "Failed to verify ID Token: "+err.Error(), http.StatusInternalServerError)
			return
		}

		resp := struct {
			OAuth2Token   *oauth2.Token
			IDTokenClaims json.RawMessage // ID Token payload is just JSON.
		}{oauth2Token, json.RawMessage{}}

		if err := idToken.Claims(&resp.IDTokenClaims); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		data, err := json.MarshalIndent(resp, "", "    ")
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		w.Write(data)
	})

	log.Printf("Go Demo listening on http://localhost:8081")
	log.Fatal(http.ListenAndServe(":8081", nil))
}
