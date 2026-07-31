# Google authentication setup

This guide prepares a development-only Google OAuth client for Career OS Stage
3. It enables a separate Gmail permission flow, but it does not scan Gmail or
make the application public.

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

The initial sign-in requests only `openid`, `email`, and `profile`. Those scopes
identify the user; they do not grant access to Gmail messages. After sign-in,
the user may separately select **Connect Gmail**, which requests
`gmail.readonly`.

```text
Sign in:       Who is this user?             -> Career OS session
Connect Gmail: May Career OS read Gmail?     -> encrypted refresh token
Disconnect:   Revoke permission and delete  -> Career OS account remains
```

Career OS requires the connected Gmail account to be the same Google account
used to sign in. Supporting a different mailbox can be considered later if a
real use case justifies the extra account-selection complexity.

The planned local data model is:

- `users`: Career OS identity, including Google's stable `sub` identifier,
  current email, display name, and timestamps;
- `job_applications.user_id`: ownership of each application;
- a server-side session: the browser stores only an opaque, `HttpOnly` session
  cookie, not a Google token;
- `google_connections`: contains the connected address, granted scopes, and an
  encrypted refresh token separately from the user record.

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

There is no upfront charge for this local OAuth and Gmail API setup. A billing
account is not normally required for ordinary Gmail API development, although
Google applies API quotas and the console may prompt for billing for unrelated
Cloud services.

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

## 3. Configure data access

Open **Google Auth Platform > Data Access**. Keep these identity scopes:

- `openid`;
- `.../auth/userinfo.email`;
- `.../auth/userinfo.profile`.

Then add:

```text
https://www.googleapis.com/auth/gmail.readonly
```

Career OS does not request this scope during sign-in. It is requested only from
the separate **Connect Gmail** control.

`gmail.metadata` sounds narrower, but Google does not allow Gmail search
queries with that scope. Stage 4 needs search queries to find likely
job-related messages, so `gmail.readonly` is the narrowest scope that supports
the planned behaviour. It cannot send, modify, or delete messages.

Google classifies both `gmail.readonly` and `gmail.metadata` as restricted
scopes. A private project in Testing mode can be used by its configured test
users. A future public application will require Google's verification process,
and storing restricted-scope data on a server can require a security
assessment. That production work belongs to Stage 8.

## 4. Enable the Gmail API

1. In Google Cloud Console, confirm `career-os-dev` is still selected.
2. Open **APIs & Services > Library**.
3. Search for **Gmail API**.
4. Open it and select **Enable**.

Enabling the API does not give Career OS access to an inbox. The user must still
grant the Gmail scope through OAuth.

## 5. Configure the development audience

1. Open **Google Auth Platform > Audience**.
2. Leave the publishing status as **Testing**.
3. Under **Test users**, add the personal Google account you will use while
   developing Career OS.

The Gmail scope does require the account to be in this test-user allowlist. Do
not publish the app or submit it for verification yet.

In Testing mode, Google expires authorizations involving non-identity scopes
after seven days; the refresh token expires too. During development, reconnect
Gmail when this happens. It is expected test-project behaviour, not an
encryption bug.

## 6. Update the local web client

1. Open **Google Auth Platform > Clients**.
2. Select **Create client**.
3. Choose **Web application** as the application type. The Spring Boot API is a
   confidential server application and can keep a client secret; this is not a
   browser-only JavaScript client.
4. Name the client `Career OS Local`.
5. Leave **Authorized JavaScript origins** empty. The browser will not call
   Google APIs directly.
6. Under **Authorized redirect URIs**, keep the sign-in callback and add the
   Gmail callback, so the list contains both exact values:

   ```text
   http://localhost:8080/login/oauth2/code/google
   http://localhost:8080/login/oauth2/code/google-gmail
   ```

7. Select **Create**.

Both redirects point to Spring Security on the backend, not to Next.js on port
3000. The first completes identity sign-in. The second exchanges Gmail consent
for backend-only tokens and stores the refresh token in encrypted form.

The scheme, hostname, port, path, case, and trailing slash must match exactly.
For example, `127.0.0.1` is not the same configured value as `localhost`.

## 7. Protect credentials and create an encryption key

Copy the generated client ID and client secret into a password manager or other
private temporary storage. Do not paste either value into this guide, commit it,
or put it directly in `application.properties`.

The application reads the OAuth client values from:

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

The token encryption key is a different secret. Generate a random 32-byte key
from the repository root:

```bash
openssl rand -base64 32
```

Copy the single line of output into the ignored `.env.local`:

```text
TOKEN_ENCRYPTION_KEY=the generated Base64 value
```

Do not use the example test key from source code. Do not commit or share this
key. PostgreSQL stores only encrypted refresh-token data; the key must stay
outside PostgreSQL so a database leak alone is not enough to recover a token.
Changing or losing the key makes existing connections unreadable, requiring
users to reconnect Gmail.

## How token storage works

Google first returns an authorization code to the API. Spring Security validates
the OAuth state and exchanges that one-use code on the server. Career OS asks
for `access_type=offline`, so Google also returns a refresh token.

- access tokens are short-lived credentials used for Gmail API calls;
- the refresh token can obtain new access tokens without repeated consent;
- neither token is returned to Next.js or stored in browser local storage;
- the API encrypts the refresh token with AES-256-GCM and a fresh random nonce;
- AES-GCM also detects accidental or malicious changes to the ciphertext;
- encryption is tied to the owning Career OS user ID;
- the API discards Spring's temporary authorized-client copy after persistence.

Hashing is not suitable here. Password hashes are deliberately one-way, while
Stage 4 must recover the refresh token to obtain a short-lived access token.
Encryption is reversible only when the API has the separate key.

On disconnect, the API decrypts the token in memory and asks Google to revoke
it. The local `google_connections` row is deleted even if Google cannot revoke
an already expired token, so a broken grant cannot prevent reconnection. The
Career OS user and their job applications are not deleted.

## Expected outcome

At the end of this setup, the Google Cloud project should show:

- project: `career-os-dev`;
- audience: External, Testing;
- test user: your development Google account;
- client type: Web application;
- redirect URI: `http://localhost:8080/login/oauth2/code/google`;
- additional redirect URI:
  `http://localhost:8080/login/oauth2/code/google-gmail`;
- data access: identity scopes plus `gmail.readonly`;
- Gmail API: enabled;
- billing: not required.

Career OS can now store and revoke a Gmail grant. It still does not read any
mailbox data; that begins only after explicit approval to start Stage 4.

## Verify the complete Stage 3 flow

1. Restart the API after loading the updated `.env.local`.
2. Sign in at `http://localhost:3000/applications`.
3. Select **Connect Gmail**.
4. Choose the same account used for Career OS sign-in and approve read-only
   access.
5. Confirm the dashboard shows the connected Gmail address.
6. Restart the API and confirm the connection still appears.
7. Select **Disconnect Gmail** and confirm the connect control returns.

To confirm PostgreSQL never received the raw token:

```bash
docker compose exec database psql -U career_os -d career_os
```

Then run:

```text
SELECT user_id, gmail_address, granted_scopes,
       left(encrypted_refresh_token, 3) AS token_version
FROM google_connections;
\q
```

`token_version` should be `v1:`. Never print or paste the complete encrypted
value unnecessarily.

## Common setup problems

- `redirect_uri_mismatch`: compare every character of the URI with the value in
  **Clients**. Console changes can take several minutes to propagate.
- `access_denied`: confirm the intended account is selected and, once Gmail
  scopes are introduced, that it is listed under **Test users**.
- Gmail returns to the app but is not connected: ensure the Gmail callback URI
  ending in `/google-gmail` was added and `TOKEN_ENCRYPTION_KEY` is loaded.
- `missing-refresh-token`: reconnect and approve the consent screen. Career OS
  requests consent explicitly because Google may omit a refresh token from
  later exchanges otherwise.
- Account mismatch: sign in again and connect the same Google account. Career
  OS deliberately prevents attaching one user's grant to another user's data.
- Connection stops working after seven days: reconnect it. Testing-mode grants
  involving Gmail permissions expire after seven days.
- `org_internal`: the app was configured as Internal but the selected account
  is outside that Google Workspace organization. Change the audience to
  External for personal-account development.
- Client secret exposed in Git: revoke or delete the OAuth client immediately,
  create a replacement, and update the local secret. Removing it in a later
  commit does not remove it from Git history.

## Primary references

- [Google: manage OAuth clients](https://support.google.com/cloud/answer/15549257)
- [Google: OpenID Connect sign-in](https://developers.google.com/identity/openid-connect/openid-connect)
- [Google: web-server OAuth flow](https://developers.google.com/identity/protocols/oauth2/web-server)
- [Google: Gmail scopes](https://developers.google.com/workspace/gmail/api/auth/scopes)
- [Google: Gmail message search](https://developers.google.com/workspace/gmail/api/reference/rest/v1/users.messages/list)
- [Google: OAuth app states and audiences](https://developers.google.com/identity/protocols/oauth2/production-readiness/overview)
- [Spring Security: OAuth2 client configuration](https://docs.spring.io/spring-boot/reference/security/oauth2.html)
