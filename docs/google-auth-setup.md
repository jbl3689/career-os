# Google authentication setup

This guide prepares a development-only Google OAuth client for Career OS Stage
3. It does not enable Gmail scanning or make the application public.

Google changes its Cloud Console periodically. These steps use the current
**Google Auth Platform** pages: **Branding**, **Audience**, **Clients**, and
**Data Access**.

## What we are setting up

Google will authenticate the person, but Career OS will still own its user and
session data:

```text
Browser -> Career OS API -> Google authorization screen
Browser <- Career OS session cookie <- Career OS API
                         -> PostgreSQL user record
```

The initial sign-in will request only `openid`, `email`, and `profile`. Those
scopes identify the user; they do not grant access to Gmail messages.

The planned local data model is:

- `users`: Career OS identity, including Google's stable `sub` identifier,
  current email, display name, and timestamps;
- `job_applications.user_id`: ownership of each application;
- a server-side session: the browser stores only an opaque, `HttpOnly` session
  cookie, not a Google token;
- `google_connections`: added when Gmail connection is implemented, containing
  granted scopes and encrypted token data separately from the user record.

Google email addresses can change, so the unique Google `sub` value—not the
email address—will identify a returning user.

The ownerless Stage 2 development data was deleted before starting Stage 3.
This lets the ownership migration require every future application to belong to
a real Career OS user without inventing a legacy user or silently assigning old
records to whoever signs in first.

## 1. Create the development project

1. Open the [Google Cloud Console](https://console.cloud.google.com/).
2. Open the project selector in the top bar and select **New Project**.
3. Name it `career-os-dev`.
4. If Google asks for an organization or location and this is a personal Google
   account, leave the default selection.
5. Select **Create**, then make sure `career-os-dev` is the active project.

Use a separate project for a future production deployment. This prevents test
users, localhost redirects, and development credentials from being mixed with
the public application.

Billing is not required for this identity-only local setup.

## 2. Register the app with Google Auth Platform

1. In the console menu, open **Google Auth Platform** and then **Overview**.
2. Select **Get started** if the project has not been registered.
3. Enter `Career OS Development` as the app name.
4. Select your email address as the user support email.
5. For **Audience**, choose **External**. This allows a personal Gmail account
   to sign in. **Internal** is only appropriate when every user belongs to the
   same Google Workspace organization.
6. Enter your email address under developer contact information.
7. Accept Google's API Services User Data Policy acknowledgement and finish the
   registration.

Do not upload a logo or add homepage, privacy-policy, terms, or authorized-domain
values for localhost development. Those become relevant before a public launch.

## 3. Keep data access identity-only

Open **Google Auth Platform > Data Access**. For this first slice, request only:

- `openid`;
- `.../auth/userinfo.email`;
- `.../auth/userinfo.profile`.

These may already appear as default sign-in scopes. Do not add
`gmail.readonly`, `gmail.metadata`, or any other Gmail scope. Do not enable the
Gmail API yet; it is unnecessary for Google sign-in and belongs to the later
Gmail-connection slice.

## 4. Configure the development audience

1. Open **Google Auth Platform > Audience**.
2. Leave the publishing status as **Testing**.
3. Under **Test users**, add the personal Google account you will use while
   developing Career OS.

Basic identity-only sign-in does not normally require the test-user allowlist,
but adding yourself now prepares the project for the later Gmail permission
test. Do not publish the app or submit it for verification.

## 5. Create the local web client

1. Open **Google Auth Platform > Clients**.
2. Select **Create client**.
3. Choose **Web application** as the application type. The Spring Boot API is a
   confidential server application and can keep a client secret; this is not a
   browser-only JavaScript client.
4. Name the client `Career OS Local`.
5. Leave **Authorized JavaScript origins** empty. The browser will not call
   Google APIs directly.
6. Under **Authorized redirect URIs**, add this exact value:

   ```text
   http://localhost:8080/login/oauth2/code/google
   ```

7. Select **Create**.

The redirect points to Spring Security on the backend, not to Next.js on port
3000. Google sends a short-lived authorization code there; Spring exchanges it
server-to-server and then creates the Career OS session.

The scheme, hostname, port, path, case, and trailing slash must match exactly.
For example, `127.0.0.1` is not the same configured value as `localhost`.

## 6. Protect the generated credentials

Copy the generated client ID and client secret into a password manager or other
private temporary storage. Do not paste either value into this guide, commit it,
or put it directly in `application.properties`.

The next implementation slice will read them from these environment variables:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

The application now reads these variables. Copy the repository's `.env.example`
to the ignored `.env.local`, add the real values there, and follow the README
startup commands. The real values must remain only in that ignored file or the
local shell.

## Expected outcome

At the end of this setup, the Google Cloud project should show:

- project: `career-os-dev`;
- audience: External, Testing;
- test user: your development Google account;
- client type: Web application;
- redirect URI: `http://localhost:8080/login/oauth2/code/google`;
- data access: identity scopes only;
- Gmail API: not enabled;
- billing: not required.

Career OS uses these credentials only for identity sign-in. It does not yet
request a Gmail permission or access any mailbox data.

## Common setup problems

- `redirect_uri_mismatch`: compare every character of the URI with the value in
  **Clients**. Console changes can take several minutes to propagate.
- `access_denied`: confirm the intended account is selected and, once Gmail
  scopes are introduced, that it is listed under **Test users**.
- `org_internal`: the app was configured as Internal but the selected account
  is outside that Google Workspace organization. Change the audience to
  External for personal-account development.
- Client secret exposed in Git: revoke or delete the OAuth client immediately,
  create a replacement, and update the local secret. Removing it in a later
  commit does not remove it from Git history.

## Primary references

- [Google: manage OAuth clients](https://support.google.com/cloud/answer/15549257)
- [Google: OpenID Connect sign-in](https://developers.google.com/identity/openid-connect/openid-connect)
- [Google: OAuth app states and audiences](https://developers.google.com/identity/protocols/oauth2/production-readiness/overview)
- [Spring Security: OAuth2 client configuration](https://docs.spring.io/spring-boot/reference/security/oauth2.html)
