# ReLaunch Phase 2 deployment runbook

This runbook covers only the fixture-based Phase 2 application. It does not
enable live AI or require an API key.

## Information required before public deployment

- Exact ReLaunch subdomain.
- Authorized SSH host alias or server address and user.
- Deployment directory on the existing DigitalOcean server.
- Existing host Nginx configuration directory and virtual-host topology.
- DNS owner and update method.
- TLS termination method, such as the existing Certbot or Cloudflare setup.
- Current deployment backup and rollback convention.

Never place passwords, private keys, access tokens, or API keys in this
repository. Use the existing authorized credential channel.

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
```

The health response must be `{"status":"ok"}`. The demo response must contain
the fixed Priya fixture with 6 READY, 1 REFRESH, and 2 LEARN skills. Complete
the browser flow from Load example through Start over.

Stop the local stack when verification is complete:

```powershell
docker compose down
```

## Public deployment checklist

1. Confirm the server's existing Docker Compose, Nginx, DNS, and TLS topology.
2. Record the currently deployed configuration and image identifiers before
   changing anything so the existing state can be restored.
3. Transfer only the reviewed ReLaunch project files to the approved deployment
   directory. Do not transfer `.env` files, local caches, `node_modules`,
   `frontend/dist`, or `backend/target`.
4. Run `docker compose config` and `docker compose build` on the server.
5. Start the ReLaunch stack. Confirm that only the frontend port is bound to
   `127.0.0.1`; the backend must remain internal to the Compose network.
6. Render `nginx/relaunch-site.conf.template` with the approved hostname, place
   it in the existing host Nginx configuration structure, and run `nginx -t`
   before reloading Nginx.
7. Configure DNS and TLS using the server's existing approved process. Do not
   invent or replace the current certificate automation.
8. Verify the public health endpoint, fixed demo endpoint, and full Priya UI
   flow. Confirm that no AI provider is configured or required.

## Rollback

If public verification fails:

1. Restore the previously recorded host Nginx configuration and validate it
   with `nginx -t` before reloading.
2. Stop the ReLaunch Compose stack without deleting unrelated containers,
   networks, images, or volumes.
3. Restore the previously recorded deployment state.
4. Recheck all pre-existing public sites and report the failed ReLaunch check.

Do not use broad Docker cleanup commands or delete shared server resources.

## Completion boundary

Phase 2 is complete only when the approved public subdomain completes the
fixture-based Priya flow. Forrest's Phase 1 accounting-realism review remains a
separate pending acceptance item until he responds.
