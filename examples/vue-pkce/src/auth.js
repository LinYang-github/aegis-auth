import { UserManager, WebStorageStateStore } from 'oidc-client-ts';

export const userManager = new UserManager({
    authority: "http://localhost:9000",
    client_id: "demo-vue-client",
    redirect_uri: "http://localhost:5173/callback",
    response_type: "code",
    scope: "openid profile",
    post_logout_redirect_uri: "http://localhost:5173/",
    userStore: new WebStorageStateStore({ store: window.localStorage }),
    monitorSession: false // For simplicity
});

export const signOut = () => userManager.signoutRedirect();
export const signIn = () => userManager.signinRedirect();
