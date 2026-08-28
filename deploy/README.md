# GCP VM production deployment

The production stack is defined in `compose.prod.yml`. Only the backend image
is built by CI. PostgreSQL, Redis, and Nginx use official images and run on the
same VM without exposing their internal ports.

The backend listens on port `8080` inside the private Compose network. Nginx is
the only public entry point on ports `80` and `443`. The configured memory
limits total about 2.9 GiB (backend 1.4 GiB, PostgreSQL 1.1 GiB, Redis 256 MiB,
Nginx 128 MiB), leaving room for Docker and the operating system on a 4 GiB VM.

## Safety rules

- Never run `docker compose down -v` or `docker volume prune`.
- `POSTGRES_VOLUME_NAME` must be the exact existing volume name.
- Keep `/srv/auction/.env` on the VM with mode `600`; do not commit it.
- Keep Flyway disabled until V1/V2 pass a rehearsal on a restored backup.
- The first adoption of the legacy PostgreSQL container is manual. Routine CD
  deliberately refuses to continue while that container is still running.

## One-time VM preparation

1. Copy `.env.example` to `/srv/auction/.env` and fill in secrets.
2. Identify the live PostgreSQL mount:

   ```bash
   docker inspect postgres_auction \
     --format '{{range .Mounts}}{{printf "%s %s %s -> %s\n" .Type .Name .Source .Destination}}{{end}}'
   ```

3. Put the exact named-volume value in `POSTGRES_VOLUME_NAME`. If the output is
   a bind mount, stop and adapt the compose volume mapping first.
4. Ensure the current PostgreSQL major version matches `POSTGRES_IMAGE`.
5. Ensure the existing TLS certificate is under
   `/etc/letsencrypt/live/api.auctionplatform.tinhlelaptrinh.id.vn`.
6. Create `/srv/auction/certbot/www` and restrict `/srv/auction/.env`:

   ```bash
   sudo mkdir -p /srv/auction/certbot/www /srv/auction/backups/postgres
   sudo chmod 600 /srv/auction/.env
   ```

If the backend repository is private on Docker Hub, log in to Docker Hub once
on the VM before enabling automated deployment.

## GitHub configuration

Configure these repository secrets:

- `DOCKER_USERNAME` and `DOCKER_PASSWORD`
- `GCP_VM_IP`, `GCP_VM_USER`, and optionally `GCP_SSH_PORT`
- `GCP_SSH_PRIVATE_KEY`
- `GCP_SSH_KNOWN_HOSTS`, captured from a trusted copy of the VM host key

Create a protected GitHub Environment named `production`. Keep the repository
variable `GCP_DEPLOY_ENABLED` unset or `false` during the one-time database
adoption. Change it to `true` only after the volume, backup, restored rehearsal,
TLS certificate, and first manual cutover have all been verified.

## Database rehearsal

Run `backup-postgres.sh`, restore the dump to an isolated PostgreSQL 15
instance, then start the backend against that clone with:

```dotenv
FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=true
FLYWAY_BASELINE_VERSION=0
```

Check migrated row counts for `users`, `products`, `images`, `product_images`,
`disputes`, and `dispute_evidences`. Migration V1 removes the legacy `images`
table after copying its data, so production must not be the first test.

After the first successful production migration, keep `FLYWAY_ENABLED=true`
and set `FLYWAY_BASELINE_ON_MIGRATE=false`.

## One-time PostgreSQL adoption

During the approved maintenance window:

1. Run and verify a new backup.
2. Stop the legacy backend so no writes can occur.
3. Stop the legacy PostgreSQL container.
4. Remove only the stopped legacy container, never its volume.
5. Run `preflight.sh` again and start the `postgres` service from the new
   compose file using the same external volume.
6. Verify `pg_isready` and critical row counts before starting the backend.

The exact stop/remove commands must be chosen only after inspecting the VM.
They are intentionally not automated in this repository.

## Routine deployment

```bash
sudo bash deploy/scripts/deploy.sh <tested-git-sha>
```

The script serializes deployments, verifies the volume and TLS files, creates a
checked PostgreSQL backup, pulls the immutable backend image, waits for service
health, tests the public HTTPS endpoint, and keeps the previous tagged image for
rollback. It never removes Docker volumes.

Backups in `/srv/auction/backups/postgres` protect the cutover, but they are on
the same VM disk. Copy verified dumps to a GCS bucket or another machine for
disaster recovery and monitor free disk space.
