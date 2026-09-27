import datetime
import fcntl
import json
import os
import subprocess
import sys
import time

PANEL_ROOT = "/www/server/panel"
ORDER_INDEX = "2d8643a4ce9cc718f128b65efdbddd58"
DOMAIN = "relaunch.hermanlidev.com"
AUTH_TO = "/www/wwwroot/relaunch.hermanlidev.com"
CERT_FILE = PANEL_ROOT + "/vhost/letsencrypt/" + DOMAIN + "/fullchain.pem"
NGINX = "/www/server/nginx/sbin/nginx"
NGINX_CONFIG = "/www/server/nginx/conf/nginx.conf"
RENEW_WINDOW_SECONDS = 30 * 24 * 60 * 60


def log(message):
    timestamp = datetime.datetime.now(datetime.timezone.utc).isoformat()
    print(f"{timestamp} {message}", flush=True)


with open("/run/lock/relaunch-cert-renew.lock", "w", encoding="utf-8") as lock:
    try:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    except BlockingIOError:
        log("another renewal check is already running; exiting")
        raise SystemExit(0)

    os.chdir(PANEL_ROOT)
    sys.path.insert(0, PANEL_ROOT)
    sys.path.insert(0, PANEL_ROOT + "/class")

    from acme_v2 import acme_v2

    client = acme_v2()
    order = client.read_config().get("orders", {}).get(ORDER_INDEX)
    if not order:
        log("configured ACME order is missing")
        raise SystemExit(1)
    if order.get("domains") != [DOMAIN]:
        log("configured ACME order does not match the ReLaunch domain")
        raise SystemExit(1)
    if order.get("status") != "valid" or order.get("save_path") != "vhost/letsencrypt/" + DOMAIN:
        log("configured ACME order is not the expected valid ReLaunch order")
        raise SystemExit(1)

    expires_at = int(order.get("cert_timeout") or 0)
    if expires_at > int(time.time()) + RENEW_WINDOW_SECONDS:
        expiry = datetime.datetime.fromtimestamp(expires_at, datetime.timezone.utc).isoformat()
        log(f"certificate remains outside the renewal window; expires {expiry}")
        raise SystemExit(0)

    result = client.renew_cert_to([DOMAIN], "http", AUTH_TO, ORDER_INDEX)
    if not result or result.get("status") is not True:
        log("certificate renewal failed: " + json.dumps(result, ensure_ascii=False))
        raise SystemExit(1)

    subprocess.run(
        ["/usr/bin/openssl", "x509", "-checkend", str(60 * 24 * 60 * 60), "-noout", "-in", CERT_FILE],
        check=True,
    )
    subprocess.run([NGINX, "-t", "-c", NGINX_CONFIG], check=True)
    subprocess.run([NGINX, "-s", "reload", "-c", NGINX_CONFIG], check=True)
    log("certificate renewed, validated, and Nginx reloaded")
