# ReLaunch Phase 2 deployment and operations runbook

This runbook records the deployed fixture-based Phase 2 application. It does
not enable live AI or require an API key. Never place passwords, private keys,
access tokens, API keys, or user resume/job text in this repository or in
deployment logs.

## Deployed topology

- Public URL: `https://relaunch.hermanlidev.com`
- Deployed source commit: `379a924`
- Deployment root: `/www/wwwroot/relaunch`
- Immutable release: `/www/wwwroot/relaunch/releases/20260927T0710Z-dd69b03`
  (the suffix is the pre-GitHub commit ID; `dd69b03` and `379a924` have the
  same Git tree and differ only because commit email metadata was rewritten)
- Active release link: `/www/wwwroot/relaunch/current`
- Compose project: `relaunch`
- Frontend: healthy container, host binding `127.0.0.1:8088 -> 8080`
- Backend: healthy container, port `8080` exposed only inside the Compose network
- Host Nginx virtual host:
  `/www/server/panel/vhost/nginx/relaunch.hermanlidev.com.conf`

The host Nginx redirects HTTP to HTTPS, except for the HTTP ACME challenge,
and proxies HTTPS traffic to `127.0.0.1:8088`. ReLaunch has an independent
virtual host and Compose project; operating it must not modify or stop any
pre-existing application.

## Local verification

From the repository root:

```powershell
docker compose config
docker compose build
docker compose up -d
```

Verify:

```powershell
Invoke-RestMethod http://127.0.0.1:8088/api/health
Invoke-RestMethod http://127.0.0.1:8088/api/reentry/demo
Invoke-RestMethod http://127.0.0.1:8088/api/reentry/examples/maya-junior-accountant
```

The health response must be `{"status":"ok"}`. The Priya response must contain
9 skills (6 READY, 1 REFRESH, 2 LEARN) and one CPA credential. The Junior
Accountant response must contain 10 skills (4 READY, 2 REFRESH, 4 LEARN), no
credential, and weekly totals of 150, 120, and 45 minutes. Complete both
browser flows through Start over.

Stop the local stack when verification is complete:

```powershell
docker compose down
```

## Public verification

After any authorized ReLaunch-only maintenance:

1. Confirm `/www/wwwroot/relaunch/current` resolves to the intended immutable
   release.
2. Confirm both `relaunch` containers are healthy and that only the frontend is
   published on `127.0.0.1:8088`.
3. Check `http://127.0.0.1:8088/api/health` on the host.
4. Run the host Nginx configuration test before any authorized reload:
   `/www/server/nginx/sbin/nginx -t -c /www/server/nginx/conf/nginx.conf`.
5. Verify the public homepage, `/api/health`, `/api/reentry/demo`, and
   `/api/reentry/examples/maya-junior-accountant` over HTTPS.
6. Complete both fixed-example browser flows. Confirm that no AI provider is
   configured or required.

## TLS renewal

- Nginx certificate paths:
  `/www/server/panel/vhost/cert/relaunch.hermanlidev.com/fullchain.pem` and
  `/www/server/panel/vhost/cert/relaunch.hermanlidev.com/privkey.pem`
- Certificate source directory:
  `/www/server/panel/vhost/letsencrypt/relaunch.hermanlidev.com`
- Renewal script: `/www/wwwroot/relaunch/ops/relaunch_renew_cert.py`
- Cron definition: `/etc/cron.d/relaunch-cert-renew`
- Schedule: daily at `08:17` server time
- Renewal log: `/www/wwwlogs/relaunch-cert-renew.log`

The source-controlled copy is `deploy/ops/relaunch_renew_cert.py`. The script
is intentionally specific to the existing ReLaunch ACME order, domain, and
save path. It exits without changing anything outside the 30-day renewal
window. After renewal it requires at least 60 days of certificate validity,
tests the host Nginx configuration, and then reloads Nginx. On the server the
script must remain owned by `root:root` with mode `700`; the cron file must
remain `root:root` with mode `644`.

## Rollback

If public verification fails, roll back only ReLaunch:

1. Record the failing release, container state, and ReLaunch logs before making
   changes.
2. Repoint `/www/wwwroot/relaunch/current` only to a previously verified
   ReLaunch release. Never point it to an unverified or partial directory.
3. From that release, reconcile only the `relaunch` Compose project. Do not use
   broad Docker cleanup commands and do not stop, remove, or recreate unrelated
   containers, networks, images, or volumes.
4. If the ReLaunch virtual host caused the failure, restore only
   `/www/server/panel/vhost/nginx/relaunch.hermanlidev.com.conf` from the
   ReLaunch backup, test the complete Nginx configuration, and reload only
   after the test passes.
5. Re-run the local-origin and public checks above, then verify all pre-existing
   public sites still respond normally.

Do not delete or alter the ACME order, shared panel data, certificate source,
DNS records, or TLS automation as part of an application rollback. If there is
no known-good ReLaunch release or vhost backup, stop and obtain explicit
recovery approval instead of guessing.

## Completion boundary

Phase 2 is complete: the public subdomain completed both fixed-example flows,
and Forrest's accounting-realism review is recorded in
`docs/forrest-review.md`. This operational closeout does not authorize a live
AI provider, changes to the classification lists, or Phase 4 work.
